package com.fpt.backend.service.impl.permission;

import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.repository.permission.PermissionActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PermissionActionCatalogInitializer
        implements ApplicationRunner {
    private final PermissionActionRepository permissionActionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (PermissionAction.DefaultAction defaultAction
                : PermissionAction.DefaultAction.values()) {
            boolean actionExists = permissionActionRepository
                    .findByActionCodeIgnoreCase(
                            defaultAction.getActionCode()
                    )
                    .isPresent();

            if (actionExists) {
                continue;
            }

            PermissionAction action = PermissionAction.builder()
                    .actionCode(defaultAction.getActionCode())
                    .actionName(defaultAction.getActionName())
                    .resourceCode(defaultAction.getResourceCode())
                    .actionDescription(defaultAction.getDescription())
                    .displayOrder(defaultAction.getDisplayOrder())
                    .status(true)
                    .build();
            permissionActionRepository.save(action);
        }
    }
}
