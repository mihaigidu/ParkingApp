package com.example.proyectofinal;

import com.example.proyectofinal.model.User;
import com.example.proyectofinal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class ProyectoFInalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoFInalApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByUsername("supervisor").isEmpty()) {
                User admin = new User();
                admin.setUsername("supervisor");
                admin.setEmail("supervisor@admin.com");
                admin.setPassword(encoder.encode("supervisor"));
                admin.setRole("SUPERVISOR");
                userRepository.save(admin);
            }
        };
    }


}
