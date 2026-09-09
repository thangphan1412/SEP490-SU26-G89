const DEPARTMENT_CODE_PATTERN = /^[A-Z][A-Z0-9_]{1,49}$/;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function formatDepartmentDate(value, dateStyle = "short") {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle,
    timeStyle: "short",
  }).format(date);
}

export function isValidDepartmentId(id) {
  return UUID_PATTERN.test(id || "");
}

export function normalizeDepartmentRequest(form) {
  return {
    departmentName: form.departmentName.trim(),
    departmentCode: form.departmentCode.trim().toUpperCase(),
    departmentStatus: form.active ? "Active" : "Inactive",
  };
}

export function validateDepartmentRequest(request) {
  if (!request.departmentName) {
    return "Department name is required.";
  }

  if (request.departmentName.length > 100) {
    return "Department name cannot exceed 100 characters.";
  }

  if (!request.departmentCode) {
    return "Department code is required.";
  }

  if (!DEPARTMENT_CODE_PATTERN.test(request.departmentCode)) {
    return "Department code must start with a letter and contain 2-50 uppercase letters, numbers, or underscores.";
  }

  if (!["Active", "Inactive"].includes(request.departmentStatus)) {
    return "Department status must be Active or Inactive.";
  }

  return "";
}

export function getDepartmentErrorMessage(error, fallbackMessage) {
  const validationErrors = error.response?.data?.data;
  const firstValidationError = validationErrors
    && typeof validationErrors === "object"
    && !Array.isArray(validationErrors)
    ? Object.values(validationErrors).find(Boolean)
    : "";

  return firstValidationError
    || error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || fallbackMessage;
}

export function getDepartmentLoadErrorMessage(error) {
  if (error.response?.status === 404) {
    return "Department was not found.";
  }

  if (error.response?.status === 400) {
    return "The department ID is invalid.";
  }

  return getDepartmentErrorMessage(
    error,
    "Unable to load department. Please try again later."
  );
}
