package atomicommit.node;

public class NodeConfig {

  enum TrProtocol {
    TWO_PHASE_COMMIT,
    ZERO_NBAC
  }

  private final TrProtocol trProtocol; /* Atomic commit protocol used */
  private final int msgDelay; /* Delay in ms after which a network-failure is considered */
  private final int nbTr;

  NodeConfig(TrProtocol pr, int delay, int tr) {
    trProtocol = pr;
    msgDelay = delay;
    nbTr = tr;
  }

  TrProtocol getTrProtocol() {
    return trProtocol;
  }

  public int getMsgDelay() {
    return msgDelay;
  }

  int getNbTr() {
    return nbTr;
  }

}
