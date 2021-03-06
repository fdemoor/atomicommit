package atomicommit.events;

import atomicommit.events.Consensus;
import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;
import atomicommit.util.misc.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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

  private class TimerHandlerINBAC implements EventHandler {

    public void handle(Object arg_) {
      int trID = (int) arg_;
      TRINBACInfo info = getInfo(trID);
      int phase = info.getPhase();
      int i = node.getIDWrapper().getRank(node.getID());

      if (phase == 0) {

        if (i < f) {
          node.sendToAllStorageNodes(trID, MessageType.TR_COLL, info.getVote0());
          info.addVote1(node.getID(), info.getVote0());
          info.incrPhase();
          node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);

        } else if (i == f) {
          List<NodeID> backUps = node.getIDWrapper().getFNodes(node.getID(), f);
          Iterator<NodeID> it = backUps.iterator();
          while (it.hasNext()) {
            node.sendToNode(trID, MessageType.TR_COLL, it.next(), info.getVote0());
          }
          info.incrPhase();
          node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);

        }
      } else if (phase == 1) {

        if (!info.decided() && !info.proposed()) {

          if (i < f) {

            if (info.checkBackUpsVote1(info.getNbInvolvedNodes(), f)) {
              boolean decision = info.getAnd1(info.getNbInvolvedNodes());
              info.decide(decision);
              decide(trID, decision);

            } else {
              Pair<Boolean,Boolean> exists = info.checkExistVote1(f);
              Consensus cons = getCons(trID);

              if (exists.getFirst()) {
                info.propose(exists.getSecond());
                cons.setVote(exists.getSecond());

              } else {
                info.propose(false);
                cons.setVote(false);
              }

              node.sendToNode(trID, MessageType.CONS_START, node.getID());
            }

          } else {

            info.incrPhase();
            Iterator<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> it2 = info.getVote1().iterator();
            while (it2.hasNext()) {
              info.addVote0(it2.next().getSecond());
            }
            info.addVote0(node.getID(), info.getVal());

            if (info.checkAllVote1(info.getNbInvolvedNodes(), f)) {
              boolean decision = info.getAnd1(info.getNbInvolvedNodes());
              info.decide(decision);
              decide(trID, decision);

            } else if (info.cntGet() >= 1) {
              Pair<Boolean,Boolean> exists = info.checkExistVote1(f);
              Consensus cons = getCons(trID);

              if (exists.getFirst()) {
                info.propose(exists.getSecond());
                cons.setVote(exists.getSecond());

              } else {
                info.propose(false);
                cons.setVote(false);
              }

              node.sendToNode(trID, MessageType.CONS_START, node.getID());

            } else {
              info.setWait(true);
              List<NodeID> nodes = node.getIDWrapper().getOtherNodes(f);
              Iterator<NodeID> it3 = nodes.iterator();
              while (it3.hasNext()) {
                node.sendToNode(trID, MessageType.TR_HELP, it3.next());
              }
              checkWait(trID);
            }
          }
        }
      }
    }
  }

  public MsgHandlerINBACSlave(StorageNode n) {
    node = n;
    timerHandler = new TimerHandlerINBAC();
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
      if (i < f) {
        info.addVote0(node.getID(), info.getVal());
      }
      node.setTimeoutEvent(timerHandler, delay, 1, (Object) trID);
    } else {
      node.setTimeoutEvent(timerHandler, delay * 2, 1, (Object) trID);
      info.incrPhase();
    }
  }

  private void handleVote(int trID, NodeID src, boolean b) {
    TRINBACInfo info = getInfo(trID);
    int phase = info.getPhase();
    if (phase == 0) {
      info.addVote0(src, b);

      int i = node.getIDWrapper().getRank(node.getID());
      boolean receivedAll;
      if (i < f) {
        receivedAll = info.checkVotes0(info.getNbInvolvedNodes() + 1);
      } else if (i == f) {
        receivedAll = info.checkVotes0(f);
      } else {
        receivedAll = false;
      }
      if (receivedAll) {
        node.removeTimeoutEvent();
        node.setTimeoutEvent(timerHandler, 0, 1, (Object) trID);
      }
    }
  }

  private void handleHelp(int trID, NodeID src) {
    TRINBACInfo info = getInfo(trID);
    int i = node.getIDWrapper().getRank(node.getID());
    int phase = info.getPhase();
    if (phase == 2 && i >= f) {
      node.sendToNode(trID, MessageType.TR_HELPED, src, info.getVote0());
    }
  }

  private void handleHelped(int trID, NodeID src, Set<Pair<NodeID,Boolean>> votes) {
    TRINBACInfo info = getInfo(trID);
    int i = node.getIDWrapper().getRank(node.getID());
    if (i >= f) {
      info.addVoteHelp(votes);
      info.cntHelpIncr();
      checkWait(trID);
    }
  }

  private void checkWait(int trID) {
    TRINBACInfo info = getInfo(trID);
    int i = node.getIDWrapper().getRank(node.getID());
    if ( (info.cntGet() + info.cntHelpGet() >= info.getNbInvolvedNodes() - f)
      && info.isWaiting() && !info.proposed() && !info.decided() && i >= f ) {

        info.setWait(false);
        if (info.checkAllVote1(info.getNbInvolvedNodes(), f)) {
          boolean decision = info.getAnd1(info.getNbInvolvedNodes());
          info.decide(decision);
          decide(trID, decision);

        } else if (info.cntGet() >= 1) {
          Pair<Boolean,Boolean> exists = info.checkExistVote1(f);
          Consensus cons = getCons(trID);

          if (exists.getFirst()) {
            info.propose(exists.getSecond());
            cons.setVote(exists.getSecond());

          } else {
            info.propose(false);
            cons.setVote(false);
          }

          node.sendToNode(trID, MessageType.CONS_START, node.getID());

        } else {

          Consensus cons = getCons(trID);

          if (info.checkHelp(info.getNbInvolvedNodes())) {

            boolean b = info.getAndHelp();
            info.propose(b);
            cons.setVote(b);

          } else {
            info.propose(false);
            cons.setVote(false);
          }

          node.sendToNode(trID, MessageType.CONS_START, node.getID());

        }

      }
  }

  private void handleColl(int trID, NodeID src, Set<Pair<NodeID,Boolean>> votes) {
    TRINBACInfo info = getInfo(trID);
    info.addVote1(src, votes);
    info.cntIncr();
    if (info.cntGet() == f) {
      node.removeTimeoutEvent();
      node.setTimeoutEvent(timerHandler, 0, 1, (Object) trID);
    }
  }

  private void handleCONS(int trID, boolean vote) {
    TRINBACInfo info = getInfo(trID);
    if (!info.decided()) {
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
        handleVote(trID, src, false);
        break;
      case TR_YES:
        handleVote(trID, src, true);
        break;
      case TR_COLL:
        handleColl(trID, src, message.getVotes());
        break;
      case TR_HELP:
        handleHelp(trID, src);
        break;
      case TR_HELPED:
        handleHelped(trID, src, message.getVotes());
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
