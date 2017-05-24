package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import org.zeromq.ZThread;

public class TransactionManager implements ZThread.IDetachedRunnable {

  private final List<Integer> storageNodes;
  private HashMap<Integer, Transaction> transactions;
  private final NodeID myID;
  private final NodeIDWrapper nodesWrapper;
  private PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();
  private Counter transactionIDs;

  TransactionManager(int id, List<Integer> servers) {

    nodesWrapper = new NodeIDWrapper(id);
    storageNodes = servers;
    myID = nodesWrapper.getNodeID(id);
    transactionIDs = new Counter();

    transactions = new HashMap<Integer,Transaction>();

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      NodeID idNode = nodesWrapper.getNodeID(it.next());
      channel.addOut(idNode);
    }

  }

  int startTransaction() {
    int trID = transactionIDs.get();
    transactionIDs.incr();
    Transaction tr = new Transaction(trID, storageNodes.size());
    logger.debug("Transaction Manager #{} starts transaction #{}", myID, trID);
    transactions.put(trID, tr);
    return trID;
  }

  void tryCommit(int trID) {
    logger.debug("Transaction Manager #{} tries to commit transaction #{}", myID, trID);
    sendToAllStorageNodes(trID, MessageType.TR_XACT);
  }

  void commitTransaction(int trID) {
    logger.debug("Transaction Manager #{} commits transaction #{}", myID, trID);
  }

  void abortTransaction(int trID) {
    logger.debug("Transaction Manager #{} aborts transaction #{}", myID, trID);
  }

  void sendToAllStorageNodes(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(nodesWrapper.getNodeID(it.next()), message);
    }
  }

  Message deliverMessage() {
    return channel.deliver();
  }

  Transaction getTransaction(int trID) {
    return transactions.get(trID);
  }

  @Override
  public void run(Object[] args) {

    EventHandler handler = new MessageHandler2PCMaster(this);
    channel.setMessageEventHandler(handler);

    EventHandler handlerTimer = new RunTransaction(this);
    channel.setTimeoutEventHandler(handlerTimer, 1, 3);

    channel.startPolling();
    channel.close();
  }



}
