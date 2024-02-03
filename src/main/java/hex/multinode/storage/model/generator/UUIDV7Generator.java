package hex.multinode.storage.model.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import com.fasterxml.uuid.Generators;

import java.util.UUID;

public class UUIDV7Generator implements IdentifierGenerator {

    @Override
    public UUID generate(SharedSessionContractImplementor var1, Object var2) {
        return generateUuidV7();
    }

    public static UUID generateUuidV7() {
        return Generators.timeBasedEpochGenerator().generate();
    }

}
