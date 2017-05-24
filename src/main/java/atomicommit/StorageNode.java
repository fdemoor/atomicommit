package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import org.zeromq.ZThread;

public class StorageNode extends Node implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID trManager;
  private final HashMap<Integer, TransactionWrapper> transactions;
  private final NodeIDWrapper nodesWrapper;
  private final PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(int id, int manager) {

    nodesWrapper = new NodeIDWrapper(id);
    myID = nodesWrapper.getNodeID(id);
    trManager = nodesWrapper.getNodeID(manager);
    transactions = new HashMap<Integer, TransactionWrapper>();

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    channel.addOut(trManager);

  }

  private class TransactionWrapper {

    private final Transaction transaction;
    private final TRProtocolInfo info;

    TransactionWrapper(Transaction tr, TRProtocolInfo prt) {
      transaction = tr;
      info = prt;
    }

  }

  void startTransaction(int trID) {
    logger.debug("Storage Node #{} starts transaction #{}", myID, trID);
    Transaction tr = new Transaction(trID);
    transactions.put(trID, new TransactionWrapper(tr, null));
  }

  TRProtocolInfo getTransactionInfo(int trID) {
    return transactions.get(trID).info;
  }

  void commitTransaction(int trID) {
    transactions.get(trID).transaction.commit();
    logger.debug("Storage Node #{} commits transaction #{}", myID, trID);
  }

  void abortTransaction(int trID) {
    transactions.get(trID).transaction.abort();
    logger.debug("Storage Node #{} aborts transaction #{}", myID, trID);
  }

  void sendToManager(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    channel.send(trManager, message);
  }

  Message deliverMessage() {
    return channel.deliver();
  }

  boolean checkManager(NodeID id) {
    boolean test = trManager.equals(id);
    if (!test) {
      logger.warn("Storage Node #{} - Message not coming from manager #{} but from #{}", trManager, id);
    }
    return test;
  }

  @Override
  public void run(Object[] args) {

    MessageHandler msgHandler = new MessageHandler(this);
    EventHandler handler = new MsgHandler2PCSlave(this);
    msgHandler.setTransactionHandler(handler);
    channel.setMessageEventHandler(msgHandler);

    channel.startPolling();
    channel.close();
  }



}
