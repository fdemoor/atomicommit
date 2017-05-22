package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private final Logger logger = LogManager.getLogger();

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
        logger.debug("[Transaction Manager #{}] Received YES from Storage Node {}", myID, id);
      }
      return checkAllHasProposed();
    }

    private int handleNO(NodeID id) {
      if (!hasProposed.contains(id)) {
        hasProposed.add(id);
        decision = false;
        logger.debug("[Transaction Manager #{}] Received NO from Storage Node {}", myID, id);
      }
      return checkAllHasProposed();
    }

    private int checkAllHasProposed() {
      if (hasProposed.size() == storageNodes.size()) {
        if (decision) {
          sendToAllStorageNodes("COMMIT");
          logger.debug("[Transaction Manager #{}] Decided to commit transaction", myID);
        } else {
          sendToAllStorageNodes("ABORT");
          logger.debug("[Transaction Manager #{}] Decided to abort transaction", myID);
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
