package atomicommit.events;

/** Info storage interface for protocols */
public abstract class ProtocolInfo {

  protected final int nbInvolvedNodes;

  ProtocolInfo(int n) {
    nbInvolvedNodes = n;
  }

  public int getNbInvolvedNodes() {
    return nbInvolvedNodes;
  }

}
