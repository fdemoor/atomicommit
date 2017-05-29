package atomicommit.util.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class NodeIDWrapper {

  private HashMap<Integer,NodeID> nodes;
  private final Logger logger = LogManager.getLogger();

  public NodeIDWrapper() {
    nodes = new HashMap<Integer,NodeID>();
  }

  public void add(int d, String ip) {
    NodeID node = new NodeID(d, ip);
    nodes.put(d, node);
  }

  public NodeID getNodeID(int d) {
    NodeID node = nodes.get(d);
    if (node == null) {
      logger.error("Asking for unknown node #{}", d);
    }
    return node;
  }

}
