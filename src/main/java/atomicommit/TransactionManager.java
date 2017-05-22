package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import javafx.util.Pair;

public class TransactionManager implements ZThread.IDetachedRunnable {

  private final List<NodeID> storageNodes;
  private HashMap<Integer, Transaction> transactions;
  private final NodeID myID;
  private Channel channel;
  private final Logger logger = LogManager.getLogger();
  private int counter;

  TransactionManager(NodeID id, List<NodeID> servers) {

    storageNodes = servers;
    myID = id;
    counter = 0;

    transactions = new HashMap<Integer,Transaction>();

    channel = new Channel();
    channel.setIn(myID);
    Iterator<NodeID> it = storageNodes.iterator();
    while (it.hasNext()) {
      NodeID idNode = it.next();
      channel.addOut(idNode);
    }

  }

  public int startTransaction() {
    int trID = counter++;
    Transaction tr = new Transaction(trID, storageNodes.size());
    logger.debug("[Transaction Manager #{}] Started transaction #{}", myID, trID);
    transactions.put(trID, tr);
    return trID;
  }

  public void tryCommit(int trID) {
    logger.debug("[Transaction Manager #{}] Trying to commit transaction #{}", myID, trID);
    sendToAllStorageNodes(trID, "XACT");
  }

  private void sendToAllStorageNodes(int trID, String message) {
    List<String> messages = new ArrayList<String>(2);
    messages.add("" + trID);
    messages.add(message);
    Iterator<NodeID> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(it.next(), myID, messages);
    }
  }

  private class RunTransaction implements ZLoop.IZLoopHandler {

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      int trID = startTransaction();
      tryCommit(trID);
      return 0;
    }
  }

  private class ManagerMessageHandler implements ZLoop.IZLoopHandler {

    private int handleVote(int trID, NodeID id, boolean vote) {
      Transaction transaction = transactions.get(trID);
      if (transaction.setVote(id, vote)) {
        if (transaction.getDecision()) {
          sendToAllStorageNodes(trID, "COMMIT");
          logger.debug("[Transaction #{}] Decided to commit transaction", trID);
        } else {
          sendToAllStorageNodes(trID, "ABORT");
          logger.debug("[Transaction #{}] Decided to abort transaction", trID);
        }
      }
      return 0;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      Pair<NodeID,List<String>> messagePair = channel.deliver();
      List<String> messages = messagePair.getValue();
      int trID = new Integer(messages.get(0));
      String msg = messages.get(1);
      NodeID src = messagePair.getKey();
      switch(msg) {
        case "YES":
          return handleVote(trID, src, true);
        case "NO":
          return handleVote(trID, src, false);
      }
      return 0;
    }
  }

   @Override
   public void run(Object[] args) {
    ZLoop reactor = new ZLoop();
    ZLoop.IZLoopHandler handler = new ManagerMessageHandler();
    channel.setInEventHandler(reactor, handler, null);

    ZLoop.IZLoopHandler handlerTimer = new RunTransaction();
    channel.setTimerHandler(reactor, handlerTimer, null, 1, 3);

    reactor.start();
    channel.close();
   }



}
