package atomicommit.util.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/** NodeID wrapper to avoid multiple instances of some node IDs */
public class NodeIDWrapper {

  private HashMap<Integer,NodeID> nodes;
  private final Logger logger = LogManager.getLogger();

  public NodeIDWrapper() {
    nodes = new HashMap<Integer,NodeID>();
  }

  /** Adds a node ID
   * @param d node int identifier
   * @param ip  node ip
   */
  public void add(int d, String ip) {
    NodeID node = new NodeID(d, ip);
    nodes.put(d, node);
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

}
