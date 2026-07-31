import { Button } from "react-bootstrap";

function PermissionConfigureRow({
  permission,
  onConfigure,
}) {
  function handleConfigure() {
    onConfigure(permission);
  }

  return (
    <div className="permission-configure-row">
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
          onClick={handleConfigure}
        >
          Configure
        </Button>
      </div>
    </div>
  );
}

export default PermissionConfigureRow;
