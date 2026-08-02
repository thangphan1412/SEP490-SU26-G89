export const PROJECT_ACTIONS = {
  VIEW_TASKS: "VIEW_TASKS",
  CREATE_TASKS: "CREATE_TASKS",
  EDIT_TASKS: "EDIT_TASKS",
  DELETE_TASKS: "DELETE_TASKS",
  VIEW_DELIVERABLES: "VIEW_DELIVERABLES",
  CREATE_DELIVERABLES: "CREATE_DELIVERABLES",
  EDIT_DELIVERABLES: "EDIT_DELIVERABLES",
  DELETE_DELIVERABLES: "DELETE_DELIVERABLES",
  VIEW_CONTRACTS: "VIEW_CONTRACTS",
  CREATE_CONTRACTS: "CREATE_CONTRACTS",
  EDIT_CONTRACTS: "EDIT_CONTRACTS",
  DELETE_CONTRACTS: "DELETE_CONTRACTS",
  EDIT_PROJECT: "EDIT_PROJECT",
  EDIT_PHASE: "EDIT_PHASE",
  MANAGE_MEMBERS: "MANAGE_MEMBERS",
};

export function hasProjectAction(access, actionCode) {
  if (!Array.isArray(access?.allowedActions)) {
    return false;
  }

  const requiredAction = normalizeAction(actionCode);

  return access.allowedActions.some(
    (allowedAction) => normalizeAction(allowedAction) === requiredAction
  );
}

export function hasAnyProjectAction(access, actionCodes) {
  if (!Array.isArray(actionCodes)) {
    return false;
  }

  return actionCodes.some((actionCode) =>
    hasProjectAction(access, actionCode)
  );
}

function normalizeAction(value) {
  return String(value || "").trim().toUpperCase();
}
