package atomicommit.eventhandler;

import atomicommit.*;
import atomicommit.perfectpointtopointlinks.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import javafx.util.Pair;

public class MessageHandler2PCMaster implements EventHandler {

  private final PerfectPointToPointLinks channel;
  private TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  public MessageHandler2PCMaster(PerfectPointToPointLinks ch, TransactionManager manager) {
    channel = ch;
    trMng = manager;
  }

  private void handleVote(int trID, NodeID id, boolean vote) {
    Transaction transaction = trMng.getTransaction(trID);
    if (transaction.setVote(id, vote)) {
      if (transaction.getDecision()) {
        trMng.sendToAllStorageNodes(trID, "COMMIT");
        logger.debug("[Transaction #{}] Decided to commit transaction", trID);
      } else {
        trMng.sendToAllStorageNodes(trID, "ABORT");
        logger.debug("[Transaction #{}] Decided to abort transaction", trID);
      }
    }
  }

  @Override
  public void handle(Object arg_) {
    Pair<NodeID,List<String>> messagePair = channel.deliver();
    List<String> messages = messagePair.getValue();
    int trID = new Integer(messages.get(0));
    String msg = messages.get(1);
    NodeID src = messagePair.getKey();
    switch(msg) {
      case "YES":
        handleVote(trID, src, true);
        break;
      case "NO":
        handleVote(trID, src, false);
        break;
    }
  }

}
