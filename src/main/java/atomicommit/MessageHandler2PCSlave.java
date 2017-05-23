package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class MessageHandler2PCSlave implements EventHandler {

  private final NodeID myID;
  private final PerfectPointToPointLinks channel;
  private final NodeID trManager;
  private final Logger logger = LogManager.getLogger();

  MessageHandler2PCSlave(PerfectPointToPointLinks ch, NodeID id, NodeID txID) {
    channel = ch;
    trManager = txID;
    myID = id;
  }

  private void handleXACT(int trID) {
    Random rand = new Random();
    int commitProba = 75;
    int randint = rand.nextInt(100);
    MessageType choice = MessageType.TR_NO;
    if (randint < commitProba) {
      choice = MessageType.TR_YES;
    }
    logger.debug("[Storage Node #{}] Received XACT for transaction #{}, proposed to commit? {}", myID, trID, choice);

    Message message = new Message(myID, trID, choice);
    channel.send(trManager, message);
  }

  private void handleCOMMIT(int trID) {
    logger.debug("[Storage Node #{}] Transaction #{} commited", myID, trID);
  }

  private void handleABORT(int trID) {
    logger.debug("[Storage Node #{}] Transaction #{} aborted", myID, trID);
  }

  @Override
  public void handle(Object arg_) {
    Message message = channel.deliver();
    int trID = message.getID();
    MessageType type = message.getType();
    NodeID src = message.getSrc();
    if (!(src.equals(trManager))) {
      logger.warn("[Storage Node #{}] Message {} not coming from manager", myID, type);
    }
    switch(type) {
      case TR_XACT:
        handleXACT(trID);
        break;
      case TR_COMMIT:
        handleCOMMIT(trID);
        break;
      case TR_ABORT:
        handleABORT(trID);
        break;
    }
  }

}
