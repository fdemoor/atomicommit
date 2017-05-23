package atomicommit.eventhandler;

import atomicommit.*;

public class RunTransaction implements EventHandler {

  private TransactionManager trMng;

  public RunTransaction(TransactionManager manager) {
    trMng = manager;
  }

  @Override
  public void handle(Object arg_) {
    int trID = trMng.startTransaction();
    trMng.tryCommit(trID);
  }

}
