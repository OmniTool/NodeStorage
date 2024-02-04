package hex.multinode.storage.model.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "multi_link")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiLink {
    @Id
    @GenericGenerator(name = "UUID_v7_id", strategy = "hex.multinode.storage.model.generator.UUIDV7Generator")
    @GeneratedValue(generator = "UUID_v7_id")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "choice_text", nullable = false)
    private String choiceText;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_node_id", nullable = false)
    private MultiNode parentNode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "child_node_id", nullable = false)
    private MultiNode childNode;

    public MultiLink(MultiNode parentNode, MultiNode childNode, String choiceText) {
        this.parentNode = parentNode;
        this.childNode = childNode;
        this.choiceText = choiceText;
    }

}
