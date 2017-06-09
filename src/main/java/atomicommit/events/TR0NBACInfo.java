package atomicommit.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;

import java.util.List;
import java.util.ArrayList;

public class TR0NBACInfo extends ProtocolInfo {

  private boolean myVote;
  private final List<NodeID> myAcks;
  private boolean decided;
  private boolean zero;
  private int phase;
  private final Logger logger = LogManager.getLogger();

  public TR0NBACInfo(int n) {
    super(n);
    myAcks = new ArrayList<NodeID>();
    decided = false;
    zero = false;
    phase = 0;
  }

  public void incrPhase() {
    phase++;
  }

  public int getPhase() {
    return phase;
  }

  public boolean getZero() {
    return zero;
  }

  public void setVote(boolean b) {
    myVote = b;
  }

  public boolean getVote() {
    return myVote;
  }

  public void setDecided() {
    decided = true;
  }

  public boolean getDecided() {
    return decided;
  }

  public void addAck(NodeID id) {
    if (!myAcks.contains(id)) {
      myAcks.add(id);
    }
  }

  public void setZero() {
    zero = true;
  }

  public boolean allAcks(int n) {
    return (myAcks.size() == n);
  }

}
