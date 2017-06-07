package atomicommit.events;

import atomicommit.node.StorageNode;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class RaftLeaderElection implements EventHandler {

  private int delay;
  private Random rand;
  private final StorageNode node;
  private final EventHandler timerHandler;
  private final Logger logger = LogManager.getLogger();

  public RaftLeaderElection(StorageNode n) {
    node = n;
    timerHandler = new TimerHandlerConsensus();
    delay = node.getConfig().getMsgDelay();
    rand = node.getConfig().getRandom();
  }

  private Consensus getInfo(int trID) {
    ProtocolInfo info = node.getConsensusInfo(trID);
    if (info instanceof Consensus) {
      return (Consensus) info;
    } else {
      logger.error("Consensus expected");
      return null;
    }
  }

  private void setTimer(int trID) {
    int start = rand.nextInt(delay);
    node.setTimeoutEvent(timerHandler, delay * 2 + start, 1, (Object) trID);
  }

  private class TimerHandlerConsensus implements EventHandler {

    public void handle(Object arg_) {
      int trID = (int) arg_;
      Consensus info = getInfo(trID);

      if (!info.isDone()) {
        info.incrPhase();
        node.sendToAllStorageNodes(trID, MessageType.CONS_XACT, info.getPhase());
        info.setTryingLead();

        setTimer(trID);
      }
    }

  }

  private void handleXACT(int trID, NodeID src, int k) {
    Consensus info = getInfo(trID);
    if (info.isStarted()) {
      int phase = info.getPhase();
      if (info.isTryingLead() && phase == k) {
        node.sendToNode(trID, MessageType.CONS_NO, src);
      } else {
        node.sendToNode(trID, MessageType.CONS_YES, src);
        info.resetTryingLead();
      }
      if (!info.isDone()) {
        node.removeTimeoutEvent();
        setTimer(trID);
      }
    }
  }

  private void handleYES(int trID, NodeID src) {
    Consensus info = getInfo(trID);
    info.addAck(src);
    if (info.enoughAcks(node.getTransanctionNbNodes(trID)) && !info.isDone()) {
      lead(trID);
    }
  }

  private void lead(int trID) {
    Consensus info = getInfo(trID);
    info.done();
    info.setElected();
    boolean vote = info.getVote();
    if (vote) {
      node.sendToAllStorageNodes(trID, MessageType.CONS_COMMIT);
      node.sendToNode(trID, MessageType.TR_CONS_COMMIT, node.getID());
    } else {
      node.sendToAllStorageNodes(trID, MessageType.CONS_ABORT);
      node.sendToNode(trID, MessageType.TR_CONS_ABORT, node.getID());
    }
  }

  private void follow(int trID, boolean vote) {
    Consensus info = getInfo(trID);
    info.done();
    if (vote) {
      node.sendToNode(trID, MessageType.TR_CONS_COMMIT, node.getID());
    } else {
      node.sendToNode(trID, MessageType.TR_CONS_ABORT, node.getID());
    }
  }

  public void handle(Object arg_) {
    Message message = (Message) arg_;
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();

    switch(type) {
      case CONS_START:
        Consensus info = getInfo(trID);
        info.start();
        setTimer(trID);
        break;
      case CONS_XACT:
        handleXACT(trID, src, message.getKey());
        break;
      case CONS_YES:
        handleYES(trID, src);
        break;
      case CONS_NO:
        break;
      case CONS_COMMIT:
        follow(trID, true);
        break;
      case CONS_ABORT:
        follow(trID, false);
        break;
    }
  }

}
