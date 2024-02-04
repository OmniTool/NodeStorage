package hex.multinode.storage.service;

import hex.multinode.storage.aspect.NodeToLog;
import hex.multinode.storage.model.data.MultiContent;
import hex.multinode.storage.model.data.MultiLink;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;
import hex.multinode.storage.repository.db.LinkDBRepository;
import hex.multinode.storage.repository.db.NodeDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MultiNodeManagerImpl implements NodeManager<MultiNode> {
    private final NodeDBRepository nodeRepository;
    private final LinkDBRepository linkRepository;

    @Autowired
    public MultiNodeManagerImpl(NodeDBRepository nodeRepository,
                                LinkDBRepository linkRepository) {
        this.nodeRepository = nodeRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode save(NodeDTO nodeDTO) {
        return saveNewNodeFromDTO(nodeDTO);
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode save(MultiNode node) {
        return nodeRepository.save(node);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MultiNode> findById(String id) {
        return nodeRepository.findById(UUID.fromString(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultiNode> findNodesByTitle(String title) {
        return nodeRepository.findNodesByTitle(title);
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode update(NodeDTO nodeDTO) {
        MultiNode node = findById(nodeDTO.id()).orElseThrow();
        node.setTitle(nodeDTO.title());
        updateContent(nodeDTO, node);
        return save(node);
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode deleteById(String id) {
        MultiNode node = findById(id).orElseThrow();
        nodeRepository.deleteById(node.getId());
        return node;
    }

    @Override
    @Transactional
    public MultiNode fork(String fromNodeId, NodeDTO toNodeDTO, String answer) {
        MultiNode childNode = saveNewNodeFromDTO(toNodeDTO);
        MultiNode parentNode = nodeRepository.findById(UUID.fromString(fromNodeId)).orElseThrow();
        linkNodes(parentNode, childNode, answer);
        return parentNode;
    }

    @Override
    @Transactional
    public MultiNode fork(String fromNodeId, String toNodeId, String answer) {
        MultiNode parentNode = nodeRepository.findById(UUID.fromString(fromNodeId)).orElseThrow();
        MultiNode childNode = nodeRepository.findById(UUID.fromString(toNodeId)).orElseThrow();
        linkNodes(parentNode, childNode, answer);
        return parentNode;
    }

    private void updateContent(NodeDTO nodeDTO, MultiNode node) {
        String text = nodeDTO.contentText();
        MultiContent content = node.getContent();
        if (content == null) {
            content = new MultiContent();
        }
        content.setText(text);
        node.setContent(content);
    }

    private MultiNode saveNewNodeFromDTO(NodeDTO nodeDTO) {
        MultiNode node = new MultiNode(nodeDTO.title());
        updateContent(nodeDTO, node);
        return nodeRepository.save(node);
    }

    private MultiLink linkNodes(MultiNode parentNode, MultiNode childNode, String answer) {
        return addLink(parentNode, childNode, answer);
    }
    private MultiLink addLink(MultiNode parentNode, MultiNode childNode, String answer) {
        return linkRepository.save(new MultiLink(parentNode, childNode, answer));
    }

}
