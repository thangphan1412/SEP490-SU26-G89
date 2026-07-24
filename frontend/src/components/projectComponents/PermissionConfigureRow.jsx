import { Button, Form } from "react-bootstrap";

function PermissionConfigureRow({
  permission,
  isOpen,
  configuration,
  actionOptions,
  saving,
  onConfigure,
  onToggleAction,
  onScopeChange,
  onSave,
}) {
  const allowedActions = Array.isArray(configuration?.allowedActions)
    ? configuration.allowedActions
    : [];

  return (
    <div className={`permission-configure-row ${isOpen ? "is-open" : ""}`}>
      <div className="permission-configure-row-summary">
        <div className="permission-configure-row-information">
          <div className="permission-configure-name-line">
            <strong>{permission.permissionName || "Unnamed permission"}</strong>
            <span className={permission.status ? "is-active" : "is-inactive"}>
              {permission.status ? "Active" : "Inactive"}
            </span>
          </div>

          <small>{permission.permissionCode || "No permission code"}</small>
          <p>{permission.permissionDescription || "No description has been added."}</p>
        </div>

        <Button
          type="button"
          variant="outline-primary"
          className="permission-configure-open-button"
          disabled={saving}
          onClick={() => onConfigure(permission)}
        >
          {isOpen ? "Configuring" : "Configure"}
        </Button>
      </div>

      {isOpen && (
        <div className="permission-configure-editor">
          <div>
            <h4>Allowed actions</h4>
            <p>Select the actions that this permission can perform.</p>
          </div>

          <div className="permission-configure-action-grid">
            {actionOptions.map((option) => (
              <Form.Check
                key={option.value}
                id={`permission-${permission.permissionId}-${option.value}`}
                type="checkbox"
                label={option.label}
                checked={allowedActions.includes(option.value)}
                onChange={() => onToggleAction(option.value)}
              />
            ))}
          </div>

          <div className="permission-configure-scope">
            <h4>Work visibility</h4>
            <p>Choose exactly one scope for tasks and deliverables.</p>

            <div className="permission-configure-scope-options">
              <Form.Check
                id={`permission-${permission.permissionId}-scope-own`}
                type="checkbox"
                label="View Own Works Only"
                checked={configuration?.workScope === "OWN"}
                onChange={() => onScopeChange("OWN")}
              />

              <Form.Check
                id={`permission-${permission.permissionId}-scope-full`}
                type="checkbox"
                label="View Full Project Works"
                checked={configuration?.workScope === "FULL"}
                onChange={() => onScopeChange("FULL")}
              />
            </div>
          </div>

          <div className="permission-configure-save-row">
            <Button
              type="button"
              className="permission-configure-save-button"
              disabled={saving}
              onClick={onSave}
            >
              {saving ? "Saving..." : "Save Configuration"}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

export default PermissionConfigureRow;
