package atomicommit.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.events.EventHandler;
import atomicommit.events.ProtocolInfo;
import atomicommit.events.Consensus;
import atomicommit.operations.Transaction;
import atomicommit.channels.PerfectPointToPointLinks;
import atomicommit.channels.ZMQChannel;

import java.util.HashMap;

public abstract class Node implements Runnable {

  protected NodeConfig config;
  protected NodeID myID;
  protected PerfectPointToPointLinks channel;
  protected NodeIDWrapper nodesWrapper;
  protected final HashMap<Integer, TransactionWrapper> transactions;
  protected final Logger logger = LogManager.getLogger();

  Node(NodeConfig conf, int id, NodeIDWrapper wrapper) {
    config = conf;
    nodesWrapper = wrapper;
    myID = nodesWrapper.getNodeID(id);
    transactions  = new HashMap<Integer,TransactionWrapper>();
    channel = new ZMQChannel(nodesWrapper, this);
  }


  /* TRANSACTION WRAPPER */

  protected class TransactionWrapper {

    protected final Transaction transaction;
    private final ProtocolInfo info;
    private ProtocolInfo consInfo;

    TransactionWrapper(Transaction tr, ProtocolInfo prt) {
      transaction = tr;
      info = prt;
      consInfo = null;
    }

  }

  /** Returns info structure associated to a transaction
   * @param trID  transaction identifier
   * @return  protocol info structure
   */
  public ProtocolInfo getTransactionInfo(int trID) {
    TransactionWrapper wrapper = transactions.get(trID);
    if (wrapper == null) {
      logger.error("No transaction #{} exists", trID);
    }
    return transactions.get(trID).info;
  }

  /** Returns info structure associated to a consensus
   * or create it if not existing yet
   * @param trID  consensus identifier
   * @return  protocol info structure
   */
  public ProtocolInfo getConsensusInfo(int trID) {
    TransactionWrapper wrapper = transactions.get(trID);
    ProtocolInfo info = wrapper.consInfo;
    if (info == null) {
      info = new Consensus(wrapper.info.getNbInvolvedNodes());
      wrapper.consInfo = info;
    }
    return info;
  }


  /* UTIL METHODS */

  /** Returns the configuration
   * @return    the node configuration
   */
  public NodeConfig getConfig() {
    return config;
  }

  /** Returns the node ID
   * @return    the node ID
   */
  public NodeID getID() {
    return myID;
  }

  /** Returns node ID wrapper
   * @return  node id wrapper
   */
  public NodeIDWrapper getIDWrapper() {
    return nodesWrapper;
  }

  /** Stops node from running */
  public void finish() {
    logger.warn("Node #{} stops", myID);
    channel.close();
  }


  /* CHANNEl METHODS */

  /** Sends a message
   * @param id  identifier
   * @param type  type of the message
   * @param dest  node ID of the destination
   */
  public void sendToNode(int id, MessageType type, NodeID dest) {
    Message message = new Message(myID, id, type);
    channel.send(dest, message);
  }

  /** Returns message that triggered a polling event
   * @return    message
   */
  public Message deliverMessage() {
    return channel.deliver();
  };

  /** Sets timeout event
   * @param handler an event handler which will be run at timeout
   * @param delay delay before the trigger of the timeout
   * @param times number of times the event must be triggered
   * @param arg_  object argument for the handler
   */
  public void setTimeoutEvent(EventHandler handler, int delay, int times, Object arg_) {
    channel.setTimeoutEventHandler(handler, delay, times, arg_);
  }

  /** Removes timeout events */
  public void removeTimeoutEvent() {
    channel.removeTimeoutEvent();
  }

  /** Sends a message to all storage nodes
   * @param id  identifier
   * @param type  type of the message
   */
  abstract public void sendToAllStorageNodes(int id, MessageType type);

  /** Sends a message to all storage nodes
   * @param id  identifier
   * @param type  type of the message
   * @param k key of the message
   */
  abstract public void sendToAllStorageNodes(int id, MessageType type, int k);


  /* TRANSACTION METHODS */

  /** Commits transaction
   * @param trID  transaction ID
   */
  abstract public void commitTransaction(int trID);

  /** Aborts transaction
   * @param trID  transaction ID
   */
  abstract public void abortTransaction(int trID);


  /* MAIN */

  @Override
  public void run() {
    try {
      channel.startPolling();
      channel.close();
    } catch (RuntimeException ex) {
      logger.warn("Run " + ex.getMessage());
    }
  }

}
