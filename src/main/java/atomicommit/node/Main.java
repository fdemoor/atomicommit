package atomicommit.node;

import atomicommit.node.TransactionManager;
import atomicommit.node.StorageNode;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.node.NodeConfig;

import java.util.ArrayList;

import org.zeromq.ZThread;

public class Main {
  public static void main(String[] args) {

    NodeIDWrapper wrapper = new NodeIDWrapper();
    int N = 4;

    NodeConfig config = new NodeConfig(NodeConfig.TrProtocol.INBAC, 1000, 1, 1);

    int managerID = 0;
    wrapper.add(managerID, "tcp://localhost:" + managerID, 0);
    ArrayList<Integer> storageIDs = new ArrayList<Integer>();
    for (int i = 1; i < N; i++) {
      storageIDs.add(i);
      wrapper.add(i, "tcp://localhost:" + i, 1);
    }
    for (int i = 1; i < N; i++) {
      NodeIDWrapper wrapper2 = new NodeIDWrapper(wrapper);
      ZThread.start(new StorageNode(config, i, managerID, storageIDs, wrapper2));
    }

    TransactionManager mng = new TransactionManager(config, managerID, storageIDs, wrapper);
    mng.runTransaction(1, config.getNbTr());
    ZThread.start(mng);

  }
}
