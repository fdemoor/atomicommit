package atomicommit.node;

public class NodeConfig {

  /** Transaction protocols */
  enum TrProtocol {
    TWO_PHASE_COMMIT,
    ZERO_NBAC
  }

  private final TrProtocol trProtocol; /* Atomic commit protocol used */
  private final int msgDelay; /* Delay in ms after which a network-failure is considered */
  private final int nbTr;
  private final int f; /* Max number of allowed crashes */

  NodeConfig(TrProtocol pr, int delay, int tr, int nbCrashes) {
    trProtocol = pr;
    msgDelay = delay;
    nbTr = tr;
    f = nbCrashes;
  }

  /** Returns transaction protocol used
   * @return transaction protocol
   */
  TrProtocol getTrProtocol() {
    return trProtocol;
  }

  /** Returns message delay used
   * @return  message delay in ms
   */
  public int getMsgDelay() {
    return msgDelay;
  }

  int getNbTr() {
    return nbTr;
  }

  public int getF() {
    return f;
  }

}
