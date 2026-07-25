export const permissionActionOptions = [
  { value: "VIEW_TASKS", label: "Allow View Tasks" },
  { value: "VIEW_DELIVERABLES", label: "Allow View Deliverables" },
  { value: "VIEW_CONTRACTS", label: "Allow View Contracts" },
  { value: "CREATE_TASKS", label: "Allow Create Tasks" },
  { value: "EDIT_TASKS", label: "Allow Edit Tasks" },
  { value: "DELETE_TASKS", label: "Allow Delete Tasks" },
  { value: "CREATE_DELIVERABLES", label: "Allow Create Deliverables" },
  { value: "EDIT_DELIVERABLES", label: "Allow Edit Deliverables" },
  { value: "DELETE_DELIVERABLES", label: "Allow Delete Deliverables" },
  { value: "EDIT_PHASE", label: "Allow Edit Phase Information" },
  { value: "MANAGE_MEMBERS", label: "Allow Manage Project Members" },
];

export const permissionWorkScopeOptions = [
  { value: "OWN", moduleValue: "WORK_SCOPE_OWN", label: "View Own Works Only" },
  { value: "FULL", moduleValue: "WORK_SCOPE_FULL", label: "View Full Project Works" },
];

export function formatPermissionModule(value) {
  if (!value) {
    return "-";
  }

  const moduleCodes = String(value)
    .split(",")
    .map((code) => code.trim())
    .filter(Boolean);

  if (moduleCodes.length === 0) {
    return "-";
  }

  return moduleCodes
    .map((code) => getPermissionModuleLabel(code))
    .join("\n");
}

function getPermissionModuleLabel(code) {
  const normalizedCode = code.toUpperCase();
  const actionOption = permissionActionOptions
    .find((option) => option.value === normalizedCode);
  const workScopeOption = permissionWorkScopeOptions
    .find((option) => option.moduleValue === normalizedCode);
  const knownOption = actionOption || workScopeOption;

  if (knownOption) {
    return knownOption.label;
  }

  const readableWords = normalizedCode
    .toLowerCase()
    .split("_")
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

  return readableWords ? `Allow ${readableWords}` : "-";
}
