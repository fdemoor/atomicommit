package atomicommit.util.node;

public class NodeID {

  private final int id;
  private final String ip;

  public NodeID(int d, String s) {
    id = d;
    String nb = "" + (6585 + id);
    ip = "tcp://localhost:" + nb;
  }

  public int getID() {
    return id;
  }

  /* TODO: Temporary function, IP will be a parameter in the end */
  public String getIP() {
    return ip;
  }

  @Override
  public String toString() {
    return "" + id;
  }

}
