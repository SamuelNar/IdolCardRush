package IdolCardRush.IdolCardRush.Entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idolName;
    private String era;
    private String groupKpop;
    private int rarity;
    private String code;
    private String urlImage;
    private String fountain;
    private String identifirer;
    private Boolean isDebut;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Usuario user;

}
