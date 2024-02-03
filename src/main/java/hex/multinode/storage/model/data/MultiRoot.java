package hex.multinode.storage.model.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "multi_root")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiRoot {
    @Id
//    @UuidGenerator
    @GenericGenerator(name = "UUID_v7_id", strategy = "hex.multinode.storage.model.generator.UUIDV7Generator")
    @GeneratedValue(generator = "UUID_v7_id")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "child_node_id", nullable = false)
    private MultiNode childNode;

    @OneToOne
    @JoinColumn(name = "parent_node_id", nullable = false)
    private MultiNode parentNode;

    public MultiRoot(MultiNode childNode, MultiNode parentNode) {
        this.childNode = childNode;
        this.parentNode = parentNode;
    }

}
