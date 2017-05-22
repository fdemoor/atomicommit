package atomicommit;

import java.util.ArrayList;

import org.zeromq.ZThread;

public class Main {
  public static void main(String[] args) {
    NodeID managerID = new NodeID(0);
    ArrayList<NodeID> storageIDs = new ArrayList<NodeID>();
    for (int i = 1; i < 4; i++) {
      NodeID id = new NodeID(i);
      storageIDs.add(id);
      ZThread.start(new StorageNode(id, managerID));
    }

    ZThread.start(new TransactionManager(managerID, storageIDs));

  }
}
