package com.fpt.backend.controller.phaseController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.response.phase.PhaseDetailResponse;
import com.fpt.backend.dto.response.phase.PhaseListItemResponse;
import com.fpt.backend.service.interfaces.phase.IPhaseService;
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
@RequestMapping(ApiConstant.Phase.PHASES)
@RequiredArgsConstructor
public class PhaseController {
    private final IPhaseService phaseService;

    @GetMapping(ApiConstant.Phase.BY_PROJECT_ID)
    public ResponseEntity<BaseResponse<List<PhaseListItemResponse>>> getPhasesByProject(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(phaseService.getPhasesByProjectId(projectId)));
    }

    @GetMapping(ApiConstant.Phase.BY_ID)
    public ResponseEntity<BaseResponse<PhaseDetailResponse>> getPhaseById(@PathVariable UUID phaseId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(phaseService.getPhaseById(phaseId)));
    }
}
