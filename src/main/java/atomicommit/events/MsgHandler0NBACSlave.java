package atomicommit.events;

import atomicommit.events.Consensus;
import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.HashMap;

public class MsgHandler0NBACSlave implements EventHandler {

  private int delay;
  private final StorageNode node;
  private final EventHandler timerHandler;
  private final Logger logger = LogManager.getLogger();

  private TR0NBACInfo getInfo(int trID) {
    ProtocolInfo info = node.getTransactionInfo(trID);
    if (info instanceof TR0NBACInfo) {
      return (TR0NBACInfo) info;
    } else {
      logger.error("TR0NBACInfo expected");
      return null;
    }
  }

  private Consensus getCons(int trID) {
    ProtocolInfo info = node.getConsensusInfo(trID);
    if (info instanceof Consensus) {
      return (Consensus) info;
    } else {
      logger.error("Consensus expected");
      return null;
    }
  }

  private void decide(int trID, boolean b) {
    if (b) {
      node.sendToManager(trID, MessageType.TR_COMMIT);
      node.commitTransaction(trID);
    } else {
      node.sendToManager(trID, MessageType.TR_ABORT);
      node.abortTransaction(trID);
    }
  }

  private class TimerHandler0NBAC implements EventHandler {

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
          node.setTimeoutEvent(this, delay * 2, 1, (Object) trID);
        } else {
          node.setTimeoutEvent(this, delay, 1, (Object) trID);
        }

      } else if (phase == 2) {
        Consensus cons = getCons(trID);
        if (info.allAcks(node.getTransanctionNbNodes(trID))) {
          cons.setVote(true);
        } else {
          cons.setVote(false);
        }
        node.sendToNode(trID, MessageType.CONS_START, node.getID());
      }

    }

  }

  public MsgHandler0NBACSlave(StorageNode n) {
    node = n;
    timerHandler = new TimerHandler0NBAC();
    delay = node.getConfig().getMsgDelay();
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
    node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);

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

  private void handleCONS(int trID, boolean vote) {
    TR0NBACInfo info = getInfo(trID);
    if (!info.getDecided()) {
      info.setDecided();
      decide(trID, vote);
    }
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
      case TR_CONS_COMMIT:
        handleCONS(trID, true);
        break;
      case TR_CONS_ABORT:
        handleCONS(trID, false);
        break;
    }
  }

}
