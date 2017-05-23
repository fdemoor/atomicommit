package atomicommit;

public class NodeID {

  private final int id;
  private final String ip;

  NodeID(int d) {
    id = d;
    String nb = "" + (6585 + id);
    ip = "tcp://localhost:" + nb;
  }

  public int getID() {
    return id;
  }

  public String getIP() {
    return ip;
  }

  @Override
  public boolean equals(Object other){
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof NodeID)) return false;
    NodeID otherNodeID = (NodeID) other;
    if (otherNodeID.getID() == id) {
      return true;
    }
    return false;
  }

  public String toString() {
    return "" + id;
  }

}
