package hex.multinode.storage.service;

import hex.multinode.storage.NodeStorageApplication;
import hex.multinode.storage.config.H2JpaTestConfig;
import hex.multinode.storage.model.data.MultiContent;
import hex.multinode.storage.model.data.MultiLink;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;
import hex.multinode.storage.model.generator.UUIDV7Generator;
import hex.multinode.storage.repository.db.LinkDBRepository;
import hex.multinode.storage.repository.db.NodeDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {NodeStorageApplication.class, H2JpaTestConfig.class})
public class NodeManagerTest {

    private final NodeManager<MultiNode> nodeManager;
    private final NodeDBRepository nodeRepository;
    private final LinkDBRepository linkRepository;

    private final String initialNodeTitle = "Стрекоза и муравей";
    private final String initialContentText = """
                        Попрыгунья Стрекоза
                        Лето красное пропела;
                        """;

    @Autowired
    public NodeManagerTest(NodeManager nodeManager,
                           NodeDBRepository nodeRepository,
                           LinkDBRepository linkRepository) {
        this.nodeManager = nodeManager;
        this.nodeRepository = nodeRepository;
        this.linkRepository = linkRepository;
    }

    @BeforeEach
    public void initCase() {
        linkRepository.deleteAll();
        nodeRepository.deleteAll();
        MultiNode node = new MultiNode(initialNodeTitle);
        node.setContent(
                new MultiContent(initialContentText)
        );
        nodeRepository.save(node);
    }

    @Test
    public void forkByDTO() {
        String answer = "some answer";
        var parentNode = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newNodeTitle = initialNodeTitle + "[1]";
        String newContentText = """
                Оглянуться не успела,
                Как зима катит в глаза.
                """;
        NodeDTO childNodeDTO = NodeDTO.of(newNodeTitle, newContentText);

        parentNode = nodeManager.fork(parentNode.getId().toString(), childNodeDTO, answer);
        assertNotNull(parentNode);

        assertLinks(initialNodeTitle, newNodeTitle, answer);

        var childNode = findByTitleAndAssertSingleNode(newNodeTitle);
        assertEquals("""
                Попрыгунья Стрекоза
                Лето красное пропела;
                Оглянуться не успела,
                Как зима катит в глаза.
                """,
                parentNode.getContent().getText() + childNode.getContent().getText());
    }

    @Test
    public void forkByExistingNode() {
        String answer = "some answer";
        MultiNode parentNode = findByTitleAndAssertSingleNode(initialNodeTitle);
        String newNodeTitle = initialNodeTitle + "[1]";
        String newContentText = """
                Оглянуться не успела,
                Как зима катит в глаза.
                """;
        MultiNode childNode = nodeManager.save(NodeDTO.of(newNodeTitle, newContentText));

        parentNode = nodeManager.fork(parentNode.getId().toString(), childNode.getId().toString(), answer);
        assertNotNull(parentNode);

        assertLinks(initialNodeTitle, newNodeTitle, answer);
    }

    private void assertLinks(String parentNodeTitle, String childNodeTitle, String answer) {
        var childNode = findByTitleAndAssertSingleNode(childNodeTitle);
        var roots = linkRepository.findLinksByChildNodeId(childNode.getId());
        assertLinkData(roots, parentNodeTitle, childNodeTitle, answer);

        var parentNode = findByTitleAndAssertSingleNode(parentNodeTitle);
        var forks = linkRepository.findLinksByParentNodeId(parentNode.getId());
        assertLinkData(forks, parentNodeTitle, childNodeTitle, answer);
    }

    private void assertLinkData(List<MultiLink> links, String parentNodeTitle, String childNodeTitle, String answer) {
        var link = links.get(0);
        assertEquals(childNodeTitle, link.getChildNode().getTitle());
        assertEquals(parentNodeTitle, link.getParentNode().getTitle());
        assertEquals(answer, link.getChoiceText());
    }


    @Test
    public void createFromDTO() {
        String newNodeTitle = "Ворона и лисица";
        String newContentText = """
                        Вороне где-то бог послал кусочек сыру;
                        """;
        NodeDTO nodeDTO = NodeDTO.of(newNodeTitle, newContentText);
        MultiNode savedNode = nodeManager.save(nodeDTO);
        assertNotNull(savedNode);
        assertNotNull(savedNode.getId());
        findByIdAndAssertSingleNode(savedNode.getId(), newNodeTitle, newContentText);
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
