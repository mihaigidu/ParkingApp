package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.User;
import com.example.proyectofinal.model.Vehiculo;
import com.example.proyectofinal.service.UserService;
import com.example.proyectofinal.service.VehiculoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/vehiculo")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String indexVehiculo() {
        return "vehiculo/add";
    }

    @GetMapping("/list")
    public String listVehiculos(Model model, Principal principal) {
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
        model.addAttribute("vehiculos", vehiculos);
        return "vehiculo/list";
    }

    @GetMapping("/add")
    public String showAddVehiculoForm(Model model) {
        model.addAttribute("vehiculos", new Vehiculo());
        return "vehiculo/add";
    }

    @PostMapping("/add")
    public String addVehiculo(@ModelAttribute Vehiculo vehiculo, Principal principal, Model model) {
        String principalName = principal.getName();
        User user = userService.findByUsername(principalName);
        if (user == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof OAuth2User oauthUser) {
                user = userService.findByEmail(oauthUser.getAttribute("email"));
            }
        }

        vehiculo.setUser(user);

        try {
            vehiculoService.guardarVehiculo(vehiculo);
        } catch (RuntimeException e) {
            model.addAttribute("vehiculos", vehiculo);
            model.addAttribute("errorMatricula", e.getMessage());
            return "vehiculo/add";
        }

        return "redirect:/home?vehicleAdded=true";
    }


    // Permite eliminar un vehículo.
    @GetMapping("/delete/{vehiculoId}")
    public String deleteVehiculo(@PathVariable Long vehiculoId) {
        vehiculoService.borrarVehiculo(vehiculoId);
        return "redirect:/vehiculo/list";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarVehiculo(@RequestParam("matricula") String matricula) {
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            String url = "https://api-matriculas-espana.p.rapidapi.com/es?plate=" + matricula;
            Response response = client.prepare("GET", url)
                    .setHeader("x-rapidapi-key", "567b5611e0mshf83158e11fcd9f7p1bfe6ajsn5fd0bf4dae76")
                    .setHeader("x-rapidapi-host", "api-matriculas-espana.p.rapidapi.com")
                    .execute()
                    .toCompletableFuture()
                    .join();

            String body = response.getResponseBody();
            System.out.println("Body de la API: " + body); // Depuración en consola del servidor

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(body);

            // Puedes examinar jsonResponse para ver qué campos contiene y depurar.
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al consultar la API de matrículas.");
        }
    }

}
