export const permissionWorkScopeOptions = [
  { value: "OWN", label: "View Own Works Only" },
  { value: "FULL", label: "View Full Project Works" },
];

export function formatPermissionActions(actionDetails, actionCodes = []) {
  if (Array.isArray(actionDetails) && actionDetails.length > 0) {
    return actionDetails
      .map((action) => action.actionName || formatActionCode(action.actionCode))
      .filter(Boolean)
      .join("\n");
  }

  if (!Array.isArray(actionCodes) || actionCodes.length === 0) {
    return "-";
  }

  return actionCodes
    .map((code) => formatActionCode(code))
    .join("\n");
}

export function formatPermissionWorkScope(workScope) {
  const option = permissionWorkScopeOptions.find(
    (currentOption) => currentOption.value === workScope
  );

  return option?.label || workScope || "-";
}

function formatActionCode(code) {
  const normalizedCode = String(code || "").trim().toUpperCase();
  const readableWords = normalizedCode
    .toLowerCase()
    .split("_")
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

  return readableWords ? `Allow ${readableWords}` : "-";
}
