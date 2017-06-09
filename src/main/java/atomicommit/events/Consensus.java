package atomicommit.events;

import atomicommit.util.node.NodeID;

import java.util.List;
import java.util.ArrayList;

public class Consensus extends ProtocolInfo {

  private boolean started;
  private boolean myVote;
  private final List<NodeID> myAcks;
  private boolean elected;
  private boolean done;
  private boolean tryingLead;
  private int phase;

  public Consensus(int n) {
    super(n);
    started = false;
    myAcks = new ArrayList<NodeID>();
    elected = false;
    done = false;
    tryingLead = false;
    phase = 0;
  }

  public void start() {
    started = true;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isDone() {
    return done;
  }

  public void done() {
    done = true;
  }

  public void incrPhase() {
    phase++;
  }

  public int getPhase() {
    return phase;
  }

  public void setTryingLead() {
    tryingLead = true;
  }

  public boolean isTryingLead() {
    return tryingLead;
  }

  public void resetTryingLead() {
    tryingLead = false;
  }

  public void setVote(boolean b) {
    myVote = b;
  }

  public boolean getVote() {
    return myVote;
  }

  public void setElected() {
    elected = true;
  }

  public void addAck(NodeID id) {
    if (!myAcks.contains(id)) {
      myAcks.add(id);
    }
  }

  public boolean enoughAcks(int n) {
    return (myAcks.size() >= (n / 2));
  }

}
