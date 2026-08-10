import { useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { listProjectPermissionConfigurations } from "../../services/projectService/projectApi.js";
import Icon from "./Icon.jsx";
import PermissionConfigureRow from "./PermissionConfigureRow.jsx";
import "../../assets/styles/css/projectStyles/PermissionConfigureModal.css";

function PermissionConfigureModal({ show, projectId, projectName, onHide }) {
  const navigate = useNavigate();
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(function () {
    if (!show) {
      return undefined;
    }

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
        const payload = await listProjectPermissionConfigurations(
          projectId,
          requestController.signal
        );

        if (requestController.signal.aborted) {
          return;
        }

        setPermissions(Array.isArray(payload) ? payload : []);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load project permissions:", requestError);
        setPermissions([]);
        setError(getErrorMessage(requestError));
      } finally {
        if (!requestController.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadPermissions();

    return function () {
      requestController.abort();
    };
  }, [projectId, show]);

  function openPermissionUpdate(permission) {
    if (!permission?.permissionId) {
      return;
    }

    onHide();
    navigate(
      `/permission/update?edit=${encodeURIComponent(permission.permissionId)}`
        + `&returnProjectId=${encodeURIComponent(projectId)}`
    );
  }

  function openCreatePermission() {
    if (!projectId) {
      return;
    }

    const encodedProjectId = encodeURIComponent(projectId);
    onHide();
    navigate(
      `/permission/create?projectId=${encodedProjectId}`
        + `&returnProjectId=${encodedProjectId}`
    );
  }

  return (
    <Modal
      show={show}
      onHide={onHide}
      centered
      scrollable
      size="lg"
      className="permission-configure-modal"
    >
      <Modal.Header closeButton>
        <div>
          <Modal.Title>Permission Configure</Modal.Title>
          <p>
            Configure access for {projectName || "this project"}.
          </p>
        </div>
      </Modal.Header>

      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}

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
                onConfigure={openPermissionUpdate}
              />
            ))}
          </div>
        )}
      </Modal.Body>

      <Modal.Footer>
        <Button type="button" variant="light" onClick={onHide}>
          Close
        </Button>
        <Button
          type="button"
          disabled={!projectId}
          className="permission-configure-add-button"
          onClick={openCreatePermission}
        >
          <Icon name="plus" size={18} color="#fff" />
          Add Permission
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
