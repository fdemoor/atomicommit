package atomicommit;

import org.zeromq.ZMQ;
import org.zeromq.ZLoop;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.PollItem;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import javafx.util.Pair;

public class Channel {

  private ZMQ.Context context;
  private ZMQ.Socket in;
  private HashMap<NodeID,ZMQ.Socket> out;

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
      System.out.println("No out channel for this destination");
    } else {
      ZMsg msg = new ZMsg();
      String src = "" + srcID.getID();
      msg.add(src);
      msg.add(message);
      msg.send(skt);
    }
  }

  public Pair<NodeID,String> deliver() {
    ZMsg msg = ZMsg.recvMsg(in);
    NodeID src = new NodeID(new Integer(msg.popString()));
    Pair<NodeID,String> result = new Pair<NodeID,String>(src, msg.popString());
    return result;
  }

  public void setInEventHandler(ZLoop reactor, ZLoop.IZLoopHandler handler, Object arg_) {
    PollItem item = new PollItem(in, ZMQ.Poller.POLLIN);
    reactor.addPoller(item, handler, arg_);
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
