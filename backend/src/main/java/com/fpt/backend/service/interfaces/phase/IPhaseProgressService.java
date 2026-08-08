package com.fpt.backend.service.interfaces.phase;

import java.util.UUID;

public interface IPhaseProgressService {
    double calculateProgress(UUID phaseId);
}
