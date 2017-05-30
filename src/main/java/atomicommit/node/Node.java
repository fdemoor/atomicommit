package atomicommit.node;

import atomicommit.util.msg.Message;
import atomicommit.util.node.NodeID;
import atomicommit.channels.PerfectPointToPointLinks;

public abstract class Node {

  protected NodeConfig config;
  protected NodeID myID;
  protected PerfectPointToPointLinks channel;

  abstract public Message deliverMessage();

  public NodeConfig getConfig() {
    return config;
  }

  public NodeID getID() {
    return myID;
  }

  public void finish() {
    channel.close();
  }

}
