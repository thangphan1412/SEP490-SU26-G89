package com.fpt.backend.service.interfaces.phase;

import java.util.UUID;

public interface PhaseProgressService {
    double calculateProgress(UUID phaseId);
}
