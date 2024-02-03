package hex.multinode.storage.repository.db;

import hex.multinode.storage.NodeStorageApplication;
import hex.multinode.storage.config.H2JpaTestConfig;
import hex.multinode.storage.model.data.MultiContent;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.generator.UUIDV7Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NodeStorageApplication.class, H2JpaTestConfig.class})
public class NodeDBRepositoryTest {

    private final NodeDBRepository nodeRepository;

    private final String initialNodeTitle = "Стрекоза и муравей";
    private final String initialContentText = """
                        Попрыгунья Стрекоза
                        Лето красное пропела;
                        """;

    @Autowired
    public NodeDBRepositoryTest(NodeDBRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @BeforeEach
    public void initCase() {
        nodeRepository.deleteAll();
        MultiNode node = new MultiNode(initialNodeTitle);
        node.setContent(
                new MultiContent(initialContentText)
        );
        nodeRepository.save(node);
    }

    @Test
    public void findByTitle() {
        findByTitleAndAssertSingleNode(initialNodeTitle, initialContentText);
    }

    @Test
    public void ceateNew() {
        String newNodeTitle = "Ворона и лисица";
        String newContentText = """
                        Вороне где-то бог послал кусочек сыру;
                        """;
        MultiNode node = new MultiNode(newNodeTitle);
        node.setContent(
                new MultiContent(newContentText)
        );
        var savedNode = nodeRepository.save(node);
        assertNotNull(savedNode);
        assertNotNull(savedNode.getId());
        findByTitleAndAssertSingleNode(newNodeTitle, newContentText);
    }

    @Test
    public void cantCreateFromEntityWithEmptyTitle() {
        var node = new MultiNode();
        assertThrows(DataIntegrityViolationException.class, () -> nodeRepository.save(node));
    }


    @Test
    public void findByExistingId() {
        var initialNode = findByTitleAndAssertSingleNode(initialNodeTitle, initialContentText);
        var nodeId = initialNode.getId();
        Optional<MultiNode> node = nodeRepository.findById(nodeId);
        assertTrue(node.isPresent());
        assertEquals(initialNodeTitle, node.get().getTitle());
        assertEquals(initialContentText, node.get().getContent().getText());
    }

    @Test
    public void cantFindByNotExistingId() {
        var nodeId = UUIDV7Generator.generateUuidV7();
        Optional<MultiNode> node = nodeRepository.findById(nodeId);
        assertFalse(node.isPresent());
    }

    @Test
    public void cantFindByNullId() {
        assertThrows(RuntimeException.class, () ->
                nodeRepository.findById(null));
    }

    @Test
    public void updateTitle() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle, initialContentText);
        String newNodeTitle = "Муравей и стрекоза";
        node.setTitle(newNodeTitle);
        nodeRepository.save(node);
        findByTitleAndAssertSingleNode(newNodeTitle, initialContentText);
    }

    @Test
    public void deleteNode() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle, initialContentText);
        nodeRepository.delete(node);
        var nodesNotExisting = nodeRepository.findNodesByTitle(initialNodeTitle);
        assertTrue(nodesNotExisting.isEmpty());
    }

    @Test
    public void deleteByExistingId() {
        var initialNode = findByTitleAndAssertSingleNode(initialNodeTitle, initialContentText);
        var nodeId = initialNode.getId();
        nodeRepository.deleteById(nodeId);
        var nodesNotExisting = nodeRepository.findNodesByTitle(initialNodeTitle);
        assertTrue(nodesNotExisting.isEmpty());
    }

    @Test
    public void deleteByNotExistingId() {
        var nodeId = UUIDV7Generator.generateUuidV7();
        assertDoesNotThrow(() ->
                nodeRepository.deleteById(nodeId));
    }

    private MultiNode findByTitleAndAssertSingleNode(String title, String contentText) {
        var nodes = nodeRepository.findNodesByTitle(title);
        assertFalse(nodes.isEmpty());
        assertEquals(1, nodes.size());
        MultiNode node = nodes.get(0);
        assertEquals(title, node.getTitle());
        assertEquals(contentText, node.getContent().getText());
        return node;
    }

}
