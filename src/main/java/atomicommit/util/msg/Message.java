package atomicommit.util.msg;

import atomicommit.util.node.NodeID;

public class Message {

  private final NodeID src;
  private final int id;
  private final MessageType type;

  public Message(NodeID srcID, int msgID, MessageType msgType) {
    src = srcID;
    id = msgID;
    type = msgType;
  }

  public NodeID getSrc() {
    return src;
  }

  public int getID() {
    return id;
  }

  public MessageType getType() {
    return type;
  }
}
