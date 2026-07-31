package com.fpt.backend.service.impl.permission;

import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.service.interfaces.permission.PermissionModuleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PermissionModuleServiceImpl
        implements PermissionModuleService {
    private static final String WORK_SCOPE_OWN = "OWN";
    private static final String WORK_SCOPE_FULL = "FULL";
    private static final String WORK_SCOPE_OWN_TOKEN = "WORK_SCOPE_OWN";
    private static final String WORK_SCOPE_FULL_TOKEN = "WORK_SCOPE_FULL";
    private static final List<String> PERMISSION_ACTIONS = List.of(
            "VIEW_TASKS",
            "VIEW_DELIVERABLES",
            "VIEW_CONTRACTS",
            "CREATE_TASKS",
            "EDIT_TASKS",
            "DELETE_TASKS",
            "CREATE_DELIVERABLES",
            "EDIT_DELIVERABLES",
            "DELETE_DELIVERABLES",
            "EDIT_PROJECT",
            "EDIT_PHASE",
            "MANAGE_MEMBERS"
    );

    @Override
    public String createModuleValue(
            List<String> allowedActions,
            String workScopeValue) {
        Set<String> requestedActions = new LinkedHashSet<>();
        List<String> actions = allowedActions == null
                ? List.of()
                : allowedActions;

        for (String action : actions) {
            String normalizedAction =
                    normalize(action).toUpperCase(Locale.ROOT);

            if (!PERMISSION_ACTIONS.contains(normalizedAction)) {
                throw new BadHttpException(
                        "Unsupported permission action: " + action
                );
            }

            requestedActions.add(normalizedAction);
        }

        String workScope =
                normalize(workScopeValue).toUpperCase(Locale.ROOT);

        if (!WORK_SCOPE_OWN.equals(workScope)
                && !WORK_SCOPE_FULL.equals(workScope)) {
            throw new BadHttpException(
                    "Work scope must be OWN or FULL"
            );
        }

        List<String> storedValues = new ArrayList<>();

        for (String supportedAction : PERMISSION_ACTIONS) {
            if (requestedActions.contains(supportedAction)) {
                storedValues.add(supportedAction);
            }
        }

        if (WORK_SCOPE_OWN.equals(workScope)) {
            storedValues.add(WORK_SCOPE_OWN_TOKEN);
        } else {
            storedValues.add(WORK_SCOPE_FULL_TOKEN);
        }

        return String.join(",", storedValues);
    }

    @Override
    public List<String> getAllowedActions(String permissionModule) {
        Set<String> storedValues =
                getStoredModuleValues(permissionModule);
        List<String> allowedActions = new ArrayList<>();

        for (String action : PERMISSION_ACTIONS) {
            if (storedValues.contains(action)) {
                allowedActions.add(action);
            }
        }

        return allowedActions;
    }

    @Override
    public String getWorkScope(String permissionModule) {
        Set<String> storedValues =
                getStoredModuleValues(permissionModule);

        if (storedValues.contains(WORK_SCOPE_OWN_TOKEN)) {
            return WORK_SCOPE_OWN;
        }

        return WORK_SCOPE_FULL;
    }

    private Set<String> getStoredModuleValues(String permissionModule) {
        Set<String> storedValues = new LinkedHashSet<>();
        String moduleValue = normalize(permissionModule);

        if (moduleValue.isBlank()) {
            return storedValues;
        }

        for (String value : moduleValue.split(",")) {
            String normalizedValue =
                    normalize(value).toUpperCase(Locale.ROOT);

            if (!normalizedValue.isBlank()) {
                storedValues.add(normalizedValue);
            }
        }

        return storedValues;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
