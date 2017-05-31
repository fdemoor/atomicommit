package atomicommit.channels;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

public class ZMQChannel implements PerfectPointToPointLinks {

  private final ZMQ.Context context;
  private ZMQ.Socket in;
  private NodeID owner;
  private HashMap<NodeID,ZMQ.Socket> out;
  private final ZLoop reactor;
  private final NodeIDWrapper nodesWrapper;
  private final Logger logger = LogManager.getLogger();

  public ZMQChannel(NodeIDWrapper wrapper) {
    nodesWrapper = wrapper;
    context = ZMQ.context(1);
    out = new HashMap<NodeID,ZMQ.Socket>();
    reactor = new ZLoop();
  }

  private class ZMQTimerHandler implements ZLoop.IZLoopHandler {

    private final EventHandler handler;

    ZMQTimerHandler(EventHandler h) {
      handler = h;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      handler.handle(arg_);
      return 0;
    }

  }

  private class ZMQMessageHandler implements ZLoop.IZLoopHandler {

    private final MessageHandler handler;

    ZMQMessageHandler(MessageHandler h) {
      handler = h;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      handler.handle(arg_);
      return 0;
    }

  }

  public void setIn(NodeID id) {
    if (owner != null) {
      logger.warn("An In-Socket is already defined");
    }
    owner = id;
    in = context.socket(ZMQ.DEALER);
    in.bind(id.getIP());
    ZMQ.Socket skt = context.socket(ZMQ.DEALER);
    skt.connect(id.getIP());
    out.put(id, skt);
  }

  public void addOut(NodeID id) {
    ZMQ.Socket skt = context.socket(ZMQ.DEALER);
    skt.connect(id.getIP());
    out.put(id, skt);
  }

  public void send(NodeID dest, Message message) {
    ZMQ.Socket skt = out.get(dest);
    if (message.getSrc() != owner) {
      logger.warn("Trying to send a message from someone else");
    }
    if (skt == null) {
      logger.warn("No out channel for this destination");
    } else {
      ZMsg msg = new ZMsg();
      msg.add("" + message.getSrc().getID());
      msg.add("" + message.getID());
      msg.add(message.getType().name());
      if (message.getKey() != null) {
        msg.add("" + message.getKey());
      }
      msg.send(skt);
      logger.debug("Node #{} sent [{}, {}, {}] to node #{}", message.getSrc(), message.getID(), message.getType(), message.getKey(), dest);
    }
  }

  public Message deliver() {
    ZMsg msg = ZMsg.recvMsg(in);
    NodeID src = nodesWrapper.getNodeID(Integer.parseInt(msg.popString()));
    int id = Integer.parseInt(msg.popString());
    MessageType type = MessageType.valueOf(msg.popString());
    String key = msg.popString();
    Message message = null;
    if (key != null) {
      message = new Message(src, id, type, Integer.parseInt(key));
    } else {
      message = new Message(src, id, type);
    }
    logger.debug("Node #{} received [{}, {}, {}] from node #{}", owner, message.getID(), message.getType(), message.getKey(), message.getSrc());
    return message;
  }

  public void setMessageEventHandler(MessageHandler handler) {
    ZMQMessageHandler h = new ZMQMessageHandler(handler);
    PollItem item = new PollItem(in, ZMQ.Poller.POLLIN);
    reactor.addPoller(item, h, null);
  }

  public void setTimeoutEventHandler(EventHandler handler, int delay, int times, Object arg_) {
    ZMQTimerHandler h = new ZMQTimerHandler(handler);
    reactor.addTimer(delay, times, h, arg_);
  }

  public void removeTimeoutEvent() {
    reactor.removeTimer(null);
  }

  public void startPolling() {
    reactor.start();
  }

  public void close() {
    in.close();
    Collection<ZMQ.Socket> skts = out.values();
    Iterator<ZMQ.Socket> it = skts.iterator();
    while (it.hasNext()) {
      ZMQ.Socket skt = it.next();
      skt.close();
    }
    out = null;
    context.term();
  }

}