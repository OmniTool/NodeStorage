package hex.multinode.storage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "hex.multinode.storage.repository.db")
@PropertySource("classpath:/persistence-test.properties")
@EnableTransactionManagement
public class H2JpaConfig {

}
