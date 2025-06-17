package com.example.proyectofinal.repository;

import com.example.proyectofinal.model.Vehiculo;
import com.example.proyectofinal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByUser(User user);
    Optional<Vehiculo> findByMatriculaAndUser(String matricula, User user);

}
