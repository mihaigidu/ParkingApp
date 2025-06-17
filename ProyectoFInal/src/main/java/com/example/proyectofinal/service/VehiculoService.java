package com.example.proyectofinal.service;

import com.example.proyectofinal.model.User;
import com.example.proyectofinal.model.Vehiculo;
import com.example.proyectofinal.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public List<Vehiculo> getVehiculosForUser(User user) {
        return vehiculoRepository.findByUser(user);
    }

    public Vehiculo guardarVehiculo(Vehiculo vehiculo) {
        boolean existe = vehiculoRepository
                .findByMatriculaAndUser(vehiculo.getMatricula(), vehiculo.getUser())
                .isPresent();

        if (existe) {
            throw new RuntimeException("Ya has registrado un vehículo con esta matrícula.");
        }

        return vehiculoRepository.save(vehiculo);
    }

    public void borrarVehiculo(Long id) {
        vehiculoRepository.deleteById(id);
    }

    public Vehiculo getVehiculoById(Long id) {
        return vehiculoRepository.findById(id).orElse(null);
    }
}
