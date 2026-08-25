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

export function calculatePhaseStartDatesForDisplay(
  phases,
  projectStartDate
) {
  let expectedStartDate = projectStartDate;
  const updatedPhases = [];

  for (const phase of phases) {
    const updatedPhase = {
      ...phase,
    };

    updatedPhase.startDate = expectedStartDate;
    updatedPhases.push(updatedPhase);

    if (phase.endDate) {
      expectedStartDate = addOneDay(phase.endDate);
    } else {
      expectedStartDate = "";
    }
  }

  return updatedPhases;
}

export function isCompletedProjectStatus(status) {
  return String(status || "").trim().toLowerCase() === "completed";
}

export function addOneDay(dateValue) {
  if (!dateValue) {
    return "";
  }

  const date = new Date(dateValue + "T00:00:00Z");

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  date.setUTCDate(date.getUTCDate() + 1);
  return date.toISOString().slice(0, 10);
}
