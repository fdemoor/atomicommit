package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import org.zeromq.ZThread;

public class StorageNode extends Node implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID trManager;
  private HashMap<Integer, Transaction> transactions;
  private final NodeIDWrapper nodesWrapper;
  private PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(int id, int manager) {

    nodesWrapper = new NodeIDWrapper(id);
    myID = nodesWrapper.getNodeID(id);
    trManager = nodesWrapper.getNodeID(manager);
    transactions = new HashMap<Integer, Transaction>();

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    channel.addOut(trManager);

  }

  void startTransaction(int trID) {
    logger.debug("Storage Node #{} starts transaction #{}", myID, trID);
    Transaction tr = new Transaction(trID);
    transactions.put(trID, tr);
  }

  void commitTransaction(int trID) {
    transactions.get(trID).commit();
    logger.debug("Storage Node #{} commits transaction #{}", myID, trID);
  }

  void abortTransaction(int trID) {
    transactions.get(trID).abort();
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
