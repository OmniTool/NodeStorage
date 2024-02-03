package hex.multinode.storage.service;

import hex.multinode.storage.NodeStorageApplication;
import hex.multinode.storage.config.H2JpaTestConfig;
import hex.multinode.storage.model.data.MultiContent;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;
import hex.multinode.storage.model.generator.UUIDV7Generator;
import hex.multinode.storage.repository.db.ForkDBRepository;
import hex.multinode.storage.repository.db.NodeDBRepository;
import hex.multinode.storage.repository.db.RootDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {NodeStorageApplication.class, H2JpaTestConfig.class})
public class NodeManagerTest {

    private final NodeManager nodeManager;
    private final NodeDBRepository nodeRepository;
    private final RootDBRepository rootRepository;
    private final ForkDBRepository forkRepository;

    private final String initialNodeTitle = "Стрекоза и муравей";
    private final String initialContentText = """
                        Попрыгунья Стрекоза
                        Лето красное пропела;
                        """;

    @Autowired
    public NodeManagerTest(NodeManager nodeManager,
                           NodeDBRepository nodeRepository,
                           RootDBRepository rootRepository,
                           ForkDBRepository forkRepository) {
        this.nodeManager = nodeManager;
        this.nodeRepository = nodeRepository;
        this.rootRepository = rootRepository;
        this.forkRepository = forkRepository;
    }

    @BeforeEach
    public void initCase() {
        rootRepository.deleteAll();
        forkRepository.deleteAll();
        nodeRepository.deleteAll();
        MultiNode node = new MultiNode(initialNodeTitle);
        node.setContent(
                new MultiContent(initialContentText)
        );
        nodeRepository.save(node);
    }

    @Test
    public void createFromEntity() {
        String newNodeTitle = "Ворона и лисица";
        String newContentText = """
                        Вороне где-то бог послал кусочек сыру;
                        """;
        var node = new MultiNode(newNodeTitle, new MultiContent(newContentText));
        var savedNode = nodeManager.save(node);
        assertNotNull(savedNode);
        assertNotNull(savedNode.getId());
        findByIdAndAssertSingleNode(savedNode.getId(), newNodeTitle, newContentText);
    }

    @Test
    public void createFromDTO() {
        String newNodeTitle = "Ворона и лисица";
        String newContentText = """
                        Вороне где-то бог послал кусочек сыру;
                        """;
        NodeDTO nodeDTO = NodeDTO.of(newNodeTitle, newContentText);
        var savedNode = nodeManager.save(nodeDTO);
        assertNotNull(savedNode);
        assertNotNull(savedNode.getId());
        findByIdAndAssertSingleNode(savedNode.getId(), newNodeTitle, newContentText);
    }

    @Test
    public void findByValidStringId() {
        String rareNodeId = UUIDV7Generator.generateUuidV7().toString();
        assertDoesNotThrow(() ->
                nodeManager.findById(rareNodeId));
    }

    @Test
    public void cantFindByInvalidId() {
        String rareNodeId = "000";
        assertThrows(IllegalArgumentException.class, () ->
                nodeManager.findById(rareNodeId));
    }

    @Test
    public void findExistingByUUID() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle);
        UUID nodeId = node.getId();
        findByIdAndAssertSingleNode(nodeId, initialNodeTitle, initialContentText);
    }

    @Test
    public void updateEntityTitleByExistingId() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newNodeTitle = "Муравей & стрекоза";
        node.setTitle(newNodeTitle);
        var savedNode = nodeManager.save(node);
        findByIdAndAssertSingleNode(savedNode.getId(), newNodeTitle, initialContentText);
    }

    @Test
    public void updateEntityContentByExistingId() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newContentText = """
                Уж сколько раз твердили миру,
                Что лесть гнусна, вредна; но только все не впрок,
                """;
        node.getContent().setText(newContentText);
        var savedNode = nodeManager.save(node);
        findByIdAndAssertSingleNode(savedNode.getId(), initialNodeTitle, newContentText);
    }

    @Test
    public void updateDTOByExistingId() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newNodeTitle = "Ворона и лисица";
        String newContentText = """
                Уж сколько раз твердили миру,
                Что лесть гнусна, вредна; но только все не впрок,
                """;
        NodeDTO nodeDTO = NodeDTO.of(node.getId().toString(), newNodeTitle, newContentText);
        var savedNode = nodeManager.update(nodeDTO);
        assertNotNull(savedNode);
        findByIdAndAssertSingleNode(node.getId(), newNodeTitle, newContentText);
    }

    @Test
    public void updateDTOByNotExistingId() {
        var nodeId = UUIDV7Generator.generateUuidV7();
        NodeDTO nodeDTO = NodeDTO.of(nodeId.toString(), "", "");
        assertThrows(NoSuchElementException.class, () ->
                nodeManager.update(nodeDTO));
    }

    @Test
    public void deleteByExistingId() {
        var node = findByTitleAndAssertSingleNode(initialNodeTitle);
        nodeManager.deleteById(node.getId().toString());
        var nodeFromRepo = nodeManager.findById(node.getId().toString());
        assertTrue(nodeFromRepo.isEmpty());
    }

    @Test
    public void deleteByNotExistingId() {
        var nodeId = UUIDV7Generator.generateUuidV7();
        assertThrows(NoSuchElementException.class, () ->
                nodeManager.deleteById(nodeId.toString()));
    }

    @Test
    public void fork() {
        var parentNode = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newNodeTitle = initialNodeTitle + "[1]";
        String newContentText = """
                Оглянуться не успела,
                Как зима катит в глаза.
                """;
        NodeDTO childNodeDTO = NodeDTO.of(newNodeTitle, newContentText);
        var childNode = nodeManager.fork(parentNode.getId().toString(), childNodeDTO);
        assertNotNull(childNode);

        childNode = findByIdAndAssertSingleNode(childNode.getId(), newNodeTitle, newContentText);
        var roots = rootRepository.findRootsByChildNodeId(childNode.getId());
        assertNotNull(roots);
        assertFalse(roots.isEmpty());
        assertEquals(newNodeTitle, roots.get(0).getChildNode().getTitle());
        assertEquals(initialNodeTitle, roots.get(0).getParentNode().getTitle());

        parentNode = findByTitleAndAssertSingleNode(initialNodeTitle);
        var forks = forkRepository.findForksByParentNodeId(parentNode.getId());
        assertNotNull(forks);
        assertFalse(forks.isEmpty());
        assertEquals(newNodeTitle, forks.get(0).getChildNode().getTitle());
        assertEquals(initialNodeTitle, forks.get(0).getParentNode().getTitle());

        assertEquals("""
                Попрыгунья Стрекоза
                Лето красное пропела;
                Оглянуться не успела,
                Как зима катит в глаза.
                """,
                parentNode.getContent().getText() + childNode.getContent().getText());
    }

    private MultiNode findByIdAndAssertSingleNode(UUID id, String nodeTitle, String contentText) {
        var nodeFromRepo = nodeManager.findById(id.toString());
        assertTrue(nodeFromRepo.isPresent());
        MultiNode node = nodeFromRepo.get();
        assertEquals(nodeTitle, node.getTitle());
        assertEquals(contentText, node.getContent().getText());
        return node;
    }

    private MultiNode findByTitleAndAssertSingleNode(String title) {
        var nodes = nodeManager.findNodesByTitle(title);
        assertFalse(nodes.isEmpty());
        MultiNode node = nodes.get(0);
        assertNotNull(node.getContent());
        assertEquals(title, node.getTitle());
        return node;
    }
}
