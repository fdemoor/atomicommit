package atomicommit.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.node.NodeID;
import atomicommit.util.misc.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

public class TRINBACInfo extends ProtocolInfo {

  private int phase;
  private boolean proposed;
  private boolean decided;
  private Set<Pair<NodeID, Boolean>> collection0;
  private Set<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> collection1;
  private Set<Pair<NodeID, Boolean>> collectionHelp;
  private boolean wait;
  private boolean val;
  private boolean decision;
  private boolean proposal;
  private int cnt;
  private int cntHelp;
  private final Logger logger = LogManager.getLogger();

  public TRINBACInfo(int n) {
    super(n);
    phase = 0;
    proposed = false;
    decided = false;
    collection0 = new HashSet<Pair<NodeID, Boolean>>();
    collection1 = new HashSet<Pair<NodeID,Set<Pair<NodeID, Boolean>>>>();
    collectionHelp = new HashSet<Pair<NodeID, Boolean>>();
    wait = false;
    cnt = 0;
    cntHelp = 0;
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

  public boolean getVal() {
    return val;
  }

  public boolean decided() {
    return decided;
  }

  public void decide(boolean b) {
    decision = b;
    decided = true;
  }

  public void propose(boolean b) {
    proposal = b;
    proposed = true;
  }

  public boolean proposed() {
    return proposed;
  }

  public void cntHelpIncr() {
    cntHelp++;
  }

  public void cntIncr() {
    cnt++;
  }

  public void setWait(boolean b) {
    wait = b;
  }

  public boolean isWaiting() {
    return wait;
  }

  public int cntGet() {
    return cnt;
  }

  public int cntHelpGet() {
    return cntHelp;
  }

  public void addVote0(NodeID node, boolean vote) {
    collection0.add(new Pair<NodeID,Boolean>(node, vote));
  }

  public void addVote0(Set<Pair<NodeID,Boolean>> votes) {
    collection0.addAll(votes);
  }

  public void addVote1(NodeID src, Set<Pair<NodeID,Boolean>> votes) {
    collection1.add(new Pair<NodeID,Set<Pair<NodeID,Boolean>>>(src, votes));
  }

  public void addVoteHelp(Set<Pair<NodeID,Boolean>> votes) {
    collectionHelp.addAll(votes);
  }

  public Set<Pair<NodeID,Boolean>> getVote0() {
    return collection0;
  }

  public Set<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> getVote1() {
    return collection1;
  }

  public boolean checkAllVote1(int n, int f) {
    if (collection1.size() != f) {
      return false;
    } else {
      Iterator<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> it = collection1.iterator();
      while (it.hasNext()) {
        if (it.next().getSecond().size() != n+1) {
          return false;
        }
      }
      return true;
    }
  }

  public boolean checkBackUpsVote1(int n, int f) {
    if (collection1.size() != f+1) {
      return false;
    } else {
      Iterator<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> it = collection1.iterator();
      boolean foundF = false;
      while (it.hasNext()) {
        int size = it.next().getSecond().size();
        if (size == f) {
          if (foundF) {
            return false;
          } else {
            foundF = true;
          }
        } else if (size != n+1) {
          return false;
        }
      }
      return true;
    }
  }

  public boolean getAnd1(int n) {
    Iterator<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> it = collection1.iterator();
    Set<Pair<NodeID, Boolean>> votes;
    do {
      votes = it.next().getSecond();
    } while (votes.size() != n+1);
    Iterator<Pair<NodeID, Boolean>> it2 = votes.iterator();
    while (it2.hasNext()) {
      if (!it2.next().getSecond()) {
        return false;
      }
    }
    return true;
  }

  public Pair<Boolean,Boolean> checkExistVote1(int n) {
    Set<Pair<NodeID, Boolean>> values = new HashSet<Pair<NodeID, Boolean>>();
    boolean and = true;
    Iterator<Pair<NodeID, Set<Pair<NodeID, Boolean>>>> it = collection1.iterator();
    while (it.hasNext()) {
      Set<Pair<NodeID, Boolean>> votes = it.next().getSecond();
      Iterator<Pair<NodeID, Boolean>> it2 = votes.iterator();
      while (it2.hasNext()) {
        Pair<NodeID, Boolean> vote = it2.next();
        values.add(vote);
        if (!vote.getSecond()) {
          and = false;
        }
      }
    }
    boolean b = (values.size() == n+1);
    return new Pair<Boolean,Boolean>(b, and);
  }

  public boolean checkHelp(int n) {
    return (collectionHelp.size() == n+1);
  }

  public boolean getAndHelp() {
    Iterator<Pair<NodeID,Boolean>> it = collectionHelp.iterator();
    while (it.hasNext()) {
      if (!it.next().getSecond()) {
        return false;
      }
    }
    return true;
  }

  public boolean checkVotes0(int n) {
    return collection0.size() == n;
  }

}
