package hex.multinode.storage.service;

import hex.multinode.storage.model.dto.NodeDTO;

import java.util.List;
import java.util.Optional;

/**
 * Эмулятор "Нелинейного сюжета"
 * @param <N> Мультинода - логический блок контента, который может ветвиться
 */
public interface NodeManager<N> {

    N save(NodeDTO node);

    N save(N node);

    Optional<N> findById(String id);

    List<N> findNodesByTitle(String title);

    N update(NodeDTO node);

    N deleteById(String id);

    N fork(String fromNodeId, NodeDTO toNodeDTO, String answer);

    N fork(String fromNodeId, String toNodeId, String answer);
}
