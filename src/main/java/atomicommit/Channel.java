package atomicommit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;

import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import javafx.util.Pair;

public class Channel {

  private ZMQ.Context context;
  private ZMQ.Socket in;
  private HashMap<NodeID,ZMQ.Socket> out;
  private final Logger logger = LogManager.getLogger();

  Channel() {
    context = ZMQ.context(1);
    out = new HashMap<NodeID,ZMQ.Socket>();
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

  public void setInEventHandler(ZLoop reactor, ZLoop.IZLoopHandler handler, Object arg_) {
    PollItem item = new PollItem(in, ZMQ.Poller.POLLIN);
    reactor.addPoller(item, handler, arg_);
  }

  public void setTimerHandler(ZLoop reactor, ZLoop.IZLoopHandler handler, Object arg_, int delay, int times) {
    reactor.addTimer(delay, times, handler, arg_);
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
