package atomicommit.node;

import java.util.Random;

public class NodeConfig {

  /** Transaction protocols */
  enum TrProtocol {
    TWO_PHASE_COMMIT,
    ZERO_NBAC,
    INBAC
  }

  private final TrProtocol trProtocol; /* Atomic commit protocol used */
  private final int msgDelay; /* Delay in ms after which a network-failure is considered */
  private final int nbTr;
  private final int f; /* Max number of allowed crashes */
  private double probaDelayed = 0;
  private double probaCrashed = 0;
  private final int seed; /* Seed for PNRG */
  private final Random rand;

  NodeConfig(TrProtocol pr, int delay, int tr, int nbCrashes, int s) {
    trProtocol = pr;
    msgDelay = delay;
    nbTr = tr;
    f = nbCrashes;
    seed = s;
    rand = new Random(seed);
  }

  /** Sets crashes and network-failures configuration
   * @param pDelayed  probability for a message to get delayed, 0 to disable
   * @param pCrashed  probability for a node to crash before sending a message, 0 to disable
   */
  public void setCrashFailureConfig(double pDelayed, double pCrashed) {
    probaDelayed = pDelayed;
    probaCrashed = pCrashed;
  }

  public boolean crash() {
    double x = rand.nextDouble();
    if (x < probaCrashed) {
      return true;
    } else {
      return false;
    }
  }

  public boolean networkFailure() {
    double x = rand.nextDouble();
    if (x < probaDelayed) {
      return true;
    } else {
      return false;
    }
  }

  public int getRandomDelay() {
    return rand.nextInt(msgDelay);
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

  public Random getRandom() {
    return rand;
  }

}
