package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZThread;

public class StorageNode extends Node implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID trManager;
  private final NodeIDWrapper nodesWrapper;
  private PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(int id, int manager) {

    nodesWrapper = new NodeIDWrapper(id);
    myID = nodesWrapper.getNodeID(id);
    trManager = nodesWrapper.getNodeID(manager);

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    channel.addOut(trManager);

  }

  void commitTransaction(int trID) {
    logger.debug("Storage Node #{} commits transaction #{}", myID, trID);
  }

  void abortTransaction(int trID) {
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
