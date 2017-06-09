package atomicommit.events;

import atomicommit.node.TransactionManager;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgHandler3PCMaster implements EventHandler {

  private final TransactionManager trMng;
  private final Logger logger = LogManager.getLogger();

  public MsgHandler3PCMaster(TransactionManager manager) {
    trMng = manager;
  }

  private TRPhaseInfo getInfo(int trID) {
    ProtocolInfo info = trMng.getTransactionInfo(trID);
    if (info instanceof TRPhaseInfo) {
      return (TRPhaseInfo) info;
    } else {
      logger.error("TRPhaseInfo expected");
      return null;
    }
  }

  private void handleAck(int trID, NodeID id) {
    TRPhaseInfo info = getInfo(trID);
    if (info.setAck(id)) {
      trMng.sendToAllStorageNodes(trID, MessageType.TR_COMMIT);
      trMng.commitTransaction(trID);
    }
  }

  private void handleVote(int trID, NodeID id, boolean vote) {
    TRPhaseInfo info = getInfo(trID);
    if (info.setVote(id, vote)) {
      if (info.getDecision()) {
        trMng.sendToAllStorageNodes(trID, MessageType.TR_PREPARE);
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
      case TR_ACK:
        handleAck(trID, src);
        break;
    }
  }

}
