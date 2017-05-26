package atomicommit;

import java.util.List;
import java.util.ArrayList;

public class TR0NBACInfo implements TRProtocolInfo {

  private boolean myVote;
  private final List<NodeID> myAcks;
  private boolean decided;
  private boolean zero;
  private int phase;

  TR0NBACInfo() {
    myAcks = new ArrayList<NodeID>();
    decided = false;
    zero = false;
    phase = 0;
  }

  void incrPhase() {
    phase++;
  }

  int getPhase() {
    return phase;
  }

  boolean getZero() {
    return zero;
  }

  void setVote(boolean b) {
    myVote = b;
  }

  boolean getVote() {
    return myVote;
  }

  void setDecided() {
    decided = true;
  }

  boolean getDecided() {
    return decided;
  }

  void addAck(NodeID id) {
    if (!myAcks.contains(id)) {
      myAcks.add(id);
    }
  }

  void setZero() {
    zero = true;
  }

  boolean allAcks(int n) {
    return (myAcks.size() == n);
  }

}
