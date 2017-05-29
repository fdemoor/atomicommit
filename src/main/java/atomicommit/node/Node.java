package atomicommit.node;

import atomicommit.util.msg.Message;
import atomicommit.util.node.NodeID;

public abstract class Node {

  protected NodeConfig config;
  protected NodeID myID;

  abstract public Message deliverMessage();

  public NodeConfig getConfig() {
    return config;
  }

  public NodeID getID() {
    return myID;
  }

}
