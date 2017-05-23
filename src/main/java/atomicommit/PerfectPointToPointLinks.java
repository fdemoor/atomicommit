package atomicommit;

public interface PerfectPointToPointLinks {

  /** Bind a socket for this node */
  public void setIn(NodeID id);

  /** Add a connecting socket to this node */
  public void addOut(NodeID id);

  /** Send a message */
  public void send(NodeID dest, Message message);;

  /**  Retrieve an incoming message */
  public Message deliver();

  /** Set the handler called when a message is received
    * The handler has to call method deliver() to actually get the message */
  public void setMessageEventHandler(EventHandler handler);

  /** Set the handler called when a timeout is received */
  public void setTimeoutEventHandler(EventHandler handler, int delay, int times);

  /** Start the message or timer polling */
  public void startPolling();

  /** Close all sockets */
  public void close();

}
