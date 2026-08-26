export const PROJECT_STATUS_OPTIONS = [
  "Planning",
  "Active",
  "On Hold",
  "Completed",
  "Cancelled",
];

export function createClientId() {
  return "phase-" + Date.now() + "-" + Math.random().toString(16).slice(2);
}

export function getEmployeeName(employee) {
  const firstName = employee.firstName || "";
  const lastName = employee.lastName || "";
  const fullName = (firstName + " " + lastName).trim();

  return fullName
    || employee.userName
    || employee.email
    || "Employee #" + employee.id;
}

export function getEmployeeDescription(employee) {
  const values = [
    employee.email,
    employee.status,
  ];

  return values.filter(Boolean).join(" | ");
}

export function getEmployeeSearchText(employee) {
  const values = [
    employee.firstName,
    employee.lastName,
    employee.userName,
    employee.email,
    employee.status,
  ];

  return values.filter(Boolean).join(" ").toLowerCase();
}

export function getFilterOptions(employees, fieldName) {
  const uniqueValues = new Set();

  for (const employee of employees) {
    const value = employee[fieldName];

    if (value) {
      uniqueValues.add(value);
    }
  }

  const options = [...uniqueValues];
  options.sort(function (firstValue, secondValue) {
    return firstValue.localeCompare(secondValue);
  });
  return options;
}

export function getProjectErrorMessage(error, fallbackMessage) {
  const responseBody = error.response?.data;

  if (!responseBody) {
    return fallbackMessage;
  }

  if (typeof responseBody === "string" && responseBody.trim()) {
    return responseBody.trim();
  }

  const errorData = responseBody.data;

  // Ưu tiên các lỗi validation theo từng field do backend trả về.
  if (errorData && typeof errorData === "object") {
    const validationMessages = [];

    for (const message of Object.values(errorData)) {
      if (typeof message === "string" && message.trim()) {
        validationMessages.push(message.trim());
      }
    }

    if (validationMessages.length > 0) {
      return validationMessages.join(" ");
    }
  }

  // Một số lỗi nghiệp vụ đặt nội dung trực tiếp trong data.
  if (typeof errorData === "string" && errorData.trim()) {
    return errorData.trim();
  }

  // Hỗ trợ các cấu trúc response lỗi phổ biến còn lại.
  const backendMessage = responseBody.message
    || responseBody.detail
    || responseBody.error;

  if (typeof backendMessage === "string" && backendMessage.trim()) {
    return backendMessage.trim();
  }

  return fallbackMessage;
}

export function getPhaseDateError(
  phases,
  projectStartDate,
  projectEndDate
) {
  for (let index = 0; index < phases.length; index++) {
    const phase = phases[index];
    const phaseNumber = index + 1;

    if (!phase.startDate) {
      return "Phase " + phaseNumber + " start date is required.";
    }

    if (!phase.endDate) {
      return "Phase " + phaseNumber + " end date is required.";
    }

    if (phase.startDate > phase.endDate) {
      return "Phase " + phaseNumber
        + " start date must not be after its end date.";
    }

    if (projectStartDate && phase.startDate < projectStartDate) {
      return "Phase " + phaseNumber
        + " start date must not be before the project start date.";
    }

    if (projectEndDate && phase.endDate > projectEndDate) {
      return "Phase " + phaseNumber
        + " end date must not be after the project end date.";
    }
  }

  return "";
}

export function isCompletedProjectStatus(status) {
  return String(status || "").trim().toLowerCase() === "completed";
}
