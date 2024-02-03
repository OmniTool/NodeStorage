package hex.multinode.storage.service;

import hex.multinode.storage.aspect.NodeToLog;
import hex.multinode.storage.model.data.MultiContent;
import hex.multinode.storage.model.data.MultiFork;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.data.MultiRoot;
import hex.multinode.storage.model.dto.NodeDTO;
import hex.multinode.storage.repository.db.ForkDBRepository;
import hex.multinode.storage.repository.db.NodeDBRepository;
import hex.multinode.storage.repository.db.RootDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MultiNodeManagerImpl implements NodeManager {
    private final NodeDBRepository nodeRepository;
    private final RootDBRepository rootRepository;
    private final ForkDBRepository forkRepository;

    @Autowired
    public MultiNodeManagerImpl(NodeDBRepository nodeRepository,
                                RootDBRepository rootRepository,
                                ForkDBRepository forkRepository) {
        this.nodeRepository = nodeRepository;
        this.rootRepository = rootRepository;
        this.forkRepository = forkRepository;
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode save(NodeDTO nodeDTO) {
        MultiNode node = new MultiNode(nodeDTO.title());
        updateContent(nodeDTO, node);
        return nodeRepository.save(node);
    }

    @Override
    @NodeToLog
    @Transactional
    public MultiNode save(MultiNode node) {
        return nodeRepository.save(node);
    }

    @Override
    @Transactional
    public Optional<MultiNode> findById(String id) {
        return nodeRepository.findById(UUID.fromString(id));
    }

    @Override
    @Transactional
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
    public MultiNode fork(String currentNodeId, NodeDTO forkNodeDTO) {
        MultiNode childNode = new MultiNode(forkNodeDTO.title());
        updateContent(forkNodeDTO, childNode);
        childNode = nodeRepository.save(childNode);
        MultiNode parentNode = nodeRepository.findById(UUID.fromString(currentNodeId)).orElseThrow();
        updateRoots(childNode, parentNode);
        updateForks(parentNode, childNode);
        return childNode;
    }

    @Override
    @Transactional
    public MultiNode fork(String parentNodeId, String childNodeId) {
        return null; //TODO
    }

    private void updateForks(MultiNode parentNode, MultiNode childNode) {
        forkRepository.save(new MultiFork(parentNode, childNode, "choice to: " + childNode.getTitle()));
    }

    private void updateRoots(MultiNode childNode, MultiNode parentNode) {
        rootRepository.save(new MultiRoot(childNode, parentNode));
    }

    private void updateContent(NodeDTO nodeDTO, MultiNode node) {
        String text = nodeDTO.contentText();
        MultiContent content = node.getContent();
        if (content != null) {
            content.setText(text);
            node.setContent(content);
        } else {
            node.setContent(new MultiContent(text));
        }
    }

}
