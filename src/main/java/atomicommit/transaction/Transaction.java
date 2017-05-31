package atomicommit.transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.ArrayList;

public class Transaction {

  private final int myID;
  private final Logger logger = LogManager.getLogger();

  public Transaction(int n) {
    myID = n;
  }

  public int getID() {
    return myID;
  }

  public void commit() {

  }

  public void abort() {

  }

  public boolean propose() {
    Random rand = new Random();
    int commitProba = 75;
    int randint = rand.nextInt(100);
    if (randint < commitProba) {
      return true;
    } else {
      return false;
    }
  }

}
