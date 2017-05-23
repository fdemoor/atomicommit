package atomicommit.perfectpointtopointlinks;

import atomicommit.*;
import atomicommit.eventhandler.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZThread;

import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import javafx.util.Pair;

public class ZMQChannel implements PerfectPointToPointLinks {

  private ZMQ.Context context;
  private ZMQ.Socket in;
  private HashMap<NodeID,ZMQ.Socket> out;
  private ZLoop reactor;
  private final Logger logger = LogManager.getLogger();

  public ZMQChannel() {
    context = ZMQ.context(1);
    out = new HashMap<NodeID,ZMQ.Socket>();
    reactor = new ZLoop();
  }

  private class ZMQEventhandler implements ZLoop.IZLoopHandler {

    private final EventHandler handler;

    ZMQEventhandler(EventHandler h) {
      handler = h;
    }

    @Override
    public int handle(ZLoop loop, PollItem item, Object arg_) {
      handler.handle(arg_);
      return 0;
    }

  }

  public void setIn(NodeID id) {
    in = context.socket(ZMQ.DEALER);
    in.bind(id.getIP());
  }

  public void addOut(NodeID id) {
    ZMQ.Socket skt = context.socket(ZMQ.DEALER);
    skt.connect(id.getIP());
    out.put(id, skt);
  }

  public void send(NodeID dest, NodeID srcID, String message) {
    ZMQ.Socket skt = out.get(dest);
    if (skt == null) {
      logger.warn("No out channel for this destination");
    } else {
      ZMsg msg = new ZMsg();
      String src = "" + srcID.getID();
      msg.add(src);
      msg.add(message);
      msg.send(skt);
    }
  }

  public void send(NodeID dest, NodeID srcID, List<String> messages) {
    ZMQ.Socket skt = out.get(dest);
    if (skt == null) {
      logger.warn("No out channel for this destination");
    } else {
      ZMsg msg = new ZMsg();
      String src = "" + srcID.getID();
      msg.add(src);
      Iterator<String> it = messages.iterator();
      while (it.hasNext()) {
        msg.add(it.next());
      }
      msg.send(skt);
    }
  }

  public Pair<NodeID,List<String>> deliver() {
    ZMsg msg = ZMsg.recvMsg(in);
    String message = msg.popString();
    NodeID src = new NodeID(new Integer(message));
    List<String> messages = new ArrayList<String>();
    message = msg.popString();
    while (message != null) {
      messages.add(message);
      message = msg.popString();
    }
    Pair<NodeID,List<String>> result = new Pair<NodeID,List<String>>(src, messages);
    return result;
  }

  public void setMessageEventHandler(EventHandler handler) {
    ZMQEventhandler h = new ZMQEventhandler(handler);
    PollItem item = new PollItem(in, ZMQ.Poller.POLLIN);
    reactor.addPoller(item, h, null);
  }

  public void setTimeoutEventHandler(EventHandler handler, int delay, int times) {
    ZMQEventhandler h = new ZMQEventhandler(handler);
    reactor.addTimer(delay, times, h, null);
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
