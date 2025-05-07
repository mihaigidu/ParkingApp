package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.Payment;
import com.example.proyectofinal.model.Reserva;
import com.example.proyectofinal.model.User;
import com.example.proyectofinal.service.PaymentService;
import com.example.proyectofinal.service.ReservaService;
import com.example.proyectofinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private UserService userService;


    @GetMapping("/procesar-pago/{reservaId}")
    public String mostrarPaginaPago(@PathVariable Long reservaId, Model model, Principal principal) {
        if (principal == null) {
            System.out.println("⚠️ Usuario no autenticado.");
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oauthUser =
                        (org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal();
                String email = oauthUser.getAttribute("email");
                user = userService.findByEmail(email);
            }
        }
        if (user == null) {
            System.out.println("⚠️ Error: No se encontró el usuario para el principal dado.");
            return "redirect:/error";
        }

        Reserva reserva = reservaService.getReservaById(reservaId);
        if (reserva == null || !reserva.getUsuario().getId().equals(user.getId())) {
            return "redirect:/error";
        }

        model.addAttribute("userId", user.getId());
        model.addAttribute("reservaId", reservaId);
        model.addAttribute("matricula", reserva.getMatricula());
        model.addAttribute("tiempo", reserva.getTiempo());
        model.addAttribute("precio", reserva.getPrecio());
        model.addAttribute("zone", "centro");

        return "procesar-pago";
    }

    @PostMapping("/procesar-pago")
    public String procesarPago(@RequestParam Map<String, String> params) {
        System.out.println("📥 Parámetros recibidos en POST: " + params);

        // Verifica que el parámetro userId esté presente
        if (!params.containsKey("userId") || params.get("userId").isEmpty()) {
            System.out.println("⚠️ Error: userId está vacío o no está presente en el POST.");
            return "redirect:/error";
        }

        try {
            Long userId = Long.parseLong(params.get("userId"));
            Long reservaId = Long.parseLong(params.get("reservaId"));
            String metodoPago = params.get("metodoPago");
            Double precio = Double.parseDouble(params.get("precio"));

            User user = userService.getUserById(userId);
            Reserva reserva = reservaService.getReservaById(reservaId);

            if (!reserva.getUsuario().getId().equals(user.getId())) {
                return "redirect:/error";
            }

            Payment pago = new Payment();
            pago.setUser(user);
            pago.setAmount(precio);
            pago.setZone(params.get("zone"));
            pago.setPaymentDate(LocalDateTime.now());

            switch (metodoPago.toLowerCase()) {
                case "tarjeta":
                    System.out.println("Procesando pago con tarjeta...");
                    break;
                case "paypal":
                    System.out.println("Procesando pago con PayPal...");
                    break;
                default:
                    throw new IllegalArgumentException("Método de pago no soportado: " + metodoPago);
            }

            paymentService.registrarPago(pago);
            System.out.println("✅ Pago registrado exitosamente.");

            return "redirect:/home?paid=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
}
