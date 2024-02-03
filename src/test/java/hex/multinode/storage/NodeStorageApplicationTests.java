package hex.multinode.storage;

import hex.multinode.storage.config.H2JpaTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {NodeStorageApplication.class, H2JpaTestConfig.class})
class NodeStorageApplicationTests {

    @Test
    void contextLoads() {
    }

}
