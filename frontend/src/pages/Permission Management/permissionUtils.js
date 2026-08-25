// Định dạng thời điểm của quyền theo định dạng ngày giờ dễ đọc.
export function formatPermissionDate(value) {
  // Hiển thị ký hiệu trống khi chưa có giá trị ngày.
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  // Giữ nguyên giá trị gốc khi ngày không thể phân tích.
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

// Ghép mã và tên dự án thành nhãn lựa chọn quyền.
export function formatPermissionProjectName(project) {
  const projectCode = project.projectCode ? `${project.projectCode} - ` : "";
  const projectName = project.projectName || `Project #${project.id}`;
  return projectCode + projectName;
}

// Định dạng dự án đang liên kết với một quyền.
export function formatPermissionProjectValue(permission) {
  // Hiển thị trạng thái chưa gán khi thiếu cả mã và tên dự án.
  if (!permission.projectName && !permission.projectCode) {
    return "Unassigned";
  }

  return [permission.projectCode, permission.projectName]
    .filter(Boolean)
    .join(" - ");
}

// Lấy thông báo lỗi ưu tiên từ response hoặc dùng nội dung dự phòng.
export function getPermissionErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || fallbackMessage;
}
