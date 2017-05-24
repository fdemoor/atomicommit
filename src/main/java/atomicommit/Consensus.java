package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Consensus {

  private final int myID;
  private final int nbInvolvedNodes;
  private int decision;
  private boolean hasDecided;
  private final Logger logger = LogManager.getLogger();

  Consensus(int n, int d) {
    myID = n;
    nbInvolvedNodes = d;
    hasDecided = false;
    decision = -1;
  }

  int getDecision() {
    if (hasDecided) {
      return decision;
    } else {
      logger.warn("Trying to obtain decision value from consensus #{} while not yet determined", myID);
      return -1;
    }
  }

  void commit() {
    logger.debug("Consensus #{} commited", myID);
  }

  void abort() {
    logger.debug("Consensus #{} aborted", myID);
  }

}
