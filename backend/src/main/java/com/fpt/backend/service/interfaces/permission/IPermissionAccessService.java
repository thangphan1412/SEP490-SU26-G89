package com.fpt.backend.service.interfaces.permission;

import com.fpt.backend.dto.response.project.ProjectAccessResponse;

import java.util.List;
import java.util.UUID;

public interface IPermissionAccessService {
    // Lấy thông tin truy cập của người dùng hiện tại trong một dự án.
    ProjectAccessResponse getCurrentUserAccess(UUID projectId);

    // Yêu cầu người dùng hiện tại có quyền truy cập dự án.
    void requireProjectAccess(UUID projectId);

    // Yêu cầu người dùng hiện tại có một action cụ thể trong dự án.
    void requireAction(UUID projectId, String actionCode);

    // Kiểm tra thông tin truy cập có chứa action cụ thể hay không.
    boolean hasAction(
            ProjectAccessResponse access,
            String actionCode
    );

    // Kiểm tra action có phạm vi thao tác trên toàn dự án hay không.
    boolean hasFullWorkScope(
            ProjectAccessResponse access,
            String actionCode
    );

    // Lấy các dự án mà người dùng hiện tại có action được yêu cầu.
    List<UUID> getCurrentUserProjectIdsWithAction(String actionCode);
}
