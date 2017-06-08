package atomicommit.util.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

/** NodeID wrapper to avoid multiple instances of some node IDs */
public class NodeIDWrapper {

  private final ArrayList<Integer> ids;
  private final HashMap<Integer,NodeID> nodes;
  private boolean sorted;
  private final Logger logger = LogManager.getLogger();

  public NodeIDWrapper() {
    ids = new ArrayList<Integer>();
    nodes = new HashMap<Integer,NodeID>();
    sorted = true;
  }

  public NodeIDWrapper(NodeIDWrapper that) {
    ids = new ArrayList<Integer>(that.ids);
    nodes = new HashMap<Integer,NodeID>(that.nodes);
    sorted = that.sorted;
  }

  private void sort() {
    if (!sorted) {
      Collections.sort(ids);
      sorted = true;
    }
  }

  /** Adds a node ID
   * @param d node int identifier
   * @param ip  node ip
   * @param type  0 if manager, anything else if storage node
   */
  public void add(int d, String ip, int type) {
    NodeID node = new NodeID(d, ip, type);
    nodes.put(d, node);
    if (type != 0) {
      ids.add(d);
    }
    sorted = false;
  }

  /** Returns node ID asked, null if unknown
   * @param d node int identifier
   * @return  node ID, null if unknown
   */
  public NodeID getNodeID(int d) {
    NodeID node = nodes.get(d);
    if (node == null) {
      logger.error("Asking for unknown node #{}", d);
    }
    return node;
  }

  /** Returns f nodes with lowest id
   * @param self  ID of node asking the list
   * @param f number of nodes asked
   * @return  list of f nodes, not containing self
   */
  public List<NodeID> getFNodes(NodeID self, int f) {
    sort();
    List<NodeID> l = new ArrayList<NodeID>();
    int k = 0, s = 0;
    while (k != f) {
      NodeID n = getNodeID(ids.get(s));
      if (!n.equals(self)) {
        l.add(n);
        k++;
      }
      s++;
    }
    return l;
  }

  /** Returns nodes with rank greather than f
   * @param f int
   * @return  list of nodes with rank greather than f
   */
  public List<NodeID> getOtherNodes(int f) {
    sort();
    List<NodeID> l = new ArrayList<NodeID>();
    for (int k = f; k < ids.size(); k++) {
      NodeID n = getNodeID(ids.get(k));
    }
    return l;
  }

  /** Returns rank of node using identifier natural order
   * @param self  ID of node asking its rank
   * @return  rank, -1 if self is unknown
   */
  public int getRank(NodeID self) {
    sort();
    int k = 0;
    while (k < ids.size()) {
      NodeID n = getNodeID(ids.get(k));
      if (n.equals(self)) {
        return k;
      }
      k++;
    }
    return -1;
  }

}
