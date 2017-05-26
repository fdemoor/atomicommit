package atomicommit.events;

import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.HashMap;

public class MsgHandler0NBACSlave implements EventHandler {

  private int DELAY = 1000;

  private final StorageNode node;
  private final Logger logger = LogManager.getLogger();
  private final EventHandler timerHandler;

  private TR0NBACInfo getInfo(int trID) {
    TRProtocolInfo info = node.getTransactionInfo(trID);
    if (info instanceof TR0NBACInfo) {
      return (TR0NBACInfo) info;
    } else {
      logger.error("TR0NBACInfo expected");
      return null;
    }
  }

  private class timerHandler0NBAC implements EventHandler {

    private void decide(int trID, boolean b) {
      if (b) {
        node.sendToManager(trID, MessageType.TR_COMMIT);
        node.commitTransaction(trID);
      } else {
        node.sendToManager(trID, MessageType.TR_ABORT);
        node.abortTransaction(trID);
      }
    }

    public void handle(Object arg_) {
      int trID = (int) arg_;
      TR0NBACInfo info = getInfo(trID);
      int phase = info.getPhase();

      if  (phase == 1) {
        info.incrPhase();
        if (!info.getZero() && info.getVote()) {
          info.setDecided();
          decide(trID, true);
        } else if (info.getZero() && info.getVote()) {
          node.sendToAllStorageNodes(trID, MessageType.TR_NO);
          node.setTimeoutEvent(this, DELAY * 2, 1, (Object) trID);
        } else {
          node.setTimeoutEvent(this, DELAY, 1, (Object) trID);
        }

      } else if (phase == 2) {
        if (info.allAcks(node.getTransanctionNbNodes(trID))) {
          // FIXME: Propose 1 to Consensus
          decide(trID, false);
        } else {
          // FIXME: Propose 0 to Consensus
          decide(trID, false);
        }
      }

    }

  }

  public MsgHandler0NBACSlave(StorageNode n) {
    node = n;
    timerHandler = new timerHandler0NBAC();
  }

  private void handleXACT(int trID) {

    Random rand = new Random();
    int commitProba = 75;
    int randint = rand.nextInt(100);
    MessageType choice = MessageType.TR_NO;
    if (randint < commitProba) {
      choice = MessageType.TR_YES;
    }

    TR0NBACInfo info = getInfo(trID);

    switch (choice) {
      case TR_NO:
        info.setVote(false);
        node.sendToAllStorageNodes(trID, MessageType.TR_NO);
        break;
      default:
        info.setVote(true);
        break;
    }
    info.incrPhase();
    node.setTimeoutEvent(timerHandler, DELAY, 1, (Object) trID);

  }

  private void handleNO(int trID, NodeID src) {
    TR0NBACInfo info = getInfo(trID);
    int phase = info.getPhase();

    if (phase == 1) {
      info.setZero();
      node.sendToNode(trID, MessageType.TR_ACK, src);

    } else if (phase == 2) {
      if ( ! (info.getVote() && info.getDecided()) ) {
        node.sendToNode(trID, MessageType.TR_ACK, src);
      }
    }

  }

  private void handleACK(int trID, NodeID src) {
    TR0NBACInfo info = getInfo(trID);
    info.addAck(src);
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    switch(type) {
      case TR_XACT:
        if (node.checkManager(src)) {
          handleXACT(trID);
        }
        break;
      case TR_NO:
        handleNO(trID, src);
        break;
      case TR_ABORT:
        handleACK(trID, src);
        break;
    }
  }

}
