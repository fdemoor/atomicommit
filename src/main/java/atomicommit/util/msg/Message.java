package atomicommit.util.msg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;
import atomicommit.util.misc.Pair;

import java.util.List;

public class Message {

  private final NodeID src;
  private final int id;
  private final MessageType type;
  private final Integer key;
  private final List<Pair<NodeID,Boolean>> votes;
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

  public Message(NodeID srcID, int msgID, MessageType msgType, List<Pair<NodeID,Boolean>> l) {
    if (msgType != MessageType.TR_COLL) {
      logger.error("TR_COLL expected");
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

  public List<Pair<NodeID,Boolean>> getVotes() {
    return votes;
  }

}
