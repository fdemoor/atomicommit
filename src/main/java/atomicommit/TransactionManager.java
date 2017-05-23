package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import org.zeromq.ZThread;

public class TransactionManager implements ZThread.IDetachedRunnable {

  private final List<NodeID> storageNodes;
  private HashMap<Integer, Transaction> transactions;
  private final NodeID myID;
  private PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();
  private int counter;

  TransactionManager(NodeID id, List<NodeID> servers) {

    storageNodes = servers;
    myID = id;
    counter = 0;

    transactions = new HashMap<Integer,Transaction>();

    channel = new ZMQChannel();
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
    Message message = new Message(myID, trID, MessageType.TR_XACT);
    sendToAllStorageNodes(message);
  }

  public void sendToAllStorageNodes(Message message) {
    Iterator<NodeID> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(it.next(), message);
    }
  }

  public Transaction getTransaction(int trID) {
    return transactions.get(trID);
  }

  public NodeID getID() {
    return myID;
  }

  @Override
  public void run(Object[] args) {

    EventHandler handler = new MessageHandler2PCMaster(channel, this);
    channel.setMessageEventHandler(handler);

    EventHandler handlerTimer = new RunTransaction(this);
    channel.setTimeoutEventHandler(handlerTimer, 1, 3);

    channel.startPolling();
    channel.close();
  }



}
