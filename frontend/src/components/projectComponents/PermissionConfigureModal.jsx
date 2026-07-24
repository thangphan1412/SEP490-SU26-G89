import { useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
  configureProjectPermission,
  listProjectPermissionConfigurations,
} from "../../services/projectService/projectApi.js";
import PermissionConfigureRow from "./PermissionConfigureRow.jsx";
import "../../assets/styles/css/projectStyles/PermissionConfigureModal.css";

const permissionActionOptions = [
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

const emptyConfiguration = {
  allowedActions: [],
  workScope: "FULL",
};

function PermissionConfigureModal({ show, projectId, projectName, onHide }) {
  const [permissions, setPermissions] = useState([]);
  const [selectedPermissionId, setSelectedPermissionId] = useState(null);
  const [configuration, setConfiguration] = useState(emptyConfiguration);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (!show) {
      return undefined;
    }

    let isActive = true;

    const loadPermissions = async () => {
      if (!projectId) {
        setPermissions([]);
        setError("Project id is missing.");
        return;
      }

      try {
        setLoading(true);
        setError("");
        setSuccessMessage("");
        setSelectedPermissionId(null);
        const response = await listProjectPermissionConfigurations(projectId);
        const payload = response.data?.data ?? response.data;

        if (isActive) {
          setPermissions(Array.isArray(payload) ? payload : []);
        }
      } catch (requestError) {
        console.error("Unable to load project permissions:", requestError);

        if (isActive) {
          setPermissions([]);
          setError(getErrorMessage(requestError));
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    };

    loadPermissions();

    return () => {
      isActive = false;
    };
  }, [projectId, show]);

  const openConfiguration = (permission) => {
    setSelectedPermissionId(permission.permissionId);
    setConfiguration({
      allowedActions: Array.isArray(permission.allowedActions)
        ? [...permission.allowedActions]
        : [],
      workScope: permission.workScope === "OWN" ? "OWN" : "FULL",
    });
    setError("");
    setSuccessMessage("");
  };

  const toggleAction = (action) => {
    setConfiguration((currentConfiguration) => {
      const currentActions = currentConfiguration.allowedActions;
      const hasAction = currentActions.includes(action);

      return {
        ...currentConfiguration,
        allowedActions: hasAction
          ? currentActions.filter((currentAction) => currentAction !== action)
          : [...currentActions, action],
      };
    });
  };

  const saveConfiguration = async () => {
    if (!selectedPermissionId) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");
      const response = await configureProjectPermission(
        projectId,
        selectedPermissionId,
        configuration
      );
      const updatedPermission = response.data?.data ?? response.data;

      setPermissions((currentPermissions) =>
        currentPermissions.map((permission) =>
          permission.permissionId === selectedPermissionId
            ? updatedPermission
            : permission
        )
      );
      setConfiguration({
        allowedActions: Array.isArray(updatedPermission?.allowedActions)
          ? [...updatedPermission.allowedActions]
          : [],
        workScope: updatedPermission?.workScope === "OWN" ? "OWN" : "FULL",
      });
      setSuccessMessage(
        `Configuration for "${updatedPermission?.permissionName || "permission"}" was saved.`
      );
    } catch (requestError) {
      console.error("Unable to save permission configuration:", requestError);
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      show={show}
      onHide={() => {
        if (!saving) {
          onHide();
        }
      }}
      centered
      scrollable
      size="lg"
      className="permission-configure-modal"
    >
      <Modal.Header closeButton={!saving}>
        <div>
          <Modal.Title>Permission Configure</Modal.Title>
          <p>
            Configure access for {projectName || "this project"}.
          </p>
        </div>
      </Modal.Header>

      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}

        {loading ? (
          <div className="permission-configure-state">
            <Spinner animation="border" size="sm" />
            Loading permissions...
          </div>
        ) : permissions.length === 0 ? (
          <div className="permission-configure-state">
            This project does not have any permissions to configure.
          </div>
        ) : (
          <div className="permission-configure-list">
            {permissions.map((permission) => (
              <PermissionConfigureRow
                key={permission.permissionId}
                permission={permission}
                isOpen={selectedPermissionId === permission.permissionId}
                configuration={configuration}
                actionOptions={permissionActionOptions}
                saving={saving}
                onConfigure={openConfiguration}
                onToggleAction={toggleAction}
                onScopeChange={(workScope) =>
                  setConfiguration((currentConfiguration) => ({
                    ...currentConfiguration,
                    workScope,
                  }))
                }
                onSave={saveConfiguration}
              />
            ))}
          </div>
        )}
      </Modal.Body>

      <Modal.Footer>
        <Button type="button" variant="light" disabled={saving} onClick={onHide}>
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to process the permission configuration. Please try again.";
}

export default PermissionConfigureModal;
