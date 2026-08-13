package com.fpt.backend.service.impl.phase;

import com.fpt.backend.repository.phase.PhaseTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhaseProgressService {
    private final PhaseTaskRepository phaseTaskRepository;

    public double calculateProgress(UUID phaseId) {
        long totalTasks = phaseTaskRepository.countByPhaseId(phaseId);

        if (totalTasks == 0) {
            return 0;
        }

        long doneTasks = phaseTaskRepository.countDoneByPhaseId(phaseId);
        double progress = doneTasks * 100.0 / totalTasks;

        return Math.round(progress * 100.0) / 100.0;
    }
}
