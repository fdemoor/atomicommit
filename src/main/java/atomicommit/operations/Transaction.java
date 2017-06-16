package atomicommit.operations;

import atomicommit.node.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.ArrayList;

public class Transaction {

  private final Node node;
  private final int myID;
  private boolean done;
  private final ArrayList<Operation> ops;
  private final Logger logger = LogManager.getLogger();

  public Transaction(int id, Node n) {
    myID = id;
    node = n;
    done = false;
    ops = new ArrayList<Operation>();
  }

  public int getID() {
    return myID;
  }

  public boolean commit() {
    if (!done) {
      done = true;
      logger.debug("Node #{} commits transaction #{}", node.getID(), myID);
      return true;
    }
    return false;
  }

  public boolean abort() {
    if (!done) {
      done = true;
      logger.debug("Node #{} aborts transaction #{}", node.getID(), myID);
      return true;
    }
    return false;
  }

  public boolean propose() {
    Random rand = node.getConfig().getRandom();
    int commitProba = 75;
    int randint = rand.nextInt(100);
    if (randint < commitProba) {
      return true;
    } else {
      return false;
    }
  }

  public void addOp(Operation op) {
    ops.add(op);
  }

}
