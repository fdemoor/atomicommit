package atomicommit.events;

import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgHandlerStartTR implements EventHandler {

  private final StorageNode node;
  private final Logger logger = LogManager.getLogger();

  public MsgHandlerStartTR(StorageNode n) {
    node = n;
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    assert(type == MessageType.TR_START);
    node.startTransaction(trID);
  }

}
