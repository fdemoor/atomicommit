package atomicommit;

import java.util.HashMap;

public class NodeIDWrapper {

  private final NodeID owner;
  private HashMap<Integer,NodeID> nodes;

  NodeIDWrapper(int d) {
    owner = new NodeID(d);
    nodes = new HashMap<Integer,NodeID>();
    nodes.put(d, owner);
  }

  NodeID getNodeID(int d) {
    NodeID node = nodes.get(d);
    if (node == null) {
      node = new NodeID(d);
      nodes.put(d, node);
    }
    return node;
  }

}
