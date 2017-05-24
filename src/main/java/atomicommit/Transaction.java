package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

public class Transaction {

  private final int myID;
  private final int nbInvolvedNodes;
  private ArrayList<NodeID> hasProposed;
  private boolean decision;
  private boolean hasDecided;
  private final Logger logger = LogManager.getLogger();

  Transaction(int n, int d) {
    myID = n;
    nbInvolvedNodes = d;
    hasProposed = new ArrayList();
    hasDecided = false;
    decision = true;
  }

  boolean setVote(NodeID id, boolean vote) {

    if (!hasProposed.contains(id)) {
      hasProposed.add(id);
      logger.debug("Received {} from Storage Node #{} for transaction #{}", vote, id, myID);
    }
    if (!vote) {
      decision = false;
    }
    if (hasProposed.size() == nbInvolvedNodes) {
      hasDecided = true;
    }
    return hasDecided;
  }

  boolean getDecision() {
    if (hasDecided) {
      return decision;
    } else {
      logger.warn("Trying to obtain decision value from transaction #{} while not yet determined", myID);
      return false;
    }
  }

}
