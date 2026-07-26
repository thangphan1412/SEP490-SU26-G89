export const PROJECT_STATUS_OPTIONS = [
  "Planning",
  "Active",
  "On Hold",
  "Completed",
  "Cancelled",
];

export const PHASE_STATUS_OPTIONS = [
  "Planning",
  "In Progress",
  "On Hold",
  "Completed",
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
    getEmployeeRoleNames(employee).join(", ") || "No assigned role",
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
    ...getEmployeeRoleNames(employee),
    employee.status,
  ];

  return values.filter(Boolean).join(" ").toLowerCase();
}

export function getEmployeeRoleNames(employee) {
  if (!Array.isArray(employee.roles)) {
    return [];
  }

  const roleNames = [];

  for (const role of employee.roles) {
    const roleName = role?.roleName?.trim();

    if (roleName) {
      roleNames.push(roleName);
    }
  }

  return roleNames;
}

export function employeeHasRole(employee, roleId) {
  if (!Array.isArray(employee.roles)) {
    return false;
  }

  for (const role of employee.roles) {
    if (String(role.id) === String(roleId)) {
      return true;
    }
  }

  return false;
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

export function recalculatePhaseStarts(phases, projectStartDate) {
  let expectedStartDate = projectStartDate;
  const updatedPhases = [];

  for (const phase of phases) {
    updatedPhases.push({
      ...phase,
      startDate: expectedStartDate,
    });
    expectedStartDate = phase.endDate ? addOneDay(phase.endDate) : "";
  }

  return updatedPhases;
}

export function validateProject(project) {
  if (!project.projectStartDate || !project.projectEndDate) {
    return "Project start date and end date are required.";
  }

  if (project.projectEndDate < project.projectStartDate) {
    return "Project end date must not be before its start date.";
  }

  if (project.phases.length === 0) {
    return "Add at least one phase to cover the full project timeline.";
  }

  let expectedStartDate = project.projectStartDate;

  for (let index = 0; index < project.phases.length; index += 1) {
    const phase = project.phases[index];
    const phaseNumber = index + 1;

    if (!phase.startDate || !phase.endDate) {
      return "Phase " + phaseNumber
        + " requires both a start date and an end date.";
    }

    if (phase.endDate < phase.startDate) {
      return "Phase " + phaseNumber
        + " end date must not be before its start date.";
    }

    if (
      phase.startDate < project.projectStartDate
      || phase.endDate > project.projectEndDate
    ) {
      return "Phase " + phaseNumber
        + " must stay inside the project date range.";
    }

    if (phase.startDate !== expectedStartDate) {
      if (phase.startDate < expectedStartDate) {
        return "Phase " + phaseNumber
          + " overlaps the previous phase. It must start on "
          + expectedStartDate + ".";
      }

      return "There is a gap before Phase " + phaseNumber
        + ". It must start on " + expectedStartDate + ".";
    }

    expectedStartDate = addOneDay(phase.endDate);
  }

  const finalPhase = project.phases[project.phases.length - 1];

  if (finalPhase.endDate !== project.projectEndDate) {
    return "The final phase must end on the project end date "
      + project.projectEndDate + ".";
  }

  return "";
}

export function getApiErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || fallbackMessage;
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
