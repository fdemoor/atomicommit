package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import java.util.Random;

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

    private int handleXACT() {
      Random rand = new Random();
      int commitProba = 90;
      int randint = rand.nextInt(100);
      String choice = "NO";
      if (randint < commitProba) {
        choice = "YES";
      }
      logger.debug("[Storage Node #{}] Received XACT - Proposed to commit? {}", myID, choice);

      channel.send(txManager, myID, choice);

      return 0;
    }

    private int handleCOMMIT() {
      logger.debug("[Storage Node #{}] Transaction commited", myID);
      return -1;
    }

    private int handleABORT() {
      logger.debug("[Storage Node #{}] Transaction aborted", myID);
      return -1;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      Pair<NodeID,String> messagePair = channel.deliver();
      String msg = messagePair.getValue();
      NodeID src = messagePair.getKey();
      if (!(src.equals(txManager))) {
        logger.debug("[Storage Node #{}] Message not coming from manager", myID);
      }
      switch(msg) {
        case "XACT":
          return handleXACT();
        case "COMMIT":
          return handleCOMMIT();
        case "ABORT":
          return handleABORT();
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
