package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler2PCMaster implements EventHandler {

  private final TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  MessageHandler2PCMaster(TransactionManager manager) {
    trMng = manager;
  }

  private void handleVote(int trID, NodeID id, boolean vote) {
    Transaction transaction = trMng.getTransaction(trID);
    if (transaction.setVote(id, vote)) {
      if (transaction.getDecision()) {
        trMng.sendToAllStorageNodes(trID, MessageType.TR_COMMIT);
        trMng.commitTransaction(trID);
      } else {
        trMng.sendToAllStorageNodes(trID, MessageType.TR_ABORT);
        trMng.abortTransaction(trID);;
      }
    }
  }

  @Override
  public void handle(Object arg_) {
    Message message = trMng.deliverMessage();
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    switch(type) {
      case TR_YES:
        handleVote(trID, src, true);
        break;
      case TR_NO:
        handleVote(trID, src, false);
        break;
    }
  }

}
