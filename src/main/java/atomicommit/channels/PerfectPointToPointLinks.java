package atomicommit.channels;

import atomicommit.util.msg.Message;
import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.util.node.NodeID;

/** Channel interface for message passing between processes */
public interface PerfectPointToPointLinks {

  /** Add a connecting socket to this node
   * @param id  ID of connecting node
   */
  void addOut(NodeID id);

  /** Send a message
   * @param dest  ID of destination node
   * @param message message to send
   */
  void send(NodeID dest, Message message);;

  /**  Retrieve an incoming message
   * @return message
    */
  Message deliver();

  /** Set the handler called when a message is received
   * @param handler message handler
   */
  void setMessageEventHandler(MessageHandler handler);

  /** Set the handler called when a timeout is received
   * @param handler timeout handler
   * @param delay delay before timeout
   * @param times number of times timeout is repeated
   * @param arg_  handler argument
   */
  void setTimeoutEventHandler(EventHandler handler, int delay, int times, Object arg_);

  /** Remove timeout event */
  void removeTimeoutEvent();

  /** Start the message or timer polling */
  void startPolling();

  /** Close all sockets */
  void close();

}
