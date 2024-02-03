package hex.multinode.storage.repository.db;

import hex.multinode.storage.model.data.MultiFork;
import hex.multinode.storage.model.data.MultiNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ForkDBRepository extends JpaRepository<MultiFork, UUID> {

    List<MultiFork> findForksByChildNodeId(UUID childNodeId);

    List<MultiFork> findForksByParentNodeId(UUID parentNodeId);

}
