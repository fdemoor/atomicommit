package atomicommit.eventhandler;

import atomicommit.*;
import atomicommit.perfectpointtopointlinks.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import javafx.util.Pair;

public class MessageHandler2PCSlave implements EventHandler {

  private final NodeID myID;
  private final PerfectPointToPointLinks channel;
  private final NodeID txManager;
  private final Logger logger = LogManager.getLogger();

  public MessageHandler2PCSlave(PerfectPointToPointLinks ch, NodeID id, NodeID txID) {
    channel = ch;
    txManager = txID;
    myID = id;
  }

  private void handleXACT(int trID) {
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
  }

  private void handleCOMMIT(int trID) {
    logger.debug("[Storage Node #{}] Transaction #{} commited", myID, trID);
  }

  private void handleABORT(int trID) {
    logger.debug("[Storage Node #{}] Transaction #{} aborted", myID, trID);
  }

  @Override
  public void handle(Object arg_) {
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
        handleXACT(trID);
        break;
      case "COMMIT":
        handleCOMMIT(trID);
        break;
      case "ABORT":
        handleABORT(trID);
        break;
    }
  }

}
