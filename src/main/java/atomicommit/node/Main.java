package atomicommit.node;

import atomicommit.node.TransactionManager;
import atomicommit.node.StorageNode;

import java.util.ArrayList;

import org.zeromq.ZThread;

public class Main {
  public static void main(String[] args) {
    int managerID = 0;
    ArrayList<Integer> storageIDs = new ArrayList<Integer>();
    for (int i = 1; i < 4; i++) {
      storageIDs.add(i);
    }
    for (int i = 1; i < 4; i++) {
      ZThread.start(new StorageNode(i, managerID, storageIDs));
    }

    ZThread.start(new TransactionManager(managerID, storageIDs));

  }
}
