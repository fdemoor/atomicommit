package atomicommit.node;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.events.MsgHandler2PCMaster;
import atomicommit.events.MsgHandler3PCMaster;
import atomicommit.events.MsgHandler0NBACMaster;
import atomicommit.events.RunTransaction;
import atomicommit.events.TRPhaseInfo;
import atomicommit.util.misc.Counter;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.util.msg.MessageType;
import atomicommit.util.msg.Message;
import atomicommit.transaction.Transaction;
import atomicommit.channels.ZMQChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class TransactionManager extends Node {

  private final List<Integer> storageNodes;
  private final Counter transactionIDs;
  private int nbTrDone = 0;
  private int nbTrC = 0;
  private int nbTrA = 0;
  private long startTime;

  TransactionManager(NodeConfig conf, int id, List<Integer> servers, NodeIDWrapper wrapper) {

    super(conf, id, wrapper);

    storageNodes = servers;
    transactionIDs = new Counter();

    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      NodeID idNode = nodesWrapper.getNodeID(it.next());
      channel.addOut(idNode);
    }
    MessageHandler msgHandler = new MessageHandler(this);
    switch (config.getTrProtocol()) {
      case ZERO_NBAC:
      case INBAC:
        msgHandler.setTransactionHandler(new MsgHandler0NBACMaster(this));
        break;
      case THREE_PHASE_COMMIT:
        msgHandler.setTransactionHandler(new MsgHandler3PCMaster(this));
        break;
      case TWO_PHASE_COMMIT:
      default:
        msgHandler.setTransactionHandler(new MsgHandler2PCMaster(this));
        break;
    }
    channel.setMessageEventHandler(msgHandler);

  }


  /* TRANSACTION METHODS */

  public int startTransaction() {
    int trID = transactionIDs.get();
    transactionIDs.incr();
    Transaction tr = new Transaction(trID, this);
    sendToAllStorageNodes(trID, MessageType.TR_START);
    logger.debug("Transaction Manager #{} starts transaction #{}", myID, trID);
    transactions.put(trID, new TransactionWrapper(tr, new TRPhaseInfo(storageNodes.size())));

    if (nbTrDone == 0) {
      startTime = System.currentTimeMillis();
    }

    return trID;
  }

  public void tryCommit(int trID) {
    logger.debug("Transaction Manager #{} tries to commit transaction #{}", myID, trID);
    sendToAllStorageNodes(trID, MessageType.TR_XACT);
  }

  @Override
  public void commitTransaction(int trID) {
    if (transactions.get(trID).transaction.commit()) {
      nbTrC++;
    }
    logTransaction();
  }

  @Override
  public void abortTransaction(int trID) {
    if (transactions.get(trID).transaction.abort()) {
      nbTrA++;
    }
    logTransaction();
  }

  public void logTransaction() {
    nbTrDone++;
    if (nbTrDone == config.getNbTr()) {
      long duration = System.currentTimeMillis() - startTime;
      logger.info("Took {} ms for {} transactions: {} commited, {} aborted", duration, config.getNbTr(), nbTrC, nbTrA);
      //sendToAllStorageNodes(0, MessageType.NODE_STOP);
      //finish();
    }
  }

  public void runTransaction(int delay, int times) {
    EventHandler handlerTimer = new RunTransaction(this);
    channel.setTimeoutEventHandler(handlerTimer, delay, times, null);
  }


  /* CHANNEL METHODS */

  @Override
  public void sendToAllStorageNodes(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(nodesWrapper.getNodeID(it.next()), message);
    }
  }

  @Override
  public void sendToAllStorageNodes(int id, MessageType type, int k) {
    Message message = new Message(myID, id, type, k);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(nodesWrapper.getNodeID(it.next()), message);
    }
  }

}
