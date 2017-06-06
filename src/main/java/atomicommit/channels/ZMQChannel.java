package atomicommit.channels;

import atomicommit.events.EventHandler;
import atomicommit.events.MessageHandler;
import atomicommit.util.node.NodeID;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.util.msg.Message;
import atomicommit.util.msg.MessageType;
import atomicommit.util.misc.Pair;

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
import java.util.List;
import java.util.ArrayList;

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
      logger.error("Trying to send a message from someone else");
    }
    if (skt == null) {
      logger.warn("No out channel for this destination");
    } else {
      ZMsg msg = new ZMsg();
      msg.add("" + message.getSrc().getID());
      msg.add("" + message.getID());
      msg.add(message.getType().name());
      Integer key = message.getKey();
      List<Pair<NodeID,Boolean>> votes = message.getVotes();
      if (key != null) {
        msg.add("" + key);
        logger.debug("Node #{} sent [{}, {}, {}] to node #{}", message.getSrc(), message.getID(), message.getType(), key, dest);
      } else if (votes != null) {
        Iterator<Pair<NodeID,Boolean>> it = votes.iterator();
        while (it.hasNext()) {
          Pair<NodeID,Boolean> pair = it.next();
          msg.add("" + pair.getFirst().getID());
          msg.add(pair.getSecond().toString());
        }
        logger.debug("Node #{} sent [{}, {}, {}] to node #{}", message.getSrc(), message.getID(), message.getType(), votes, dest);
      } else {
        logger.debug("Node #{} sent [{}, {}] to node #{}", message.getSrc(), message.getID(), message.getType(), dest);
      }
      msg.send(skt);
    }
  }

  public Message deliver() {
    ZMsg msg = ZMsg.recvMsg(in);
    NodeID src = nodesWrapper.getNodeID(Integer.parseInt(msg.popString()));
    int id = Integer.parseInt(msg.popString());
    MessageType type = MessageType.valueOf(msg.popString());
    String key = msg.popString();
    Message message = null;
    if (key == null) {
      message = new Message(src, id, type);
      logger.debug("Node #{} received [{}, {}] from node #{}", owner, message.getID(), message.getType(), message.getSrc());
    } else {
      switch (type) {
        case TR_COLL:
          List<Pair<NodeID,Boolean>> votes = new ArrayList<Pair<NodeID,Boolean>>();
          String vote = msg.popString();
          while (key != null && vote != null) {
            votes.add(new Pair<NodeID,Boolean>(nodesWrapper.getNodeID(Integer.parseInt(key)), Boolean.valueOf(vote)));
            key = msg.popString();
            vote = msg.popString();
          }
          message = new Message(src, id, type, votes);
          logger.debug("Node #{} received [{}, {}, {}] from node #{}", owner, message.getID(), message.getType(), votes, message.getSrc());
          break;
        default:
          int k = Integer.parseInt(key);
          message = new Message(src, id, type, k);
          logger.debug("Node #{} received [{}, {}, {}] from node #{}", owner, message.getID(), message.getType(), k, message.getSrc());
          break;
      }
    }
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
