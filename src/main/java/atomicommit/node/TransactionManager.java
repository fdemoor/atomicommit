package atomicommit.node;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.events.MsgHandler2PCMaster;
import atomicommit.events.MsgHandler0NBACMaster;
import atomicommit.events.RunTransaction;
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
import java.util.HashMap;

public class TransactionManager extends Node {

  private final List<Integer> storageNodes;
  private final HashMap<Integer, TransactionWrapper> transactions;;
  private final Counter transactionIDs;
  private int nbTrDone = 0;
  private int nbTrC = 0;
  private int nbTrA = 0;
  private long startTime;

  TransactionManager(NodeConfig conf, int id, List<Integer> servers, NodeIDWrapper wrapper) {

    config = conf;
    nodesWrapper = wrapper;
    storageNodes = servers;
    myID = nodesWrapper.getNodeID(id);
    transactionIDs = new Counter();

    transactions = new HashMap<Integer,TransactionWrapper>();

    channel = new ZMQChannel(nodesWrapper, this);
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
      case TWO_PHASE_COMMIT:
      default:
        msgHandler.setTransactionHandler(new MsgHandler2PCMaster(this));
        break;
    }
    channel.setMessageEventHandler(msgHandler);

  }


  /* TRANSACTION WRAPPER */

  private class TransactionWrapper {

    private final Transaction transaction;
    private final int nbInvolvedNodes;
    private final ArrayList<NodeID> hasProposed;
    private boolean decision;
    private boolean hasDecided;
    private boolean done;

    TransactionWrapper(Transaction tr, int n) {
      transaction = tr;
      nbInvolvedNodes = n;
      hasProposed = new ArrayList<NodeID>();
      hasDecided = false;
      decision = true;
      done = false;
    }

    boolean setVote(NodeID id, boolean vote) {

      if (!hasProposed.contains(id)) {
        hasProposed.add(id);
        logger.debug("Received {} from Storage Node #{} for transaction #{}", vote, id, transaction.getID());
      }
      if (!vote) {
        decision = false;
      }
      if (hasProposed.size() == nbInvolvedNodes) {
        hasDecided = true;
      }
      return hasDecided;
    }

    boolean getDecision() {
      if (hasDecided) {
        return decision;
      } else {
        logger.warn("Trying to obtain decision value from transaction #{} while not yet determined", myID);
        return false;
      }
    }

    void setDone() {
      done = true;
    }

    boolean isDone() {
      return done;
    }

  }


  /* TRANSACTION METHODS */

  public int startTransaction() {
    int trID = transactionIDs.get();
    transactionIDs.incr();
    Transaction tr = new Transaction(trID, this);
    sendToAllStorageNodes(trID, MessageType.TR_START);
    logger.debug("Transaction Manager #{} starts transaction #{}", myID, trID);
    transactions.put(trID, new TransactionWrapper(tr, storageNodes.size()));

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
    if (!transactions.get(trID).isDone()) {
      transactions.get(trID).setDone();
      logger.debug("Transaction Manager #{} commits transaction #{}", myID, trID);
      Transaction tr = transactions.get(trID).transaction;
      tr.commit();
      nbTrC++;
      logTransaction();
    }
  }

  @Override
  public void abortTransaction(int trID) {
    if (!transactions.get(trID).isDone()) {
      transactions.get(trID).setDone();
      logger.debug("Transaction Manager #{} aborts transaction #{}", myID, trID);
      Transaction tr = transactions.get(trID).transaction;
      tr.abort();
      nbTrA++;
      logTransaction();
    }
  }

  public void logTransaction() {
    nbTrDone++;
    if (nbTrDone == config.getNbTr()) {
      long duration = System.currentTimeMillis() - startTime;
      logger.info("Took {} ms for {} transactions: {} commited, {} aborted", duration, config.getNbTr(), nbTrC, nbTrA);
      sendToAllStorageNodes(0, MessageType.NODE_STOP);
      finish();
    }
  }

  public boolean setTransactionVote(int trID, NodeID id, boolean vote) {
    return transactions.get(trID).setVote(id, vote);
  }

  public boolean getTransactionDecision(int trID) {
    return transactions.get(trID).getDecision();
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
