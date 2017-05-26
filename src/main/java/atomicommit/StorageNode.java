package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.zeromq.ZThread;

public class StorageNode extends Node implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID trManager;
  private final List<Integer> nodes;
  private final HashMap<Integer, TransactionWrapper> transactions;
  private final NodeIDWrapper nodesWrapper;
  private final PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(int id, int manager, List<Integer> nodesList) {

    nodesWrapper = new NodeIDWrapper(id);
    myID = nodesWrapper.getNodeID(id);
    nodes = nodesList;
    trManager = nodesWrapper.getNodeID(manager);
    transactions = new HashMap<Integer, TransactionWrapper>();

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    channel.addOut(trManager);
    Iterator<Integer> it = nodes.iterator();
    while (it.hasNext()) {
      NodeID nodeID = nodesWrapper.getNodeID(it.next());
      if (!nodeID.equals(myID)) {
        channel.addOut(nodeID);
      }
    }
  }

  private class TransactionWrapper {

    private final Transaction transaction;
    private final TRProtocolInfo info;
    private final int nbInvolvedNodes;

    TransactionWrapper(Transaction tr, TRProtocolInfo prt, int n) {
      transaction = tr;
      info = prt;
      nbInvolvedNodes = n;
    }

  }

  void setTimeoutEvent(EventHandler handler, int delay, int times, Object arg_) {
    channel.setTimeoutEventHandler(handler, delay, times, arg_);
  }

  void startTransaction(int trID) {
    logger.debug("Storage Node #{} starts transaction #{}", myID, trID);
    Transaction tr = new Transaction(trID);
    TRProtocolInfo info = new TR0NBACInfo();
    transactions.put(trID, new TransactionWrapper(tr, info, nodes.size() -1));
  }

  TRProtocolInfo getTransactionInfo(int trID) {
    return transactions.get(trID).info;
  }

  void commitTransaction(int trID) {
    transactions.get(trID).transaction.commit();
    logger.debug("Storage Node #{} commits transaction #{}", myID, trID);
  }

  void abortTransaction(int trID) {
    transactions.get(trID).transaction.abort();
    logger.debug("Storage Node #{} aborts transaction #{}", myID, trID);
  }

  void sendToManager(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    channel.send(trManager, message);
  }

  void sendToNode(int id, MessageType type, NodeID dest) {
    if (!dest.equals(myID)) {
      Message message = new Message(myID, id, type);
      channel.send(dest, message);
    }
  }

  void sendToAllStorageNodes(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    Iterator<Integer> it = nodes.iterator();
    while (it.hasNext()) {
      NodeID nodeID = nodesWrapper.getNodeID(it.next());
      if (!nodeID.equals(myID)) {
        channel.send(nodeID, message);
      }
    }
  }

  Message deliverMessage() {
    return channel.deliver();
  }

  int getTransanctionNbNodes(int trID) {
    return transactions.get(trID).nbInvolvedNodes;
  }

  boolean checkManager(NodeID id) {
    boolean test = trManager.equals(id);
    if (!test) {
      logger.warn("Storage Node #{} - Message not coming from manager #{} but from #{}", trManager, id);
    }
    return test;
  }

  @Override
  public void run(Object[] args) {

    MessageHandler msgHandler = new MessageHandler(this);
    //EventHandler handler = new MsgHandler2PCSlave(this);
    EventHandler handler = new MsgHandler0NBACSlave(this);
    msgHandler.setTransactionHandler(handler);
    channel.setMessageEventHandler(msgHandler);

    channel.startPolling();
    channel.close();
  }



}
