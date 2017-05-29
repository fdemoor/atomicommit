package atomicommit.node;

public class NodeConfig {

  enum TrProtocol {
    TWO_PHASE_COMMIT,
    ZERO_NBAC
  }

  private final TrProtocol trProtocol; /* Atomic commit protocol used */
  private final int msgDelay; /* Delay in ms after which a network-failure is considered */

  NodeConfig(TrProtocol pr) {
    trProtocol = pr;
    msgDelay = 1000;
  }

  TrProtocol getTrProtocol() {
    return trProtocol;
  }

  public int getMsgDelay() {
    return msgDelay;
  }

}
