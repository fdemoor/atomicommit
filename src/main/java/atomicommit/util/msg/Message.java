package atomicommit.util.msg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;
import atomicommit.util.misc.Pair;

import java.util.Set;

public class Message {

  private final NodeID src;
  private final int id;
  private final MessageType type;
  private final Integer key;
  private final Set<Pair<NodeID,Boolean>> votes;
  private final Logger logger = LogManager.getLogger();

  public Message(NodeID srcID, int msgID, MessageType msgType) {
    src = srcID;
    id = msgID;
    type = msgType;
    key = null;
    votes = null;
  }

  public Message(NodeID srcID, int msgID, MessageType msgType, int k) {
    src = srcID;
    id = msgID;
    type = msgType;
    key = k;
    votes = null;
  }

  public Message(NodeID srcID, int msgID, MessageType msgType, Set<Pair<NodeID,Boolean>> l) {
    if (msgType != MessageType.TR_COLL && msgType != MessageType.TR_HELPED) {
      logger.error("TR_COLL or TR_HELPED expected but got {}", msgType);
    }
    src = srcID;
    id = msgID;
    type = msgType;
    key = null;
    votes = l;
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

  public Set<Pair<NodeID,Boolean>> getVotes() {
    return votes;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(id);
    sb.append(", ");
    sb.append(type);
    if (key != null) {
      sb.append(", ");
      sb.append(key);
    }
    if (votes != null) {
      sb.append(", ");
      sb.append(votes);
    }
    sb.append("]");
    return sb.toString();
  }

}
