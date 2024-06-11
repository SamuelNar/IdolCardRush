package IdolCardRush.IdolCardRush.Servicios;

import IdolCardRush.IdolCardRush.Entidades.Carta;
import IdolCardRush.IdolCardRush.Entidades.Usuario;
import IdolCardRush.IdolCardRush.Enumerador.Rol;
import IdolCardRush.IdolCardRush.Repositorio.UserRepositorio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserServicios implements UserDetailsService {
    @Autowired
    UserRepositorio userRepositorio;
    @Autowired
    CardServicios cardServicios;
    @Autowired
    GameServicios gameServicios;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void Registrar(String username, String email, String password, String password2) {
        validar(username, email,password, password2);
        Usuario user = new Usuario();
        user.setName(username);
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setRol(Rol.USER);
        user.setCard(null);
        user.setMoney(0);
        userRepositorio.save(user);
    }
    private static final Logger logger = LoggerFactory.getLogger(UserServicios.class);
    @Transactional
    public void asignarCarta(Usuario user, Carta carta) {
        user = userRepositorio.findById(user.getId()).orElse(null);
        if (user != null && carta != null) {
            carta.setUser(user);
            user.getCard().add(carta);
            userRepositorio.save(user);
        } else {
            try {
                throw new NoSuchElementException("Usuario no encontrado");
            } catch (NoSuchElementException e) {
                // Maneja la excepción NoSuchElementException
               logger.error("Error al asignar la carta al usuario: " + e.getMessage());
            }
        }
    }
    public Usuario findById(Long id) {
        return userRepositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public void validar(String username,String email, String password, String password2) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede ser vacio");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede ser vacio");
        }
        if (password == null || (password.length() <= 5)) {
            throw new IllegalArgumentException("La contraseña no puede ser vacia y debe tener mas de 5 caracteres");
        }
        if (!password.equals(password2)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario user = userRepositorio.findByEmail(email);
        try {
            if (user != null) {
                List<GrantedAuthority> permisos = new ArrayList<>();
                GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + user.getRol().toString());
                permisos.add(p);
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();
                HttpSession session = attr.getRequest().getSession(true);
                session.setAttribute("usuariosession", user);
                return new User(user.getEmail(), user.getPassword(), permisos);
            } else {
                System.out.println("error");
                return null;
            }
        } catch (Exception e) {
            System.out.println("error iniciar");
            return null;
        }
    }

}
