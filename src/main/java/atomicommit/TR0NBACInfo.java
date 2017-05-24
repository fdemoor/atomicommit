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


}
