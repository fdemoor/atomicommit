package atomicommit.util.msg;

import atomicommit.util.node.NodeID;

public class Message {

  private final NodeID src;
  private final int id;
  private final MessageType type;
  private final Integer key;

  public Message(NodeID srcID, int msgID, MessageType msgType) {
    src = srcID;
    id = msgID;
    type = msgType;
    key = null;
  }

  public Message(NodeID srcID, int msgID, MessageType msgType, int k) {
    src = srcID;
    id = msgID;
    type = msgType;
    key = k;
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

  public Integer getKey() {
    return key;
  }

}
