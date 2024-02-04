package hex.multinode.storage.service;

import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeManager {

    MultiNode save(NodeDTO node);

    MultiNode save(MultiNode node);

    Optional<MultiNode> findById(String id);

    List<MultiNode> findNodesByTitle(String title);

    MultiNode update(NodeDTO node);

    MultiNode deleteById(String id);

    MultiNode fork(String fromNodeId, NodeDTO toNodeDTO, String answer);

    MultiNode fork(String fromNodeId, String toNodeId, String answer);
}
