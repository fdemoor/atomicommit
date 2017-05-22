package atomicommit;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import javafx.util.Pair;

public class TransactionManager implements ZThread.IDetachedRunnable {

  private final List<NodeID> storageNodes;
  private ArrayList<NodeID> hasProposed;
  private boolean decision;
  private final NodeID myID;
  private Channel channel;

  TransactionManager(NodeID id, List<NodeID> servers) {

    storageNodes = servers;
    myID = id;

    hasProposed = new ArrayList();
    decision = true;

    channel = new Channel();
    channel.setIn(myID);
    Iterator<NodeID> it = storageNodes.iterator();
    while (it.hasNext()) {
      NodeID idNode = it.next();
      channel.addOut(idNode);
    }

  }

  private void sendToAllStorageNodes(String message) {
    Iterator<NodeID> it = storageNodes.iterator();
    while (it.hasNext()) {
      channel.send(it.next(), myID, message);
    }
  }

  private class ManagerMessageHandler implements ZLoop.IZLoopHandler {

    private int handleYES(NodeID id) {
      if (!hasProposed.contains(id)) {
        hasProposed.add(id);
        System.out.format("[Transaction Manager #%s] Received YES from Storage Node %s%n", myID, id);
      }
      return checkAllHasProposed();
    }

    private int handleNO(NodeID id) {
      if (!hasProposed.contains(id)) {
        hasProposed.add(id);
        decision = false;
        System.out.format("[Transaction Manager #%s] Received NO from Storage Node %s%n", myID, id);
      }
      return checkAllHasProposed();
    }

    private int checkAllHasProposed() {
      if (hasProposed.size() == storageNodes.size()) {
        if (decision) {
          sendToAllStorageNodes("COMMIT");
          System.out.format("[Transaction Manager #%s] Decided to commit transaction%n", myID);
        } else {
          sendToAllStorageNodes("ABORT");
          System.out.format("[Transaction Manager #%s] Decided to abort transaction%n", myID);
        }
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      Pair<NodeID,String> messagePair = channel.deliver();
      String msg = messagePair.getValue();
      NodeID src = messagePair.getKey();
      switch(msg) {
        case "YES":
          return handleYES(src);
        case "NO":
          return handleNO(src);
      }
      return 0;
    }
  }

   @Override
   public void run(Object[] args) {
     ZLoop reactor = new ZLoop();
     ZLoop.IZLoopHandler handler = new ManagerMessageHandler();
     channel.setInEventHandler(reactor, handler, null);

     sendToAllStorageNodes("XACT");
     reactor.start();

     channel.close();
   }



}
