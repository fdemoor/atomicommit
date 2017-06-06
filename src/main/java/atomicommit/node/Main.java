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

    NodeConfig config = new NodeConfig(NodeConfig.TrProtocol.TWO_PHASE_COMMIT, 100, 1000, 3);

    int managerID = 0;
    wrapper.add(managerID, "tcp://localhost:" + managerID);
    ArrayList<Integer> storageIDs = new ArrayList<Integer>();
    for (int i = 1; i < N; i++) {
      storageIDs.add(i);
      wrapper.add(i, "tcp://localhost:" + i);
    }
    for (int i = 1; i < N; i++) {
      ZThread.start(new StorageNode(config, i, managerID, storageIDs, wrapper));
    }

    TransactionManager mng = new TransactionManager(config, managerID, storageIDs, wrapper);
    mng.runTransaction(1, config.getNbTr());
    ZThread.start(mng);

  }
}
