package atomicommit.util.node;

public class NodeID {

  private final int id;
  private final String ip;
  private final int type;

  public NodeID(int d, String s, int t) {
    id = d;
    String nb = "" + (6585 + id);
    ip = "tcp://localhost:" + nb;
    type = t;
  }

  public int getID() {
    return id;
  }

  /* TODO: Temporary function, IP will be a parameter in the end */
  public String getIP() {
    return ip;
  }

  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return "" + id;
  }

}
