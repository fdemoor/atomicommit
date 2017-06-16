package atomicommit.node;

import atomicommit.node.TransactionManager;
import atomicommit.node.StorageNode;
import atomicommit.util.node.NodeIDWrapper;
import atomicommit.node.NodeConfig;

import java.util.ArrayList;
import java.lang.Thread;
import java.nio.channels.ClosedByInterruptException;

public class Main {
  public static void main(String[] args) {

    NodeIDWrapper wrapper = new NodeIDWrapper();
    int N = 4;

    // NodeConfig config = new NodeConfig(NodeConfig.TrProtocol.TWO_PHASE_COMMIT, 100, 1, 1, 42419841);
    NodeConfig config = new NodeConfig(NodeConfig.TrProtocol.INBAC, 100, 1, 1, 42419841);
    // NodeConfig config = new NodeConfig(NodeConfig.TrProtocol.ZERO_NBAC, 100, 1, 1, 42419841);
    config.setCrashFailureConfig(0, 0);

    int managerID = 0;
    wrapper.add(managerID, "tcp://localhost:" + managerID, 0);
    ArrayList<Integer> storageIDs = new ArrayList<Integer>();
    for (int i = 1; i < N; i++) {
      storageIDs.add(i);
      wrapper.add(i, "tcp://localhost:" + i, 1);
    }

    for (int i = 1; i < N; i++) {
      NodeIDWrapper wrapper2 = new NodeIDWrapper(wrapper);
      (new Thread(new StorageNode(config, i, managerID, storageIDs, wrapper2))).start();
    }

    TransactionManager mng = new TransactionManager(config, managerID, storageIDs, wrapper);
    mng.runTransaction(1000, config.getNbTr());
    (new Thread(mng)).start();
    return;

  }
}
