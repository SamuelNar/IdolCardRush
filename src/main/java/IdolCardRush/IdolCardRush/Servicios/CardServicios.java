package IdolCardRush.IdolCardRush.Servicios;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import IdolCardRush.IdolCardRush.Entidades.Carta;
import IdolCardRush.IdolCardRush.Entidades.Usuario;
import IdolCardRush.IdolCardRush.Repositorio.CardRepositorio;

@Service
public class CardServicios {

    @Autowired
    private CardRepositorio cartaRepositorio;

    public CardServicios(CardRepositorio cartaRepositorio) {
        this.cartaRepositorio = cartaRepositorio;
    }

    private static final Logger logger = LoggerFactory.getLogger(CardServicios.class);

    @Transactional
    public Carta crearCartaDesdeJSON(String File, String fountain)
            throws StreamReadException, DatabindException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/Json/Card.json");
            List<Carta> cartas = mapper.readValue(file, new TypeReference<List<Carta>>() {
            });
            List<Carta> cartasDelFountain = cartas.stream()
                    .filter(carta -> carta.getFountain().equals(fountain))
                    .collect(Collectors.toList());

            if (!cartasDelFountain.isEmpty()) {
                int randomIndex = (int) (Math.random() * cartasDelFountain.size());
                Carta cartaAleatoria = cartasDelFountain.get(randomIndex);
                Carta nuevaCarta = new Carta();
                nuevaCarta.setIdolName(cartaAleatoria.getIdolName());
                nuevaCarta.setEra(cartaAleatoria.getEra());
                nuevaCarta.setGroupKpop(cartaAleatoria.getGroupKpop());
                nuevaCarta.setRarity(cartaAleatoria.getRarity());
                nuevaCarta.setUrlImage(cartaAleatoria.getUrlImage());
                nuevaCarta.setCode(generarVarianteCartas(cartaAleatoria));
                nuevaCarta.setFountain(cartaAleatoria.getFountain());
                nuevaCarta.setUser(null);
                nuevaCarta.setIdentifirer(generarIdentificadorCarta(cartaAleatoria));
                cartaRepositorio.save(nuevaCarta);
                return nuevaCarta;
            } else {
                return null;
            }
        } catch (StreamReadException e) {
            // Maneja la excepción StreamReadException
            logger.error("Error al leer el archivo JSON: " + e.getMessage());
            return null;
        } catch (DatabindException e) {
            // Maneja la excepción DatabindException
            logger.error("Error al deserializar el archivo JSON: " + e.getMessage());
            return null;
        } catch (IOException e) {
            // Maneja la excepción IOException
            logger.error("Error al leer el archivo JSON: " + e.getMessage());
            return null;
        }

    }

    private String generarVarianteCartas(Carta carta) {
        String codeVariant = carta.getCode() + "." + RandomStringUtils.randomAlphabetic(4);
        return codeVariant;
    }
    private String generarIdentificadorCarta(Carta carta) {
        String idolName = carta.getIdolName().replaceAll("[\\s']+", "");
        String era = carta.getEra().replaceAll("[\\s']+", "");
        String groupKpop = carta.getGroupKpop().replaceAll("[\\s']+", "");
        String fountain = carta.getFountain().replaceAll("[\\s']+", "");
        Integer rarity = carta.getRarity();
        return idolName + "_" + era + "_" + groupKpop + "_" + fountain + "_" + rarity;
    }

    public Page<Carta> obtenerTodasLasCartas(Pageable pageable) {
        return // cartaRepositorio.findUniqueCards(pageable);
        cartaRepositorio.findAllUniqueCards(pageable);
        // cartaRepositorio.findAll(pageable);
    }

    public Page<Carta> obtenerCartasPorFiltroName(String idolName, Pageable pageable) {
        return cartaRepositorio.findByIdolNameContainingIgnoreCase(idolName, pageable);
    }

    public Page<Carta> obtenerCartasPorFiltroEra(String era, Pageable pageable) {
        return cartaRepositorio.findByEraContainingIgnoreCase(era, pageable);
    }

    public Page<Carta> obtenerCartasPorFiltroGrupo(String groupKpop, Pageable pageable) {
        return cartaRepositorio.findByGroupKpopContainingIgnoreCase(groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorFiltrosNameEra(String idolName, String era, Pageable pageable) {
        return cartaRepositorio.findByIdolNameContainingIgnoreCaseAndEraContainingIgnoreCase(idolName, era, pageable);
    }

    public Page<Carta> obtenerCartasPorFiltrosNameGrupo(String idolName, String groupKpop, Pageable pageable) {
        return cartaRepositorio.findByIdolNameContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(idolName, groupKpop,
                pageable);
    }

    public Page<Carta> obtenerCartasPorFiltrosEraGrupo(String era, String groupKpop, Pageable pageable) {
        return cartaRepositorio.findByEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(era, groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorFiltrosNameEraGrupo(String idolName, String era, String groupKpop,
            Pageable pageable) {
        return cartaRepositorio
                .findByIdolNameContainingIgnoreCaseAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(idolName,
                        era, groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltroidolName(Usuario usuario, String idolName, Pageable pageable) {
        return cartaRepositorio.findByUserAndIdolNameContainingIgnoreCase(usuario, idolName, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltroEra(Usuario usuario, String era, Pageable pageable) {
        return cartaRepositorio.findByUserAndEraContainingIgnoreCase(usuario, era, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltroGroup(Usuario usuario, String groupKpop, Pageable pageable) {
        return cartaRepositorio.findByUserAndGroupKpopContainingIgnoreCase(usuario, groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltrosNameEra(Usuario usuario, String idolName, String era,
            Pageable pageable) {
        return cartaRepositorio.findByUserAndIdolNameContainingIgnoreCaseAndEraContainingIgnoreCase(usuario, idolName,
                era, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltrosNameGroup(Usuario usuario, String idolName, String groupKpop,
            Pageable pageable) {
        return cartaRepositorio.findByUserAndIdolNameContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(usuario,
                idolName, groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltrosEraGroup(Usuario usuario, String era, String groupKpop,
            Pageable pageable) {
        return cartaRepositorio.findByUserAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(usuario, era,
                groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuarioYFiltrosNameEraGroup(Usuario usuario, String idolName, String era,
            String groupKpop, Pageable pageable) {
        return cartaRepositorio
                .findByUserAndIdolNameContainingIgnoreCaseAndEraContainingIgnoreCaseAndGroupKpopContainingIgnoreCase(
                        usuario, idolName, era, groupKpop, pageable);
    }

    public Page<Carta> obtenerCartasPorUsuario(Usuario usuario, Pageable pageable) {
        return cartaRepositorio.findByUser(usuario, pageable);
    }

    public Page<Carta> obtenerTodasLasCartass(Pageable pageable) {
        return cartaRepositorio.findUniqueCartas(pageable);
    }

    public Carta obtenerCartaAleatoria() {
        List<Carta> cartasDisponibles = cartaRepositorio.findByUserIsNull();
        if (!cartasDisponibles.isEmpty()) {
            long totalDeCartas = cartasDisponibles.size();
            long indiceAleatorio = (long) (Math.random() * totalDeCartas);
            return cartasDisponibles.get((int) indiceAleatorio);
        } else {
            return null;
        }
    }

    public Carta obtenerCartaAleatoriaFontain(String fountain) {
        List<Carta> cartasDisponibles = cartaRepositorio.findByUserIsNullAndFountain(fountain); // Trae por usuario =
                                                                                                // null y por fountain      
        Hibernate.initialize(cartasDisponibles);
        if (!cartasDisponibles.isEmpty()) {
            long totalDeCartas = cartasDisponibles.size();
            long indiceAleatorio = (long) (Math.random() * totalDeCartas);
            return cartasDisponibles.get((int) indiceAleatorio);
        } else {
            return null;
        }
    }
    @Transactional
    public List<Carta> obtenerCartasPorUsuario(Usuario user) {
        List<Carta> cartas = cartaRepositorio.findByUser(user);
        return cartas;
    }
}
