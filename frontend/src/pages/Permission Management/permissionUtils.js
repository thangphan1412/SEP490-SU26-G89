export function formatPermissionDate(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function formatPermissionProjectName(project) {
  const projectCode = project.projectCode ? `${project.projectCode} - ` : "";
  const projectName = project.projectName || `Project #${project.id}`;
  return projectCode + projectName;
}

export function formatPermissionProjectValue(permission) {
  if (!permission.projectName && !permission.projectCode) {
    return "Unassigned";
  }

  return [permission.projectCode, permission.projectName]
    .filter(Boolean)
    .join(" - ");
}

export function getPermissionErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || fallbackMessage;
}
