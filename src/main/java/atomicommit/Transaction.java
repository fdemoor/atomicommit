package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class Transaction {

  private final int myID;
  private final Logger logger = LogManager.getLogger();

  Transaction(int n) {
    myID = n;
  }

  int getID() {
    return myID;
  }

  void commit() {

  }

  void abort() {
    
  }

}
