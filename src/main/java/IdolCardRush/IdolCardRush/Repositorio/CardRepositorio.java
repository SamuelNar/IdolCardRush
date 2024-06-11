package IdolCardRush.IdolCardRush.Repositorio;

import IdolCardRush.IdolCardRush.Entidades.Carta;
import IdolCardRush.IdolCardRush.Entidades.Usuario;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepositorio extends JpaRepository<Carta, Long> {

    boolean existsByCode(String code);

    Carta findByCode(String code);

    List<Carta> findByUserIsNull();

    List<Carta> findByUser(Usuario user);

    List<Carta> findByUserIsNullAndFountain(String fountain);

    List<Carta> findByFountain(String fountain);

    boolean existsByIdentifirer(String identificadorUnico);

    @Query("SELECT c FROM Carta c WHERE c.user.id IS NULL AND c.fountain = :fountain")
    List<Carta> findByUserIsNullAndFountainFetchAll(@Param("fountain") String fountain);

    Carta findByIdentifirer(String identifirer);

    long countByIdentifirer(String identifirer);

    @Query("SELECT c FROM Carta c WHERE (c.id, c.idolName, SUBSTRING_INDEX(c.code, '.', 1)) IN (SELECT MIN(c2.id), c2.idolName, SUBSTRING_INDEX(c2.code, '.', 1) FROM Carta c2 GROUP BY c2.idolName, SUBSTRING_INDEX(c2.code, '.', 1))")
    Page<Carta> findAllUniqueCards(Pageable pageable);

    @Query("SELECT c FROM Carta c GROUP BY c.idolName, SUBSTRING(c.code, 1, LOCATE('.', c.code) - 1)")
    Page<Carta> findUniqueCartas(Pageable pageable);

    Page<Carta> findAll(Specification<Carta> spec, Pageable pageable);

    // CartaRepositorio.java
    Page<Carta> findByIdolNameContainingIgnoreCase(String idolName, Pageable pageable);

    Page<Carta> findByEraContainingIgnoreCase(String era, Pageable pageable);

    Page<Carta> findByGroupKpopContainingIgnoreCase(String groupKpop, Pageable pageable);

    Page<Carta> findByIdolNameContainingIgnoreCaseAndEraContainingIgnoreCase(String idolName, String era,
            Pageable pageable);

    Page<Carta> findByIdolNameContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(String idolName, String groupKpop,
            Pageable pageable);

    Page<Carta> findByEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(String era, String groupKpop,
            Pageable pageable);

    Page<Carta> findByIdolNameContainingIgnoreCaseAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(
            String idolName, String era, String groupKpop, Pageable pageable);

    Page<Carta> findByUser(Usuario user, Pageable pageable);

    Page<Carta> findByUserAndIdolNameContainingIgnoreCase(Usuario usuario, String idolName, Pageable pageable);

    Page<Carta> findByUserAndEraContainingIgnoreCase(Usuario usuario, String era, Pageable pageable);

    Page<Carta> findByUserAndGroupKpopContainingIgnoreCase(Usuario usuario, String groupKpop, Pageable pageable);

    Page<Carta> findByUserAndIdolNameContainingIgnoreCaseAndEraContainingIgnoreCase(Usuario usuario, String idolName,
            String era, Pageable pageable);

    Page<Carta> findByUserAndIdolNameContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(Usuario usuario,
            String idolName, String groupKpop, Pageable pageable);

    Page<Carta> findByUserAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(Usuario usuario, String era,
            String groupKpop, Pageable pageable);

    Page<Carta> findByUserAndIdolNameContainingIgnoreCaseAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(
            Usuario usuario, String idolName, String era, String groupKpop, Pageable pageable);
}
