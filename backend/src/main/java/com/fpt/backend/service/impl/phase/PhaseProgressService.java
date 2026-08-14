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

    // Tính phần trăm tiến độ phase dựa trên tỷ lệ task đã hoàn thành.
    public double calculateProgress(UUID phaseId) {
        long totalTasks = phaseTaskRepository.countByPhaseId(phaseId);

        // Trả về 0 để tránh phép chia cho không khi phase chưa có task.
        if (totalTasks == 0) {
            return 0;
        }

        long doneTasks = phaseTaskRepository.countDoneByPhaseId(phaseId);
        double progress = doneTasks * 100.0 / totalTasks;

        return Math.round(progress * 100.0) / 100.0;
    }
}
