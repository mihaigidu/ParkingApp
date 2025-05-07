package com.example.proyectofinal.service;

import com.example.proyectofinal.model.Payment;
import com.example.proyectofinal.model.User;
import com.example.proyectofinal.repository.PaymentRepository;
import com.example.proyectofinal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;


    public Payment createPayment(Long userId, Double amount, String zone) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Payment payment = new Payment();
        payment.setUser(userOptional.get());
        payment.setAmount(amount);
        payment.setZone(zone);
        payment.setPaymentDate(LocalDateTime.now());

        return paymentRepository.save(payment);
    }


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    public void deletePayment(Long paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    public void registrarPago(Payment payment) {
        paymentRepository.save(payment);
    }
}
