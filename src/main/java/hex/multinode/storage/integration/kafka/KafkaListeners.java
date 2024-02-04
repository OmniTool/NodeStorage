package hex.multinode.storage.integration.kafka;

import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;

import hex.multinode.storage.service.NodeManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class KafkaListeners {

    private final NodeManager<MultiNode> nodeManager;

    @Autowired
    public KafkaListeners(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @KafkaListener(
            topics = "${spring.kafka.consumer.topic}",
            groupId = "${spring.kafka.consumer.group}",
            containerFactory = "kafkaNodeConsumerFactory"
    )
    void onMessageUpdate(NodeDTO data) {
        log.debug("KafkaListeners received: " + data);
        nodeManager.update(data);
    }
}
