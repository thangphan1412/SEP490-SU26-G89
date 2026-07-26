import { useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
  configureProjectPermission,
  listProjectPermissionConfigurations,
} from "../../services/projectService/projectApi.js";
import {
  permissionActionOptions,
  permissionWorkScopeOptions,
} from "../permissionComponents/permissionModuleOptions.js";
import PermissionConfigureRow from "./PermissionConfigureRow.jsx";
import "../../assets/styles/css/projectStyles/PermissionConfigureModal.css";

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

  useEffect(function () {
    if (!show) {
      return undefined;
    }

    let isActive = true;
    const requestController = new AbortController();

    async function loadPermissions() {
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
        const payload = await listProjectPermissionConfigurations(
          projectId,
          requestController.signal
        );

        if (isActive) {
          setPermissions(Array.isArray(payload) ? payload : []);
        }
      } catch (requestError) {
        if (!isActive) {
          return;
        }

        console.error("Unable to load project permissions:", requestError);
        setPermissions([]);
        setError(getErrorMessage(requestError));
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadPermissions();

    return function () {
      isActive = false;
      requestController.abort();
    };
  }, [projectId, show]);

  function openConfiguration(permission) {
    setSelectedPermissionId(permission.permissionId);
    setConfiguration({
      allowedActions: Array.isArray(permission.allowedActions)
        ? [...permission.allowedActions]
        : [],
      workScope: permission.workScope === "OWN" ? "OWN" : "FULL",
    });
    setError("");
    setSuccessMessage("");
  }

  function toggleAction(action) {
    setConfiguration(function (currentConfiguration) {
      const currentActions = currentConfiguration.allowedActions;
      const hasAction = currentActions.includes(action);
      let allowedActions;

      if (hasAction) {
        allowedActions = currentActions.filter((currentAction) => currentAction !== action);
      } else {
        allowedActions = [...currentActions, action];
      }

      return { ...currentConfiguration, allowedActions };
    });
  }

  async function saveConfiguration() {
    if (!selectedPermissionId) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");
      const updatedPermission = await configureProjectPermission(
        projectId,
        selectedPermissionId,
        configuration
      );

      setPermissions(function (currentPermissions) {
        const updatedPermissions = [];

        for (const permission of currentPermissions) {
          if (permission.permissionId === selectedPermissionId) {
            updatedPermissions.push(updatedPermission);
          } else {
            updatedPermissions.push(permission);
          }
        }

        return updatedPermissions;
      });
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
  }

  function handleModalHide() {
    if (!saving) {
      onHide();
    }
  }

  function changeWorkScope(workScope) {
    setConfiguration(function (currentConfiguration) {
      return {
        ...currentConfiguration,
        workScope,
      };
    });
  }

  return (
    <Modal
      show={show}
      onHide={handleModalHide}
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
                workScopeOptions={permissionWorkScopeOptions}
                saving={saving}
                onConfigure={openConfiguration}
                onToggleAction={toggleAction}
                onScopeChange={changeWorkScope}
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
