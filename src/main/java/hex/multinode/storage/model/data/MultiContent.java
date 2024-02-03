package hex.multinode.storage.model.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "multi_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiContent {
    @Id
    @GenericGenerator(name = "UUID_v7_id", strategy = "hex.multinode.storage.model.generator.UUIDV7Generator")
    @GeneratedValue(generator = "UUID_v7_id")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "text", length = 1000)
    private String text;

    @Column(name = "illustration_loc")
    private String illustrationLoc;

//    private Animation animationLoc;
//    private Audio audioLoc;
//    private Video videoLoc;

    public MultiContent(String text) {
        this.text = text;
    }

    public MultiContent(String text, String illustrationLoc) {
        this.text = text;
        this.illustrationLoc = illustrationLoc;
    }

}
