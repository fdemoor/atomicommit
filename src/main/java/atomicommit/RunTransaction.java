package atomicommit;

public class RunTransaction implements EventHandler {

  private TransactionManager trMng;

  RunTransaction(TransactionManager manager) {
    trMng = manager;
  }

  @Override
  public void handle(Object arg_) {
    int trID = trMng.startTransaction();
    trMng.tryCommit(trID);
  }

}
