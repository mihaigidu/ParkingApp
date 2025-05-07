package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.User;
import com.example.proyectofinal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "homeInicio";
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        System.out.println("Accediendo a /register");
        model.addAttribute("user", new User());
        return "register";
    }


    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") User user,
                                      BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        userService.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/editar-usuario")
    public String editarUsuario(Model model) {
        // Obtener el usuario autenticado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);

        if (user != null) {
            model.addAttribute("user", user);
            return "editar-usuario";  // La vista para editar el usuario
        } else {
            return "redirect:/error";
        }
    }

    // Actualizar la información del usuario
    @PostMapping("/editar-usuario")
    public String actualizarUsuario(@ModelAttribute("user") User user) {
        // Obtener el usuario autenticado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userService.findByUsername(username);

        if (existingUser != null) {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            userService.save(existingUser);  // Guardar los cambios en la base de datos
            return "redirect:/home";  // Redirigir al home después de la actualización
        } else {
            return "redirect:/error";
        }
    }



}
