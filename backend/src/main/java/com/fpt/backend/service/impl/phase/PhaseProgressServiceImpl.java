package com.fpt.backend.service.impl.phase;

import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.service.interfaces.phase.PhaseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhaseProgressServiceImpl implements PhaseProgressService {
    private final PhaseTaskRepository phaseTaskRepository;

    @Override
    public double calculateProgress(UUID phaseId) {
        long totalTasks = phaseTaskRepository.countByPhaseId(phaseId);

        if (totalTasks == 0) {
            return 0;
        }

        long completedTasks = phaseTaskRepository.countCompletedByPhaseId(phaseId);
        double progress = completedTasks * 100.0 / totalTasks;

        return Math.round(progress * 100.0) / 100.0;
    }
}
