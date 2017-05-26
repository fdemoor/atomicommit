package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgHandler0NBACMaster implements EventHandler {

  private final TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  MsgHandler0NBACMaster(TransactionManager manager) {
    trMng = manager;
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    switch(type) {
      case TR_COMMIT:
        trMng.commitTransaction(trID);
        break;
      case TR_ABORT:
        trMng.abortTransaction(trID);
        break;
    }
  }

}
