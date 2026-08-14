package com.fpt.backend.service.impl.permission;

import com.fpt.backend.dto.response.permission.PermissionActionResponse;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.enums.WorkScope;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.permission.PermissionActionRepository;
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
public class PermissionActionService {
    private final PermissionActionRepository permissionActionRepository;

    // Áp dụng danh sách action và phạm vi làm việc được yêu cầu cho một quyền.
    public void configurePermission(
            Permissions permission,
            List<String> allowedActionCodes,
            String workScopeValue) {
        // Yêu cầu đối tượng quyền phải tồn tại trước khi cấu hình action.
        if (permission == null) {
            throw new BadHttpException("Permission is required");
        }

        WorkScope workScope = parseWorkScope(workScopeValue);
        Set<String> requestedCodes = normalizeActionCodes(
                allowedActionCodes
        );
        List<PermissionAction> availableActions =
                findAllActionEntities();
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
            // Thu thập các mã action không tồn tại trong permission catalog.
            if (!availableActionByCode.containsKey(requestedCode)) {
                unsupportedCodes.add(requestedCode);
            }
        }

        // Từ chối mọi mã action không tồn tại trong catalog.
        if (!unsupportedCodes.isEmpty()) {
            throw new BadHttpException(
                    "Unsupported permission actions: "
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

    // Cấp toàn bộ action trong catalog và phạm vi FULL cho một quyền.
    public void configureFullAccess(Permissions permission) {
        // Yêu cầu đối tượng quyền phải tồn tại trước khi cấp toàn quyền.
        if (permission == null) {
            throw new BadHttpException("Permission is required");
        }

        List<PermissionAction> availableActions =
                findAllActionEntities();

        // Không cho phép cấu hình toàn quyền khi catalog chưa có action.
        if (availableActions.isEmpty()) {
            throw new BadHttpException(
                    "The permission action catalog is empty"
            );
        }

        permission.setActions(new LinkedHashSet<>(availableActions));
        permission.setWorkScope(WorkScope.FULL);
    }

    // Lấy danh sách mã action đã được gán cho một quyền theo thứ tự hiển thị.
    public List<String> getAllowedActionCodes(Permissions permission) {
        List<String> actionCodes = new ArrayList<>();

        for (PermissionAction action : getActions(permission)) {
            actionCodes.add(action.getActionCode());
        }

        return actionCodes;
    }

    // Chuyển các action đã gán thành dữ liệu chi tiết trả về cho client.
    public List<PermissionActionResponse> getActionDetails(
            Permissions permission) {
        List<PermissionActionResponse> responses = new ArrayList<>();

        for (PermissionAction action : getActions(permission)) {
            responses.add(toResponse(action));
        }

        return responses;
    }

    // Lấy phạm vi làm việc của quyền và mặc định FULL khi chưa được cấu hình.
    public String getWorkScope(Permissions permission) {
        // Dùng FULL làm giá trị an toàn cho quyền cũ chưa có work scope.
        if (permission == null || permission.getWorkScope() == null) {
            return WorkScope.FULL.name();
        }

        return permission.getWorkScope().name();
    }

    // Lấy toàn bộ action khả dụng trong catalog dưới dạng response.
    public List<PermissionActionResponse> getAvailableActions() {
        List<PermissionActionResponse> responses = new ArrayList<>();

        for (PermissionAction action : findAllActionEntities()) {
            responses.add(toResponse(action));
        }

        return responses;
    }

    // Đọc toàn bộ entity action theo thứ tự hiển thị ổn định.
    private List<PermissionAction> findAllActionEntities() {
        return permissionActionRepository
                .findAllByOrderByDisplayOrderAscActionCodeAsc();
    }

    // Lấy và sắp xếp các action đã gán cho một quyền.
    private List<PermissionAction> getActions(Permissions permission) {
        // Trả về danh sách rỗng khi quyền hoặc tập action chưa được khởi tạo.
        if (permission == null || permission.getActions() == null) {
            return List.of();
        }

        return permission.getActions().stream()
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

    // Lấy thứ tự hiển thị của action và đẩy giá trị thiếu xuống cuối danh sách.
    private int getDisplayOrder(PermissionAction action) {
        if (action == null || action.getDisplayOrder() == null) {
            return Integer.MAX_VALUE;
        }

        return action.getDisplayOrder();
    }

    // Chuyển chuỗi phạm vi đã được DTO kiểm tra thành WorkScope.
    private WorkScope parseWorkScope(String value) {
        return WorkScope.valueOf(normalize(value));
    }

    // Chuẩn hóa và loại trùng danh sách mã action đầu vào.
    private Set<String> normalizeActionCodes(List<String> actionCodes) {
        Set<String> normalizedCodes = new LinkedHashSet<>();

        for (String actionCode : actionCodes) {
            normalizedCodes.add(normalize(actionCode));
        }

        return normalizedCodes;
    }

    // Chuyển entity action thành dữ liệu trả về cho API.
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

    // Chuẩn hóa chuỗi về chữ hoa và loại bỏ khoảng trắng thừa.
    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }
}
