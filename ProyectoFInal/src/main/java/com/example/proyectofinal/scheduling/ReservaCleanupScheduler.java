package com.example.proyectofinal.scheduling;

import com.example.proyectofinal.model.Reserva;
import com.example.proyectofinal.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservaCleanupScheduler {

    @Autowired
    private ReservaService reservaService;

    @Scheduled(cron = "0 * * * * *") // cada minuto
    public void eliminarReservasExpiradas() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneId.of("Europe/Madrid"));

        List<Reserva> expiradas = reservaService.obtenerTodasReservas().stream()
                .filter(r -> {
                    OffsetDateTime fin = r.getFechaFin();
                    return fin != null && (fin.isBefore(ahora) || fin.isEqual(ahora));
                })
                .collect(Collectors.toList());

        if (!expiradas.isEmpty()) {
            reservaService.borrarReservas(expiradas);
            System.out.println("🧹 Borradas " + expiradas.size() + " reservas expiradas.");
        }
    }
}
