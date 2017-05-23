package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler2PCMaster implements EventHandler {

  private final PerfectPointToPointLinks channel;
  private TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  MessageHandler2PCMaster(PerfectPointToPointLinks ch, TransactionManager manager) {
    channel = ch;
    trMng = manager;
  }

  private void handleVote(int trID, NodeID id, boolean vote) {
    Transaction transaction = trMng.getTransaction(trID);
    if (transaction.setVote(id, vote)) {
      if (transaction.getDecision()) {
        Message message = new Message(trMng.getID(), trID, MessageType.TR_COMMIT);
        trMng.sendToAllStorageNodes(message);
        logger.debug("[Transaction #{}] Decided to commit transaction", trID);
      } else {
        Message message = new Message(trMng.getID(), trID, MessageType.TR_ABORT);
        trMng.sendToAllStorageNodes(message);
        logger.debug("[Transaction #{}] Decided to abort transaction", trID);
      }
    }
  }

  @Override
  public void handle(Object arg_) {
    Message message = channel.deliver();
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
