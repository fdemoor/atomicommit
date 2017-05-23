package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZThread;

public class StorageNode implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID trManager;
  private PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(NodeID id, NodeID manager) {

    myID = id;
    trManager = manager;

    channel = new ZMQChannel();
    channel.setIn(myID);
    channel.addOut(trManager);

  }

  @Override
  public void run(Object[] args) {

    EventHandler handler = new MessageHandler2PCSlave(channel, myID, trManager);
    channel.setMessageEventHandler(handler);

    channel.startPolling();
    channel.close();
  }



}
