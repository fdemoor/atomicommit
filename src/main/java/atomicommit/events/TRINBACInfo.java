package atomicommit.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class TRINBACInfo implements ProtocolInfo {

  private int phase;
  private boolean proposed;
  private boolean decided;
  private HashMap<NodeID,Boolean> collection0;
  private HashMap<NodeID,Boolean> collection1;
  private HashMap<NodeID,Boolean> collection_help;
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
    collection0 = new HashMap<NodeID,Boolean>();
    collection1 = new HashMap<NodeID,Boolean>();
    collection_help = new HashMap<NodeID,Boolean>();
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

}
