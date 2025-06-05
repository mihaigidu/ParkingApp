package com.example.proyectofinal.repository;

import com.example.proyectofinal.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioUsername(String username);
    List<Reserva> findByMatriculaIgnoreCase(String matricula);

}
