package hex.multinode.storage.model.data;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "multi_node")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiNode {
    @Id
    @GenericGenerator(name = "UUID_v7_id", strategy = "hex.multinode.storage.model.generator.UUIDV7Generator")
    @GeneratedValue(generator = "UUID_v7_id")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "childNode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MultiRoot> roots;

    @OneToMany(mappedBy = "parentNode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MultiFork> forks;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "content_id")
    private MultiContent content;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    public MultiNode(String title) {
        this.title = title;
    }

    public MultiNode(String title, MultiContent content) {
        this.title = title;
        this.content = content;
    }

    private List<MultiRoot> getRoots() {
        return roots;
    }

    private List<MultiFork> getForks() {
        return forks;
    }
}
