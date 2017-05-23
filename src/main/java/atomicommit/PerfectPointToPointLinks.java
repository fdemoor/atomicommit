package atomicommit;

import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import javafx.util.Pair;

public interface PerfectPointToPointLinks {

  /** Bind a socket for this node */
  public void setIn(NodeID id);

  /** Add a connecting socket to this node */
  public void addOut(NodeID id);

  /** Send a string message */
  public void send(NodeID dest, NodeID srcID, String message);

  /** Send a list of string messages */
  public void send(NodeID dest, NodeID srcID, List<String> messages);

  /**  Retrieve an incoming message */
  public Pair<NodeID,List<String>> deliver();

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
