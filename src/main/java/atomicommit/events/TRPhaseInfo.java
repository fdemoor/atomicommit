package atomicommit.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;

import java.util.ArrayList;

public class TRPhaseInfo extends ProtocolInfo {

  private final ArrayList<NodeID> hasProposed;
  private final ArrayList<NodeID> hasAcked;
  private boolean decision;
  private boolean allVotes;
  private boolean allAcks;
  private final Logger logger = LogManager.getLogger();

  public TRPhaseInfo(int n) {
    super(n);
    hasProposed = new ArrayList<NodeID>();
    hasAcked = new ArrayList<NodeID>();
    decision = true;
    allVotes = false;
    allAcks = false;
  }

  boolean setVote(NodeID id, boolean vote) {
    if (!hasProposed.contains(id)) {
      hasProposed.add(id);
    }
    if (!vote) {
      decision = false;
    }
    if (hasProposed.size() == nbInvolvedNodes) {
      allVotes = true;
    }
    return allVotes;
  }

  boolean setAck(NodeID id) {
    if (!hasAcked.contains(id)) {
      hasAcked.add(id);
    }
    if (hasAcked.size() == nbInvolvedNodes) {
      allAcks = true;
    }
    return allAcks;
  }

  boolean getDecision() {
    if (allVotes) {
      return decision;
    } else {
      logger.warn("Trying to obtain decision value while not yet determined");
      return false;
    }
  }

}
