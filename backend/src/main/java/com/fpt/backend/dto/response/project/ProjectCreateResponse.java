package com.fpt.backend.dto.response.project;

import java.util.UUID;

// Xác nhận tạo thành công mà không trả dữ liệu chi tiết cho người chưa có quyền xem.
public record ProjectCreateResponse(
        UUID id,
        boolean canView
) {
}
