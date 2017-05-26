package atomicommit.node;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.events.MsgHandler0NBACMaster;
import atomicommit.events.RunTransaction;
import atomicommit.util.misc.Counter;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.util.msg.MessageType;
import atomicommit.util.msg.Message;
import atomicommit.transaction.Transaction;
import atomicommit.channels.PerfectPointToPointLinks;
import atomicommit.channels.ZMQChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.zeromq.ZThread;

public class TransactionManager extends Node implements ZThread.IDetachedRunnable {

  private final List<Integer> storageNodes;
  private final HashMap<Integer, TransactionWrapper> transactions;
  private final NodeID myID;
  private final NodeIDWrapper nodesWrapper;
  private final PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();
  private final Counter transactionIDs;

  private class TransactionWrapper {

    private final Transaction transaction;
    private final int nbInvolvedNodes;
    private final ArrayList<NodeID> hasProposed;
    private boolean decision;
    private boolean hasDecided;
    private boolean done;

    TransactionWrapper(Transaction tr, int n) {
      transaction = tr;
      nbInvolvedNodes = n;
      hasProposed = new ArrayList();
      hasDecided = false;
      decision = true;
      done = false;
    }

    boolean setVote(NodeID id, boolean vote) {

      if (!hasProposed.contains(id)) {
        hasProposed.add(id);
        logger.debug("Received {} from Storage Node #{} for transaction #{}", vote, id, transaction.getID());
      }
      if (!vote) {
        decision = false;
      }
      if (hasProposed.size() == nbInvolvedNodes) {
        hasDecided = true;
      }
      return hasDecided;
    }

    boolean getDecision() {
      if (hasDecided) {
        return decision;
      } else {
        logger.warn("Trying to obtain decision value from transaction #{} while not yet determined", myID);
        return false;
      }
    }

    void setDone() {
      done = true;
    }

    boolean isDone() {
      return done;
    }

  }

  TransactionManager(int id, List<Integer> servers) {

    nodesWrapper = new NodeIDWrapper(id);
    storageNodes = servers;
    myID = nodesWrapper.getNodeID(id);
    transactionIDs = new Counter();

    transactions = new HashMap<Integer,TransactionWrapper>();

    channel = new ZMQChannel(nodesWrapper);
    channel.setIn(myID);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      NodeID idNode = nodesWrapper.getNodeID(it.next());
      channel.addOut(idNode);
    }

  }

  public int startTransaction() {
    int trID = transactionIDs.get();
    transactionIDs.incr();
    Transaction tr = new Transaction(trID);
    sendToAllStorageNodes(trID, MessageType.TR_START);
    logger.debug("Transaction Manager #{} starts transaction #{}", myID, trID);
    transactions.put(trID, new TransactionWrapper(tr, storageNodes.size()));
    return trID;
  }

  public void tryCommit(int trID) {
    logger.debug("Transaction Manager #{} tries to commit transaction #{}", myID, trID);
    sendToAllStorageNodes(trID, MessageType.TR_XACT);
  }

  public void commitTransaction(int trID) {
    if (!transactions.get(trID).isDone()) {
      transactions.get(trID).setDone();
      logger.debug("Transaction Manager #{} commits transaction #{}", myID, trID);
      Transaction tr = transactions.get(trID).transaction;
      tr.commit();
    }
  }

  public void abortTransaction(int trID) {
    if (!transactions.get(trID).isDone()) {
      transactions.get(trID).setDone();
      logger.debug("Transaction Manager #{} aborts transaction #{}", myID, trID);
      Transaction tr = transactions.get(trID).transaction;
      tr.abort();
    }
  }

  public boolean setTransactionVote(int trID, NodeID id, boolean vote) {
    return transactions.get(trID).setVote(id, vote);
  }

  public boolean getTransactionDecision(int trID) {
    return transactions.get(trID).getDecision();
  }

  public void sendToAllStorageNodes(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    Iterator<Integer> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(nodesWrapper.getNodeID(it.next()), message);
    }
  }

  public Message deliverMessage() {
    return channel.deliver();
  }

  @Override
  public void run(Object[] args) {

    MessageHandler msgHandler = new MessageHandler(this);
    //EventHandler handler = new MsgHandler2PCMaster(this);
    EventHandler handler = new MsgHandler0NBACMaster(this);
    msgHandler.setTransactionHandler(handler);
    channel.setMessageEventHandler(msgHandler);


    EventHandler handlerTimer = new RunTransaction(this);
    channel.setTimeoutEventHandler(handlerTimer, 1, 1, null);

    channel.startPolling();
    channel.close();
  }



}
