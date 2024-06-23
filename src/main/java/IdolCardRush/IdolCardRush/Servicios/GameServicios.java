package IdolCardRush.IdolCardRush.Servicios;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import IdolCardRush.IdolCardRush.Entidades.Carta;
import IdolCardRush.IdolCardRush.Entidades.Usuario;
import IdolCardRush.IdolCardRush.Repositorio.UserRepositorio;


@Service
public class GameServicios {
    @Autowired
    UserRepositorio userRepositorio;
    @Autowired
    CardServicios cardServicios;
    public List<Map<String, Object>> obtenerCartasJuego(String nomArchivo) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(nomArchivo);
        return mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
        });
    }


    public List<Map<String, Object>> obtenerPreguntasJuego(String nomArchivo) throws Exception {
        try {
            List<Map<String, Object>> preguntas = obtenerCartasJuego(nomArchivo);
            List<Map<String, Object>> listarPreguntas = new ArrayList<>();
            for (Map<String, Object> map : preguntas) {
                String preguntaText = (String) map.get("pregunta");
                @SuppressWarnings("unchecked")
                List<String> opciones = (List<String>) map.get("opciones");
                String respuestaCorrecta = (String) map.get("respuesta_correcta");
                Map<String, Object> preguntaKpop = new HashMap<>();
                preguntaKpop.put("pregunta", preguntaText);
                preguntaKpop.put("opciones", opciones);
                preguntaKpop.put("respuesta_correcta", respuestaCorrecta);
                listarPreguntas.add(preguntaKpop);
            }
            return listarPreguntas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void trabajo (Usuario user, List<Carta> cartas){
        Random random = new Random();
        cartas = cardServicios.obtenerCartasPorDebut(user);
        if (cartas.size() == 0){
            System.out.println("Necesita debutar una carta primero");
        }else{
            int moneyWork = random.nextInt(11) + 50;  
        int UserMoney = user.getMoney();
        user.setMoney(UserMoney + moneyWork);
        userRepositorio.save(user);       
        }        
    }

}
