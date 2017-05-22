package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import javafx.util.Pair;

public class StorageNode implements ZThread.IDetachedRunnable {

  private final NodeID myID;
  private final NodeID txManager;
  private Channel channel;
  private final Logger logger = LogManager.getLogger();

  StorageNode(NodeID id, NodeID manager) {

    myID = id;
    txManager = manager;

    channel = new Channel();
    channel.setIn(myID);
    channel.addOut(txManager);

  }

  private class SlaveMessageHandler implements ZLoop.IZLoopHandler {

    private int handleXACT(int trID) {
      Random rand = new Random();
      int commitProba = 75;
      int randint = rand.nextInt(100);
      String choice = "NO";
      if (randint < commitProba) {
        choice = "YES";
      }
      logger.debug("[Storage Node #{}] Received XACT for transaction #{}, proposed to commit? {}", myID, trID, choice);

      List<String> messages = new ArrayList<String>();
      messages.add("" + trID);
      messages.add(choice);
      channel.send(txManager, myID, messages);

      return 0;
    }

    private int handleCOMMIT(int trID) {
      logger.debug("[Storage Node #{}] Transaction #{} commited", myID, trID);
      return 0;
    }

    private int handleABORT(int trID) {
      logger.debug("[Storage Node #{}] Transaction #{} aborted", myID, trID);
      return 0;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      Pair<NodeID,List<String>> messagePair = channel.deliver();
      List<String> messages = messagePair.getValue();
      int trID = new Integer(messages.get(0));
      String msg = messages.get(1);
      NodeID src = messagePair.getKey();
      if (!(src.equals(txManager))) {
        logger.warn("[Storage Node #{}] Message {} not coming from manager", myID, msg);
      }
      switch(msg) {
        case "XACT":
          return handleXACT(trID);
        case "COMMIT":
          return handleCOMMIT(trID);
        case "ABORT":
          return handleABORT(trID);
      }
      return 0;
    }
  }

  @Override
  public void run(Object[] args) {
    ZLoop reactor = new ZLoop();
    ZLoop.IZLoopHandler handler = new SlaveMessageHandler();
    channel.setInEventHandler(reactor, handler, null);
    reactor.start();

    channel.close();
  }



}
