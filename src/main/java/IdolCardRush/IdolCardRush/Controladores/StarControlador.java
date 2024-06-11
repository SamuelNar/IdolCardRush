package IdolCardRush.IdolCardRush.Controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import IdolCardRush.IdolCardRush.Entidades.Usuario;
import IdolCardRush.IdolCardRush.Servicios.UserServicios;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/")
public class StarControlador {

    @Autowired
    UserServicios userServicios;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/registrar")
    public String registrar() {
        return "Registro.html";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String name, @RequestParam String email, @RequestParam String password,
            @RequestParam String password2, ModelMap modelo) {
        try {
            userServicios.Registrar(name, email, password, password2);
            modelo.put("exito", "Usuario registrado con exito");
            return "index.html";
        } catch (Exception e) {
            modelo.put("error", e.getMessage());
            modelo.put("name", name);
        }
        return "Registro.html";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "Usuario invalido o contraseña incorrecta");
        }
        return "Login.html";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuariosession");
        if (user.getRol().toString().equals("ADMIN")) {
            return "redirect:/admin";
        } else if (user.getRol().toString().equals("USER")) {
            return "redirect:/user/panel";
        }
        return "index.html";
    }

}
