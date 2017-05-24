package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler {

  private final Node node;
  private final Logger logger = LogManager.getLogger();

  private EventHandler startTransaction;
  private EventHandler transactionHandler;

  MessageHandler(Node n) {
    node = n;
    if (n instanceof StorageNode) {
      startTransaction = new MsgHandlerStartTR((StorageNode) n);
    }
  }

  void setTransactionHandler(EventHandler handler) {
    transactionHandler = handler;
  }

  public void handle(Object arg_) {
    Message message = node.deliverMessage();
    String type = message.getType().name();
    if (type.contains("TR")) {
      switch (message.getType()) {
        case TR_START:
          startTransaction.handle((Object) message);
          break;
        default:
          transactionHandler.handle((Object) message);
          break;
      }
    }
  }

}
