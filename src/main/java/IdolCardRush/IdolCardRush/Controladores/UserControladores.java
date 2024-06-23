package IdolCardRush.IdolCardRush.Controladores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import IdolCardRush.IdolCardRush.Entidades.Carta;
import IdolCardRush.IdolCardRush.Entidades.Usuario;
import IdolCardRush.IdolCardRush.Servicios.CardServicios;
import IdolCardRush.IdolCardRush.Servicios.GameServicios;
import IdolCardRush.IdolCardRush.Servicios.UserServicios;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserControladores {

    private static final Logger logger = LoggerFactory.getLogger(UserControladores.class);
    private static final int COOLDOWN_MINUTES = 2;
    @Autowired
    private CardServicios cardServicios;
    @Autowired
    private UserServicios userServicios;
    @Autowired
    private GameServicios gameServicios;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/panel")
    @Transactional
    public String registrarCarta(Model modelo, HttpSession session) {
        return "Usuario.html";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/asignarCarta")
    @Transactional
    public String asignarCarta(HttpSession session, Model modelo, RedirectAttributes redirectAttributes) {
        return "DropCartas.html";
    }
    
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/dropearCarta")
    public String DropCarta(HttpSession session, @RequestParam("fountain") String fountain, Model model) {
        Usuario user = traerUsuarioLogueado(session);
        LocalDateTime lastDropTime = (LocalDateTime) session.getAttribute("lastDropTime");
        LocalDateTime now = LocalDateTime.now();
        if (lastDropTime != null) {
            Duration duration = Duration.between(lastDropTime, now);
            long minutesSinceLastDrop = duration.toMinutes();
    
            if (minutesSinceLastDrop < COOLDOWN_MINUTES) {
                long minutesLeft = COOLDOWN_MINUTES - minutesSinceLastDrop;
                model.addAttribute("Error", "Debes esperar " + minutesLeft + " minutos antes de dropear otra carta.");
                return "DropCartas.html"; // O la vista que prefieras mostrar
            }
        }
        try {
            Carta cartaAsignada = cardServicios.crearCartaDesdeJSON("src/main/resources/Json/Card.json", fountain);
            if (cartaAsignada != null) {
                userServicios.asignarCarta(user, cartaAsignada);
                model.addAttribute("cartaAsignada", cartaAsignada);
                 session.setAttribute("lastDropTime", now);
            } else {
                model.addAttribute("Error", "Carta no encontrada");
            }
        } catch (Exception e) {
            model.addAttribute("Error", "Error al cargar la carta: " + e.getMessage());
            logger.error("Error al cargar la carta: " + e.getMessage());
        }
        return "DropCartas.html";
    }

    private Usuario traerUsuarioLogueado(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuariosession");
        return user;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/preguntas")
    @Transactional
    public String Preguntas(HttpSession session, Model modelo) throws Exception {
        try {
            List<Map<String, Object>> preguntas = gameServicios
                    .obtenerPreguntasJuego("src/main/resources/Json/Quest.json");
            Random random = new Random();
            int indice = random.nextInt(preguntas.size());
            Map<String, Object> preguntaRandom = preguntas.get(indice);
            session.setAttribute("pregunta", preguntaRandom);
            modelo.addAttribute("preguntas", preguntaRandom);
            return "preguntas.html";
        } catch (Exception e) {
            return null;
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/work")
    public String work(HttpSession session, Model modelo, RedirectAttributes redirectAttributes) {
        Usuario user = traerUsuarioLogueado(session);
        List<Carta> carta = userServicios.obtenerCartas(user);    
        gameServicios.trabajo(user,carta);
        modelo.addAttribute("user", user);
        return "redirect:/user/panel";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/verificarRespuesta")
    @Transactional
    // @RequestParam("opcionElegida")String opcionElegida,HttpSession session,Model
    // modelo
    public String verificarRespuesta(@RequestParam("opcionElegida") String opcionElegida, HttpSession session,
            Model modelo) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> preguntas = (Map<String, Object>) session.getAttribute("pregunta");
            String respuestaCorrecta = (String) preguntas.get("respuesta_correcta");
            System.out.println(respuestaCorrecta);
            boolean esCorrecta = opcionElegida.equals(respuestaCorrecta);
            modelo.addAttribute("esCorrecta", esCorrecta);
            modelo.addAttribute("respuestaCorrecta", respuestaCorrecta);
            return "verificarRespuesta.html";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error en el controlador");
            return null;
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/cartas")
    public String cartas(Model modelo, @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "idolName", required = false) String idolName,
            @RequestParam(name = "era", required = false) String era,
            @RequestParam(name = "groupKpop", required = false) String groupKpop) {
        PageRequest pageRequest = PageRequest.of(page, 10);
        Page<Carta> cartasPage;

        if (idolName != null && !idolName.isEmpty()) {
            if (era != null && !era.isEmpty()) {
                if (groupKpop != null && !groupKpop.isEmpty()) {
                    cartasPage = cardServicios.obtenerCartasPorFiltrosNameEraGrupo(idolName, era, groupKpop,
                            pageRequest);
                } else {
                    cartasPage = cardServicios.obtenerCartasPorFiltrosNameEra(idolName, era, pageRequest);
                }
            } else if (groupKpop != null && !groupKpop.isEmpty()) {
                cartasPage = cardServicios.obtenerCartasPorFiltrosNameGrupo(idolName, groupKpop, pageRequest);
            } else {
                cartasPage = cardServicios.obtenerCartasPorFiltroName(idolName, pageRequest);
            }
        } else if (era != null && !era.isEmpty()) {
            if (groupKpop != null && !groupKpop.isEmpty()) {
                cartasPage = cardServicios.obtenerCartasPorFiltrosEraGrupo(era, groupKpop, pageRequest);
            } else {
                cartasPage = cardServicios.obtenerCartasPorFiltroEra(era, pageRequest);
            }
        } else if (groupKpop != null && !groupKpop.isEmpty()) {
            cartasPage = cardServicios.obtenerCartasPorFiltroGrupo(groupKpop, pageRequest);
        } else {
            cartasPage = cardServicios.obtenerTodasLasCartass(pageRequest);
        }

        modelo.addAttribute("cartas", cartasPage.getContent());
        modelo.addAttribute("currentPage", page);
        modelo.addAttribute("totalPages", cartasPage.getTotalPages());
        modelo.addAttribute("baseUrl", "/user/cartas");
        modelo.addAttribute("idolName", idolName);
        modelo.addAttribute("era", era);
        modelo.addAttribute("groupKpop", groupKpop);

        return "Cartas.html";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/perfil/{id}")
    public String perfil(Model modelo, @PathVariable Long id, HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "idolName", required = false) String idolName,
            @RequestParam(name = "era", required = false) String era,
            @RequestParam(name = "groupKpop", required = false) String groupKpop) {
        Usuario user = (Usuario) session.getAttribute("usuariosession");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Usuario userPerfil = userServicios.findById(id);
            modelo.addAttribute("userPerfil", userPerfil);
            PageRequest pageRequest = PageRequest.of(page, 10);
            Page<Carta> userCardPage;
            userCardPage = cardServicios.obtenerCartasPorUsuario(userPerfil, pageRequest);
            modelo.addAttribute("userCard", userCardPage);
            modelo.addAttribute("currentPage", page);
            modelo.addAttribute("totalPages", userCardPage.getTotalPages());
            modelo.addAttribute("baseUrl", "/user/perfil/" + id);
            modelo.addAttribute("idolName", idolName);
            modelo.addAttribute("era", era);
            modelo.addAttribute("groupKpop", groupKpop);
            return "Perfil.html";
        } catch (Exception e) {
            e.printStackTrace();
            modelo.addAttribute("error", e.getMessage());
            return "error.html";
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/perfil/filtrar")
    public String filtrarPerfil(Model modelo, HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "idolName", required = false) String idolName,
            @RequestParam(name = "era", required = false) String era,
            @RequestParam(name = "groupKpop", required = false) String groupKpop) {
        Usuario user = (Usuario) session.getAttribute("usuariosession");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            PageRequest pageRequest = PageRequest.of(page, 10);
            Page<Carta> userCardPage;
            Usuario userPerfil = userServicios.findById(user.getId());
            modelo.addAttribute("userPerfil", userPerfil);
            if (idolName != null && !idolName.isEmpty()) {
                if (era != null && !era.isEmpty()) {
                    if (groupKpop != null && !groupKpop.isEmpty()) {
                        userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltrosNameEraGroup(userPerfil, idolName,
                                era, groupKpop, pageRequest);
                    } else {
                        userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltrosNameEra(userPerfil, idolName, era,
                                pageRequest);
                    }
                } else if (groupKpop != null && !groupKpop.isEmpty()) {
                    userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltrosNameGroup(userPerfil, idolName,
                            groupKpop, pageRequest);
                } else {
                    userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltroidolName(userPerfil, idolName,
                            pageRequest);
                }
            } else if (era != null && !era.isEmpty()) {
                if (groupKpop != null && !groupKpop.isEmpty()) {
                    userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltrosEraGroup(userPerfil, era, groupKpop,
                            pageRequest);
                } else {
                    userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltroEra(userPerfil, era, pageRequest);
                }
            } else if (groupKpop != null && !groupKpop.isEmpty()) {
                userCardPage = cardServicios.obtenerCartasPorUsuarioYFiltroGroup(userPerfil, groupKpop, pageRequest);
            } else {
                userCardPage = cardServicios.obtenerCartasPorUsuario(userPerfil, pageRequest);
            }
            modelo.addAttribute("userCard", userCardPage.getContent());
            modelo.addAttribute("currentPage", page);
            modelo.addAttribute("totalPages", userCardPage.getTotalPages());
            modelo.addAttribute("baseUrl", "/user/perfil/filtrar");
            modelo.addAttribute("idolName", idolName);
            modelo.addAttribute("era", era);
            modelo.addAttribute("groupKpop", groupKpop);
            return "PerfilFiltrado.html";
        } catch (Exception e) {
            e.printStackTrace();
            modelo.addAttribute("error", e.getMessage());
            return "error.html";
        }
    }

    @GetMapping("/perfil/limpiar")
    public String limpiarFiltros(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuariosession");
        // Eliminar los parámetros de filtro de la sesión
        session.removeAttribute("idolName");
        session.removeAttribute("era");
        session.removeAttribute("groupKpop");
        // Redirigir de vuelta a la página de perfil filtrado sin filtros aplicados
        return "redirect:/user/perfil/" + user.getId();
    }

    @GetMapping("/debutar")
    public String debut (HttpSession session, Model modelo, RedirectAttributes redirectAttributes){    
        Usuario user = traerUsuarioLogueado(session);
        List<Carta> cartas = userServicios.obtenerCartas(user);
        modelo.addAttribute("cartas", cartas);
        return "elegirCarta.html";
    }

    @PostMapping("/debutar")
    public String debutar(HttpSession session, Model modelo, RedirectAttributes redirectAttributes, @RequestParam("id") Long id){
        Usuario user = traerUsuarioLogueado(session);
        Carta carta = cardServicios.obtenerCartaPorId(id);        
        if (carta != null && carta.getUser().getId().equals(user.getId())){
            userServicios.debutarCarta(user, carta);
            carta.setIsDebut(!carta.getIsDebut());
            cardServicios.guardarCarta(carta);
            redirectAttributes.addFlashAttribute("Exito", "La carta ha debutodo con exito");
        }else{
            redirectAttributes.addFlashAttribute("Error", "La carta no ha debutado");
        }        
        return "redirect:/user/panel";
    }
    
}
