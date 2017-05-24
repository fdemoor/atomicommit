package atomicommit;

public class Message {

  private final NodeID src;
  private final int id;
  private final MessageType type;

  Message(NodeID srcID, int msgID, MessageType msgType) {
    src = srcID;
    id = msgID;
    type = msgType;
  }

  NodeID getSrc() {
    return src;
  }

  int getID() {
    return id;
  }

  MessageType getType() {
    return type;
  }
}
