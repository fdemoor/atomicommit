package atomicommit;

public class NodeID {

  private final int id;
  private final String ip;

  NodeID(int d) {
    id = d;
    String nb = "" + (6585 + id);
    ip = "tcp://localhost:" + nb;
  }

  int getID() {
    return id;
  }

  /* TODO: Temporary function, IP will be a parameter in the end */
  String getIP() {
    return ip;
  }

  @Override
  public String toString() {
    return "" + id;
  }

}
