package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.Reserva;
import com.example.proyectofinal.model.User;
import com.example.proyectofinal.model.Vehiculo;
import com.example.proyectofinal.service.ReservaService;
import com.example.proyectofinal.service.UserService;
import com.example.proyectofinal.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Controller
public class ReservaController {

    private final ReservaService reservaService;
    private final UserService userService;
    private final VehiculoService vehiculoService;

    public ReservaController(ReservaService reservaService,
                             UserService userService,
                             VehiculoService vehiculoService) {
        this.reservaService = reservaService;
        this.userService = userService;
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/reserva")
    public String verReservas(Model model, Principal principal) {
        // Obtiene las reservas del usuario actual
        List<Reserva> reservas = reservaService.obtenerReservasDelUsuario();
        model.addAttribute("reservas", reservas);
        return "reserva";
    }

    @GetMapping("/nueva-reserva")
    public String mostrarFormularioReserva(Model model, Principal principal) {
        model.addAttribute("reserva", new Reserva());

        String principalName = principal.getName();
        User user = userService.findByUsername(principalName);
        if (user == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
                String email = oauthUser.getAttribute("email");
                user = userService.findByEmail(email);
            }
        }

        List<Vehiculo> vehiculos = vehiculoService.getVehiculosForUser(user);
        model.addAttribute("vehicles", vehiculos);


        return "nueva-reserva";
    }

    @PostMapping("/nueva-reserva")
    public String guardarReserva(
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult result,
            @RequestParam(value = "vehiculoId", required = false) Long vehiculoId,
            Principal principal
    ) {
        if (result.hasErrors()) {
            return "nueva-reserva";
        }

        String principalName = principal.getName();
        User user = userService.findByUsername(principalName);
        if (user == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
                String email = oauthUser.getAttribute("email");
                user = userService.findByEmail(email);
            }
        }
        if (user == null) {
            return "redirect:/login?error";
        }

        if (vehiculoId != null) {
            Vehiculo vehiculo = vehiculoService.getVehiculoById(vehiculoId);
            if (vehiculo != null && vehiculo.getUser().getId().equals(user.getId())) {
                reserva.setMatricula(vehiculo.getMatricula());
            }
        }

        reserva.setUsuario(user);
        reserva.setFechaReserva(OffsetDateTime.now(ZoneId.of("Europe/Madrid")));
        reserva.setPrecio(reserva.getTiempo() * 0.05);

        reservaService.guardarReserva(reserva);
        return "redirect:/procesar-pago/" + reserva.getId();
    }

    @GetMapping("/reserva/extender/{id}")
    public String mostrarFormularioExtension(@PathVariable Long id, Model model, Principal principal) {
        Reserva reserva = reservaService.getReservaById(id);

        if (reserva == null || !reserva.getUsuario().getUsername().equals(principal.getName())) {
            return "redirect:/error";
        }

        model.addAttribute("reserva", reserva);
        return "extender-reserva";
    }

    @PostMapping("/reserva/extender")
    public String procesarExtensionReserva(@RequestParam Long reservaId, @RequestParam int nuevoTiempo) {
        Reserva reserva = reservaService.getReservaById(reservaId);
        if (reserva == null) return "redirect:/error";

        // Sumar el nuevo tiempo al existente
        int tiempoTotal = reserva.getTiempo() + nuevoTiempo;
        reserva.setTiempo(tiempoTotal);

        // Actualizar fechaFin sumando los nuevos minutos
        if (reserva.getFechaFin() != null) {
            reserva.setFechaFin(reserva.getFechaFin().plusMinutes(nuevoTiempo));
        } else if (reserva.getFechaReserva() != null) {
            reserva.setFechaFin(reserva.getFechaReserva().plusMinutes(tiempoTotal));
        } else {
            reserva.setFechaFin(reserva.getFechaReserva().plusMinutes(tiempoTotal));
        }

        // Recalcular precio
        reserva.setPrecio(tiempoTotal * 0.05);

        reservaService.guardarReserva(reserva);
        return "redirect:/procesar-pago/" + reserva.getId();
    }


}
