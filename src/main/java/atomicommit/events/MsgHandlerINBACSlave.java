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
import java.util.List;
import java.util.Iterator;

public class MsgHandlerINBACSlave implements EventHandler {

  private final int f;
  private final int delay;
  private final StorageNode node;
  private final EventHandler timerHandler;
  private final Logger logger = LogManager.getLogger();

  private TRINBACInfo getInfo(int trID) {
    ProtocolInfo info = node.getTransactionInfo(trID);
    if (info instanceof TRINBACInfo) {
      return (TRINBACInfo) info;
    } else {
      logger.error("TRINBACInfo expected");
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
      TRINBACInfo info = getInfo(trID);
      int phase = info.getPhase();

      if  (phase == 1) {
        info.incrPhase();

      } else if (phase == 2) {
        Consensus cons = getCons(trID);
        if (true) {
          cons.setVote(false);
          logger.debug("Node {} proposes 0 to consensus", node.getID());
        } else {
          cons.setVote(true);
          logger.debug("Node {} proposes 1 to consensus", node.getID());
        }
        node.sendToNode(trID, MessageType.CONS_START, node.getID());
      }

    }

  }

  public MsgHandlerINBACSlave(StorageNode n) {
    node = n;
    timerHandler = new TimerHandler0NBAC();
    delay = node.getConfig().getMsgDelay();
    f = node.getConfig().getF();
  }

  private void handleXACT(int trID) {
    TRINBACInfo info = getInfo(trID);
    MessageType type;
    if (node.getTransanctionProposition(trID)) {
      info.setVal(true);
      type = MessageType.TR_YES;
    } else {
      info.setVal(false);
      type = MessageType.TR_NO;
    }
    List<NodeID> backUps = node.getIDWrapper().getFNodes(node.getID(), f);
    int i = node.getIDWrapper().getRank(node.getID());
    Iterator<NodeID> it = backUps.iterator();
    while (it.hasNext()) {
      node.sendToNode(trID, type, it.next());
    }
    if (i <= f) {
      node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);
    } else {
      node.setTimeoutEvent(timerHandler, delay * 2, 1, (Object) trID);
      info.incrPhase();
    }
    info.incrPhase();
    node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);
  }

  private void handleNO(int trID, NodeID src, int key) {
    TRINBACInfo info = getInfo(trID);
    int phase = info.getPhase();



  }

  private void handleACK(int trID, NodeID src) {
    TRINBACInfo info = getInfo(trID);
  }

  private void handleCONS(int trID, boolean vote) {
    TRINBACInfo info = getInfo(trID);
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
        handleNO(trID, src, message.getKey());
        break;
      case TR_ACK:
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
