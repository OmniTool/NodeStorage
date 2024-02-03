package hex.multinode.storage.repository.db;

import hex.multinode.storage.model.data.MultiNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface NodeDBRepository extends JpaRepository<MultiNode, UUID> {

    List<MultiNode> findNodesByTitle(String title);

}
