package atomicommit;

public interface PerfectPointToPointLinks {

  /** Bind a socket for this node */
  void setIn(NodeID id);

  /** Add a connecting socket to this node */
  void addOut(NodeID id);

  /** Send a message */
  void send(NodeID dest, Message message);;

  /**  Retrieve an incoming message */
  Message deliver();

  /** Set the handler called when a message is received
    * The handler has to call method deliver() to actually get the message */
  void setMessageEventHandler(EventHandler handler);

  /** Set the handler called when a timeout is received */
  void setTimeoutEventHandler(EventHandler handler, int delay, int times);

  /** Start the message or timer polling */
  void startPolling();

  /** Close all sockets */
  void close();

}
