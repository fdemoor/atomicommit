package atomicommit.events;

import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class MsgHandler2PCSlave implements EventHandler {

  private final StorageNode node;
  private final Logger logger = LogManager.getLogger();

  public MsgHandler2PCSlave(StorageNode n) {
    node = n;
  }

  private void handleXACT(int trID) {

    Random rand = new Random();
    int commitProba = 75;
    int randint = rand.nextInt(100);
    MessageType choice = MessageType.TR_NO;
    if (randint < commitProba) {
      choice = MessageType.TR_YES;
    }

    node.sendToManager(trID, choice);

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
      }
    }
  }

}
