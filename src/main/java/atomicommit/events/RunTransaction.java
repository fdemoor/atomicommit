package atomicommit.events;

import atomicommit.node.TransactionManager;

public class RunTransaction implements EventHandler {

  private final TransactionManager trMng;

  public RunTransaction(TransactionManager manager) {
    trMng = manager;
  }

  public void handle(Object arg_) {
    int trID = trMng.startTransaction();
    trMng.tryCommit(trID);
  }

}
