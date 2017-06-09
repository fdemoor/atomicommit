package atomicommit.node;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.events.MsgHandler2PCSlave;
import atomicommit.events.MsgHandler3PCSlave;
import atomicommit.events.MsgHandler0NBACSlave;
import atomicommit.events.MsgHandlerINBACSlave;
import atomicommit.events.RaftLeaderElection;
import atomicommit.events.ProtocolInfo;
import atomicommit.events.TR0NBACInfo;
import atomicommit.events.TRINBACInfo;
import atomicommit.events.Consensus;
import atomicommit.util.msg.MessageType;
import atomicommit.util.msg.Message;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.util.misc.Pair;
import atomicommit.channels.ZMQChannel;
import atomicommit.transaction.Transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Iterator;
import java.util.Set;

public class StorageNode extends Node {

  private final NodeID trManager;
  private final List<Integer> nodes;

  StorageNode(NodeConfig conf, int id, int manager, List<Integer> nodesList, NodeIDWrapper wrapper) {

    super(conf, id, wrapper);

    nodes = nodesList;
    trManager = nodesWrapper.getNodeID(manager);

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
      case ZERO_NBAC:
        msgHandler.setTransactionHandler(new MsgHandler0NBACSlave(this));
        break;
      case INBAC:
        msgHandler.setTransactionHandler(new MsgHandlerINBACSlave(this));
        break;
      case THREE_PHASE_COMMIT:
        msgHandler.setTransactionHandler(new MsgHandler3PCSlave(this));
        break;
      case TWO_PHASE_COMMIT:
      default:
        msgHandler.setTransactionHandler(new MsgHandler2PCSlave(this));
        break;
    }
    msgHandler.setConsensusHandler(new RaftLeaderElection(this));
    channel.setMessageEventHandler(msgHandler);

  }


  /* TRANSACTION METHODS */

  public void startTransaction(int trID) {
    logger.debug("Storage Node #{} starts transaction #{}", myID, trID);
    Transaction tr = new Transaction(trID, this);
    ProtocolInfo info;
    switch (config.getTrProtocol()) {
      case ZERO_NBAC:
        info = new TR0NBACInfo(nodes.size() -1);
        break;
      case INBAC:
        info = new TRINBACInfo(nodes.size() -1);
        break;
      case TWO_PHASE_COMMIT:
      default:
        info = null;
        break;
    }
    transactions.put(trID, new TransactionWrapper(tr, info));
  }

  public boolean getTransanctionProposition(int trID) {
    Transaction tr = transactions.get(trID).transaction;
    return tr.propose();
  }

  @Override
  public void commitTransaction(int trID) {
    transactions.get(trID).transaction.commit();
  }

  @Override
  public void abortTransaction(int trID) {
    transactions.get(trID).transaction.abort();
  }


  /* CHANNEL METHODS */

  public void sendToManager(int id, MessageType type) {
    Message message = new Message(myID, id, type);
    channel.send(trManager, message);
  }

  @Override
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

  @Override
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

  public void sendToAllStorageNodes(int id, MessageType type, Set<Pair<NodeID, Boolean>> l) {
    Message message = new Message(myID, id, type, l);
    Iterator<Integer> it = nodes.iterator();
    while (it.hasNext()) {
      NodeID nodeID = nodesWrapper.getNodeID(it.next());
      if (!nodeID.equals(myID)) {
        channel.send(nodeID, message);
      }
    }
  }

  public void sendToNode(int id, MessageType type, NodeID dest, Set<Pair<NodeID, Boolean>> l) {
    Message message = new Message(myID, id, type, l);
    channel.send(dest, message);
  }

  /* UTIL METHODS */

  public boolean checkManager(NodeID id) {
    boolean test = trManager.equals(id);
    if (!test) {
      logger.warn("Storage Node #{} - Message not coming from manager #{} but from #{}", myID, trManager, id);
    }
    return test;
  }

}
