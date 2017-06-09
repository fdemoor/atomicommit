package atomicommit.events;

import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgHandler3PCSlave implements EventHandler {

  private final StorageNode node;
  private final Logger logger = LogManager.getLogger();

  public MsgHandler3PCSlave(StorageNode n) {
    node = n;
  }

  private void handleXACT(int trID) {
    if (node.getTransanctionProposition(trID)) {
      node.sendToManager(trID, MessageType.TR_YES);
    } else {
      node.sendToManager(trID, MessageType.TR_NO);
    }
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    if (node.checkManager(src)) {
      switch(type) {
        case TR_XACT:
          handleXACT(trID);
          break;
        case TR_COMMIT:
          node.commitTransaction(trID);
          break;
        case TR_ABORT:
          node.abortTransaction(trID);
          break;
        case TR_PREPARE:
          node.sendToManager(trID, MessageType.TR_ACK);
      }
    }
  }

}
