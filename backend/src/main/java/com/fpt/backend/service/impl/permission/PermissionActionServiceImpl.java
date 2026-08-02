package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.permission.PermissionActionRepository;
import com.fpt.backend.service.interfaces.permission.PermissionActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionActionServiceImpl
        implements PermissionActionService {
    private final PermissionActionRepository permissionActionRepository;

    @Override
    public void configurePermission(
            Permissions permission,
            List<String> allowedActionCodes,
            String workScopeValue) {
        if (permission == null) {
            throw new BadHttpException("Permission is required");
        }

        Permissions.WorkScope workScope = parseWorkScope(workScopeValue);
        Set<String> requestedCodes = normalizeActionCodes(
                allowedActionCodes
        );
        List<PermissionAction> availableActions =
                findAvailableActionEntities();
        Map<String, PermissionAction> availableActionByCode =
                new LinkedHashMap<>();

        for (PermissionAction action : availableActions) {
            availableActionByCode.put(
                    normalize(action.getActionCode()),
                    action
            );
        }

        List<String> unsupportedCodes = new ArrayList<>();

        for (String requestedCode : requestedCodes) {
            if (!availableActionByCode.containsKey(requestedCode)) {
                unsupportedCodes.add(requestedCode);
            }
        }

        if (!unsupportedCodes.isEmpty()) {
            throw new BadHttpException(
                    "Unsupported or inactive permission actions: "
                            + String.join(", ", unsupportedCodes)
            );
        }

        Set<PermissionAction> selectedActions = new LinkedHashSet<>();

        for (PermissionAction action : availableActions) {
            if (requestedCodes.contains(normalize(action.getActionCode()))) {
                selectedActions.add(action);
            }
        }

        permission.setActions(selectedActions);
        permission.setWorkScope(workScope);
    }

    @Override
    public void configureFullAccess(Permissions permission) {
        if (permission == null) {
            throw new BadHttpException("Permission is required");
        }

        List<PermissionAction> availableActions =
                findAvailableActionEntities();

        if (availableActions.isEmpty()) {
            throw new BadHttpException(
                    "The permission action catalog is empty"
            );
        }

        permission.setActions(new LinkedHashSet<>(availableActions));
        permission.setWorkScope(Permissions.WorkScope.FULL);
    }

    @Override
    public List<String> getAllowedActionCodes(Permissions permission) {
        List<String> actionCodes = new ArrayList<>();

        for (PermissionAction action : getActions(permission)) {
            actionCodes.add(action.getActionCode());
        }

        return actionCodes;
    }

    @Override
    public List<PermissionActionResponse> getActionDetails(
            Permissions permission) {
        List<PermissionActionResponse> responses = new ArrayList<>();

        for (PermissionAction action : getActions(permission)) {
            responses.add(toResponse(action));
        }

        return responses;
    }

    @Override
    public String getWorkScope(Permissions permission) {
        if (permission == null || permission.getWorkScope() == null) {
            return Permissions.WorkScope.FULL.name();
        }

        return permission.getWorkScope().name();
    }

    @Override
    public List<PermissionActionResponse> getAvailableActions() {
        List<PermissionActionResponse> responses = new ArrayList<>();

        for (PermissionAction action : findAvailableActionEntities()) {
            responses.add(toResponse(action));
        }

        return responses;
    }

    private List<PermissionAction> findAvailableActionEntities() {
        return permissionActionRepository
                .findByStatusTrueOrderByDisplayOrderAscActionCodeAsc();
    }

    private List<PermissionAction> getActions(Permissions permission) {
        if (permission == null || permission.getActions() == null) {
            return List.of();
        }

        return permission.getActions().stream()
                .filter(action -> Boolean.TRUE.equals(action.getStatus()))
                .sorted((firstAction, secondAction) -> {
                    int orderComparison = Integer.compare(
                            getDisplayOrder(firstAction),
                            getDisplayOrder(secondAction)
                    );

                    if (orderComparison != 0) {
                        return orderComparison;
                    }

                    return normalize(firstAction.getActionCode())
                            .compareTo(normalize(
                                    secondAction.getActionCode()
                            ));
                })
                .toList();
    }

    private int getDisplayOrder(PermissionAction action) {
        if (action == null || action.getDisplayOrder() == null) {
            return Integer.MAX_VALUE;
        }

        return action.getDisplayOrder();
    }

    private Permissions.WorkScope parseWorkScope(String value) {
        String normalizedValue = normalize(value);

        try {
            return Permissions.WorkScope.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new BadHttpException(
                    "Work scope must be OWN or FULL"
            );
        }
    }

    private Set<String> normalizeActionCodes(List<String> actionCodes) {
        Set<String> normalizedCodes = new LinkedHashSet<>();

        if (actionCodes == null) {
            return normalizedCodes;
        }

        for (String actionCode : actionCodes) {
            String normalizedCode = normalize(actionCode);

            if (normalizedCode.isBlank()) {
                throw new BadHttpException(
                        "Permission action code must not be blank"
                );
            }

            normalizedCodes.add(normalizedCode);
        }

        return normalizedCodes;
    }

    private PermissionActionResponse toResponse(PermissionAction action) {
        return new PermissionActionResponse(
                action.getId(),
                action.getActionCode(),
                action.getActionName(),
                action.getResourceCode(),
                action.getActionDescription(),
                action.getDisplayOrder()
        );
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }
}
