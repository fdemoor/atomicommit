package atomicommit.transaction;

import atomicommit.node.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.ArrayList;

public class Transaction {

  private final Node node;
  private final int myID;
  private final Logger logger = LogManager.getLogger();

  public Transaction(int id, Node n) {
    myID = id;
    node = n;
  }

  public int getID() {
    return myID;
  }

  public void commit() {

  }

  public void abort() {

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

}
