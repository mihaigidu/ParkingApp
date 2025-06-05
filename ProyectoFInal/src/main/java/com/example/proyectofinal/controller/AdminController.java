package com.example.proyectofinal.controller;


import com.example.proyectofinal.model.Payment;
import com.example.proyectofinal.model.Reserva;
import com.example.proyectofinal.service.PaymentService;
import com.example.proyectofinal.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/verificar")
    public String mostrarVerificador() {
        return "admin-verificar";
    }

    @PostMapping("/verificar")
    public String verificarPago(@RequestParam String matricula, Model model) {
        List<Reserva> reservas = reservaService.buscarPorMatricula(matricula);
        boolean pagado = false;

        for (Reserva reserva : reservas) {
            List<Payment> pagos = paymentService.getPaymentsByUser(reserva.getUsuario().getId());
            if (!pagos.isEmpty()) {
                pagado = true;
                break;
            }
        }

        model.addAttribute("matricula", matricula);
        model.addAttribute("pagado", pagado);
        return "admin-verificar";
    }
}
