package atomicommit.node;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.events.MsgHandler2PCSlave;
import atomicommit.events.MsgHandler0NBACSlave;
import atomicommit.events.RaftLeaderElection;
import atomicommit.events.ProtocolInfo;
import atomicommit.events.TR0NBACInfo;
import atomicommit.events.Consensus;
import atomicommit.util.msg.MessageType;
import atomicommit.util.msg.Message;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.channels.PerfectPointToPointLinks;
import atomicommit.channels.ZMQChannel;
import atomicommit.transaction.Transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.zeromq.ZThread;

public class StorageNode extends Node implements ZThread.IDetachedRunnable {

  private final NodeID trManager;
  private final List<Integer> nodes;
  private final HashMap<Integer, TransactionWrapper> transactions;
  private final NodeIDWrapper nodesWrapper;
  private final PerfectPointToPointLinks channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(NodeConfig conf, int id, int manager, List<Integer> nodesList, NodeIDWrapper wrapper) {

    config = conf;
    nodesWrapper = wrapper;
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
    MessageHandler msgHandler = new MessageHandler(this);
    switch (config.getTrProtocol()) {
      case TWO_PHASE_COMMIT:
        msgHandler.setTransactionHandler(new MsgHandler2PCSlave(this));
        break;
      case ZERO_NBAC:
        msgHandler.setTransactionHandler(new MsgHandler0NBACSlave(this));
        break;
    }
    msgHandler.setConsensusHandler(new RaftLeaderElection(this));
    channel.setMessageEventHandler(msgHandler);

  }

  private class TransactionWrapper {

    private final Transaction transaction;
    private final ProtocolInfo info;
    private final int nbInvolvedNodes;
    private ProtocolInfo consInfo;

    TransactionWrapper(Transaction tr, ProtocolInfo prt, int n) {
      transaction = tr;
      info = prt;
      nbInvolvedNodes = n;
      consInfo = null;
    }

  }

  public void setTimeoutEvent(EventHandler handler, int delay, int times, Object arg_) {
    channel.setTimeoutEventHandler(handler, delay, times, arg_);
  }

  public void removeTimeoutEvent() {
    channel.removeTimeoutEvent();
  }
  
  public void startTransaction(int trID) {
    logger.debug("Storage Node #{} starts transaction #{}", myID, trID);
    Transaction tr = new Transaction(trID);
    ProtocolInfo info = null;
    switch (config.getTrProtocol()) {
      case ZERO_NBAC:
        info = new TR0NBACInfo();
        break;
    }
    transactions.put(trID, new TransactionWrapper(tr, info, nodes.size() -1));
  }

  public ProtocolInfo getTransactionInfo(int trID) {
    return transactions.get(trID).info;
  }

  public ProtocolInfo getConsensusInfo(int trID) {
    ProtocolInfo info = transactions.get(trID).consInfo;
    if (info == null) {
      info = new Consensus();
      transactions.get(trID).consInfo = info;
    }
    return info;
  }

  public void commitTransaction(int trID) {
    transactions.get(trID).transaction.commit();
    logger.debug("Storage Node #{} commits transaction #{}", myID, trID);
  }

  public void abortTransaction(int trID) {
    transactions.get(trID).transaction.abort();
    logger.debug("Storage Node #{} aborts transaction #{}", myID, trID);
  }

  public void sendToManager(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    channel.send(trManager, message);
  }

  public void sendToNode(int id, MessageType type, NodeID dest) {
    Message message = new Message(myID, id, type);
    channel.send(dest, message);
  }

  public void sendToAllStorageNodes(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    Iterator<Integer> it = nodes.iterator();
    while (it.hasNext()) {
      NodeID nodeID = nodesWrapper.getNodeID(it.next());
      if (!nodeID.equals(myID)) {
        channel.send(nodeID, message);
      }
    }
  }

  public void sendToAllStorageNodes(int id, MessageType type, int k) {
    Message message = new Message(myID, id, type, k);
    Iterator<Integer> it = nodes.iterator();
    while (it.hasNext()) {
      NodeID nodeID = nodesWrapper.getNodeID(it.next());
      if (!nodeID.equals(myID)) {
        channel.send(nodeID, message);
      }
    }
  }

  public Message deliverMessage() {
    return channel.deliver();
  }

  public int getTransanctionNbNodes(int trID) {
    return transactions.get(trID).nbInvolvedNodes;
  }

  public boolean checkManager(NodeID id) {
    boolean test = trManager.equals(id);
    if (!test) {
      logger.warn("Storage Node #{} - Message not coming from manager #{} but from #{}", myID, trManager, id);
    }
    return test;
  }

  @Override
  public void run(Object[] args) {
    channel.startPolling();
    channel.close();
  }



}
