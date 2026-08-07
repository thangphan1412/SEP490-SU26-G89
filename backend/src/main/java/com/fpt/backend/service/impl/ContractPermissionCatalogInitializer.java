package com.fpt.backend.service.impl;

import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.repository.permission.PermissionActionRepository;
import com.fpt.backend.repository.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps the Contract permission actions available to the existing Project
 * Permission screen without changing the Permission module itself.
 */
@Component
@RequiredArgsConstructor
public class ContractPermissionCatalogInitializer {
    private static final String PROJECT_FULL_ACCESS_PREFIX = "PFA_";

    private final PermissionActionRepository permissionActionRepository;
    private final PermissionRepository permissionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeContractActions() {
        Map<String, PermissionAction> actionByCode = new LinkedHashMap<>();

        for (PermissionAction action : permissionActionRepository.findAll()) {
            actionByCode.put(normalize(action.getActionCode()), action);
        }

        for (ContractProjectActions.Definition definition
                : ContractProjectActions.definitions()) {
            PermissionAction action = actionByCode.get(definition.code());

            if (action == null) {
                action = new PermissionAction();
            }

            action.setActionCode(definition.code());
            action.setActionName(definition.name());
            action.setResourceCode(definition.resourceCode());
            action.setActionDescription(definition.description());
            action.setDisplayOrder(definition.displayOrder());
            action.setStatus(true);
            actionByCode.put(
                    definition.code(),
                    permissionActionRepository.save(action)
            );
        }

        List<PermissionAction> contractActions =
                ContractProjectActions.definitions().stream()
                        .map(definition -> actionByCode.get(definition.code()))
                        .toList();

        for (Permissions permission : permissionRepository.findAll()) {
            if (!isProjectFullAccess(permission)) {
                continue;
            }

            if (permission.getActions() == null) {
                permission.setActions(new LinkedHashSet<>());
            }
            permission.getActions().addAll(contractActions);
            permissionRepository.save(permission);
        }
    }

    private boolean isProjectFullAccess(Permissions permission) {
        String code = normalize(permission.getPermissionCode());
        return code.startsWith(PROJECT_FULL_ACCESS_PREFIX);
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }
}
