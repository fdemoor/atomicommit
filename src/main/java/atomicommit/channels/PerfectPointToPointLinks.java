package atomicommit.channels;

import atomicommit.util.msg.Message;
import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.util.node.NodeID;

public interface PerfectPointToPointLinks {

  /** Bind a socket for this node */
  void setIn(NodeID id);

  /** Add a connecting socket to this node */
  void addOut(NodeID id);

  /** Send a message */
  void send(NodeID dest, Message message);;

  /**  Retrieve an incoming message */
  Message deliver();

  /** Set the handler called when a message is received */
  void setMessageEventHandler(MessageHandler handler);

  /** Set the handler called when a timeout is received */
  void setTimeoutEventHandler(EventHandler handler, int delay, int times, Object arg_);

  /** Remove timeout event */
  void removeTimeoutEvent();

  /** Start the message or timer polling */
  void startPolling();

  /** Close all sockets */
  void close();

}
