package atomicommit.node;

import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.node.NodeID;
import atomicommit.events.EventHandler;
import atomicommit.channels.PerfectPointToPointLinks;

public abstract class Node {

  protected NodeConfig config;
  protected NodeID myID;
  protected PerfectPointToPointLinks channel;


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

  /** Stops node from running */
  public void finish() {
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

}
