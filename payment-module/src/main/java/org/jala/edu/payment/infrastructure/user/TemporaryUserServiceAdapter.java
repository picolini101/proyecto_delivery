package org.jala.edu.payment.infrastructure.user;

import org.jala.edu.payment.domain.port.UserService;
import org.springframework.stereotype.Component;

import java.util.UUID;

// shortcut: no consulta usuarios reales y autoriza a todos; reemplazar cuando el equipo defina la integración con user-module.
@Component
public class TemporaryUserServiceAdapter implements UserService {

    @Override
    public boolean isUserActiveAndHasRole(UUID userId, String role) {
        return true;
    }
}