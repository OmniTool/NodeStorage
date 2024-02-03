package hex.multinode.storage.repository.db;

import hex.multinode.storage.model.data.MultiFork;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.data.MultiRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RootDBRepository extends JpaRepository<MultiRoot, UUID> {

    List<MultiRoot> findRootsByChildNodeId(UUID childNodeId);

    List<MultiRoot> findRootsByParentNodeId(UUID parentNodeId);

}
