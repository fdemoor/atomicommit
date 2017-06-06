package atomicommit.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;
import atomicommit.util.misc.Pair;

import java.util.ArrayList;
import java.util.List;

public class TRINBACInfo implements ProtocolInfo {

  private int phase;
  private boolean proposed;
  private boolean decided;
  private List<Pair<NodeID, Boolean>> collection0;
  private List<Pair<NodeID, List<Pair<NodeID, Boolean>>>> collection1;
  private List<Pair<NodeID, Boolean>> collection_help;
  private boolean wait;
  private boolean val;
  private boolean decision;
  private boolean proposal;
  private int cnt;
  private int cnt_help;
  private final Logger logger = LogManager.getLogger();

  public TRINBACInfo() {
    phase = 0;
    proposed = false;
    decided = false;
    collection0 = new ArrayList<Pair<NodeID, Boolean>>();
    collection1 = new ArrayList<Pair<NodeID,List<Pair<NodeID, Boolean>>>>();
    collection_help = new ArrayList<Pair<NodeID, Boolean>>();
    wait = false;
    cnt = 0;
    cnt_help = 0;
  }

  public void incrPhase() {
    phase++;
  }

  public int getPhase() {
    return phase;
  }

  public void setVal(boolean b) {
    val = b;
  }

  public boolean decided() {
    return decided;
  }

  public boolean proposed() {
    return proposed;
  }

  public void cntHelpIncr() {
    cnt_help++;
  }

  public void cntIncr() {
    cnt++;
  }

  public void addVote0(NodeID node, boolean vote) {
    collection0.add(new Pair<NodeID,Boolean>(node, vote));
  }

  public void addVote1(NodeID src, List<Pair<NodeID,Boolean>> votes) {
    collection1.add(new Pair<NodeID,List<Pair<NodeID,Boolean>>>(src, votes));
  }

  public List<Pair<NodeID, Boolean>> getColl0() {
    return collection0;
  }

}
