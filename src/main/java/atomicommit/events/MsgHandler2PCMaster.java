package atomicommit.events;

import atomicommit.node.TransactionManager;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgHandler2PCMaster implements EventHandler {

  private final TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  public MsgHandler2PCMaster(TransactionManager manager) {
    trMng = manager;
  }

  private void handleVote(int trID, NodeID id, boolean vote) {
    if (trMng.setTransactionVote(trID, id, vote)) {
      if (trMng.getTransactionDecision(trID)) {
        trMng.sendToAllStorageNodes(trID, MessageType.TR_COMMIT);
        trMng.commitTransaction(trID);
      } else {
        trMng.sendToAllStorageNodes(trID, MessageType.TR_ABORT);
        trMng.abortTransaction(trID);;
      }
    }
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
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
