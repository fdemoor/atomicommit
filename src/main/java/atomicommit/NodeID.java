package atomicommit;

public class NodeID {

  private final int ID;
  private final String IP;

  NodeID(int d) {
    ID = d;
    String nb = "" + (6585 + ID);
    IP = "tcp://localhost:" + nb;
  }

  public int getID() {
    return ID;
  }

  public String getIP() {
    return IP;
  }

  @Override
  public boolean equals(Object other){
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof NodeID)) return false;
    NodeID otherNodeID = (NodeID) other;
    if (otherNodeID.getID() == ID) {
      return true;
    }
    return false;
  }

  public String toString() {
    return "" + ID;
  }

}
