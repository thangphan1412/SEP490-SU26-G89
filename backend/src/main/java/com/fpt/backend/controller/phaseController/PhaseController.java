package com.fpt.backend.controller.phaseController;

import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.service.interfaces.phase.PhaseService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/phases")
@RequiredArgsConstructor
public class PhaseController {
    private final PhaseService phaseService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<BaseResponse<List<PhaseListItemResponse>>> getPhasesByProject(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(phaseService.getPhasesByProjectId(projectId)));
    }

    @GetMapping({"/{phaseId}", "/view/{phaseId}"})
    public ResponseEntity<BaseResponse<PhaseDetailResponse>> getPhaseById(@PathVariable UUID phaseId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(phaseService.getPhaseById(phaseId)));
    }
}
