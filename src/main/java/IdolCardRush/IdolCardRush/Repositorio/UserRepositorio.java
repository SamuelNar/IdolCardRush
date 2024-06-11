package IdolCardRush.IdolCardRush.Repositorio;

import IdolCardRush.IdolCardRush.Entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositorio extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String name);

    @Query("Select u FROM Usuario u WHERE u.name = :name")
    public Usuario buscarPorUsuario(@Param("name") String name);
}
