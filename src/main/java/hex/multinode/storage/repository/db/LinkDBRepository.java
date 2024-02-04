package hex.multinode.storage.repository.db;

import hex.multinode.storage.model.data.MultiLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LinkDBRepository extends JpaRepository<MultiLink, UUID> {

    List<MultiLink> findLinksByChildNodeId(UUID childNodeId);

    List<MultiLink> findLinksByParentNodeId(UUID parentNodeId);

}
