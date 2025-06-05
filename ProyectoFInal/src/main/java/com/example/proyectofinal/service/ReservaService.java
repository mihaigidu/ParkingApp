package com.example.proyectofinal.service;

import com.example.proyectofinal.model.Reserva;
import com.example.proyectofinal.model.User;
import com.example.proyectofinal.repository.ReservaRepository;
import com.example.proyectofinal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private  ReservaRepository reservaRepository;

    @Autowired
    private  UserRepository userRepository;

    public ReservaService(ReservaRepository reservaRepository, UserRepository userRepository) {
        this.reservaRepository = reservaRepository;
        this.userRepository = userRepository;
    }

    public List<Reserva> obtenerReservasDelUsuario() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        LocalDateTime ahora = LocalDateTime.now();

        return reservaRepository.findByUsuarioUsername(username)
                .stream()
                .filter(r ->
                        r.getFechaReserva() != null &&
                                r.getFechaReserva()
                                        .plusMinutes(r.getTiempo())
                                        .isAfter(ahora)
                )
                .collect(Collectors.toList());
    }


    public void guardarReserva(Reserva reserva) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        System.out.println("📝 Intentando guardar reserva para usuario: " + username);

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty() && auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
            String email = oauthUser.getAttribute("email");
            System.out.println("🔍 Usuario Google detectado, buscando por email: " + email);
            optionalUser = userRepository.findByEmail(email);
        }

        User user = optionalUser.orElseThrow(() -> new RuntimeException("Usuario no encontrado con username/email"));

        reserva.setUsuario(user);
        reserva.setFechaReserva(LocalDateTime.now());
        reservaRepository.save(reserva);
    }



    public Reserva getReservaById(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        return reserva.orElse(null); // Devolver null si no se encuentra la reserva
    }

    public List<Reserva> buscarPorMatricula(String matricula) {
        return reservaRepository.findByMatriculaIgnoreCase(matricula);
    }


    public List<Reserva> obtenerTodasReservas() {
        return reservaRepository.findAll();
    }

    public void borrarReservas(List<Reserva> reservas) {
        reservaRepository.deleteAll(reservas);
    }


}
