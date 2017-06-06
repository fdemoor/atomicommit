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

  /** Returns ID of the sender
   * @return  sender ID
   */
  public NodeID getSrc() {
    return src;
  }

  /** Returns message identifier
   * @return  identifier
   */
  public int getID() {
    return id;
  }

  /** Returns message type
   * @return  message type
   */
  public MessageType getType() {
    return type;
  }

  /** Returns message key if given
   * @return message key or null if no key was given
   */
  public Integer getKey() {
    return key;
  }

}
