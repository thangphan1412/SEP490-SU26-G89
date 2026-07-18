import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import {
  IconArrowLeft,
  IconClock,
  IconDeviceFloppy,
  IconInfoCircle,
} from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/CreatePermissionPage.css";
import {
  createPermission,
  listPermissionProjects,
  listPermissionRoles,
} from "../../config/permissionApi/permissionApi.js";
import { PermissionFormField, PermissionPage } from "./PermissionComponents.jsx";

const initialPermission = {
  permissionName: "",
  permissionCode: "",
  permissionModule: "",
  permissionDescription: "",
  projectId: "",
  roleId: "",
  status: true,
};

function CreatePermissionPage() {
  const navigate = useNavigate();
  const [permission, setPermission] = useState(initialPermission);
  const [projects, setProjects] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadOptions = async () => {
      try {
        const [projectResponse, roleResponse] = await Promise.all([
          listPermissionProjects(),
          listPermissionRoles(),
        ]);
        const projectPayload = projectResponse.data?.data ?? projectResponse.data;
        const rolePayload = roleResponse.data?.data ?? roleResponse.data;

        if (isActive) {
          setProjects(Array.isArray(projectPayload) ? projectPayload : []);
          setRoles(Array.isArray(rolePayload) ? rolePayload : []);
        }
      } catch (requestError) {
        console.error("Unable to load permission options:", requestError);
        if (isActive) {
          setError("Unable to load projects or roles. Please try again later.");
        }
      } finally {
        if (isActive) {
          setLoadingOptions(false);
        }
      }
    };

    loadOptions();

    return () => {
      isActive = false;
    };
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setPermission((currentPermission) => ({
      ...currentPermission,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    try {
      setSaving(true);
      const response = await createPermission({
        permissionName: permission.permissionName.trim(),
        permissionCode: permission.permissionCode.trim(),
        permissionModule: permission.permissionModule.trim(),
        permissionDescription: permission.permissionDescription.trim(),
        projectId: Number(permission.projectId),
        roleId: Number(permission.roleId),
        status: permission.status,
      });
      const createdPermission = response.data?.data ?? response.data;

      if (createdPermission?.id) {
        navigate(`/permission/list?view=${createdPermission.id}`);
        return;
      }

      navigate("/permission/list");
    } catch (requestError) {
      console.error("Unable to create permission:", requestError);
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const backAction = (
    <Button className="permission-secondary-button" onClick={() => navigate("/permission/list")}>
      <IconArrowLeft size={18} /> Back to list
    </Button>
  );
  const noOptions = !loadingOptions && (projects.length === 0 || roles.length === 0);

  return (
    <PermissionPage
      title="Create Permission"
      description="Define a clear access rule and assign it to a project and role."
      action={backAction}
    >
      <Form className="permission-create-form" onSubmit={handleSubmit}>
        {error && <Alert variant="danger">{error}</Alert>}
        {noOptions && (
          <Alert variant="warning">
            At least one project and one role are required before a permission can be created.
          </Alert>
        )}

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">1</span>
            <div>
              <h2>Permission information</h2>
              <p>Use a short name and a unique code that are easy to understand.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="permission-name" label="Permission Name" required>
                <Form.Control
                  className="permission-input"
                  name="permissionName"
                  value={permission.permissionName}
                  onChange={handleChange}
                  placeholder="Example: View contracts"
                  maxLength={50}
                  required
                />
              </PermissionFormField>
            </Col>

            <Col md={6}>
              <PermissionFormField controlId="permission-code" label="Permission Code" required hint="Use a unique code, for example CONTRACT_VIEW.">
                <Form.Control
                  className="permission-input"
                  name="permissionCode"
                  value={permission.permissionCode}
                  onChange={handleChange}
                  placeholder="CONTRACT_VIEW"
                  maxLength={50}
                  required
                />
              </PermissionFormField>
            </Col>

            <Col md={6}>
              <PermissionFormField controlId="permission-module" label="Permission Module" required hint="The feature area protected by this permission.">
                <Form.Control
                  className="permission-input"
                  name="permissionModule"
                  value={permission.permissionModule}
                  onChange={handleChange}
                  placeholder="CONTRACT"
                  maxLength={255}
                  required
                />
              </PermissionFormField>
            </Col>

            <Col xs={12}>
              <PermissionFormField controlId="permission-description" label="Description">
                <div className="permission-description-box">
                  <Form.Control
                    as="textarea"
                    className="permission-textarea"
                    name="permissionDescription"
                    value={permission.permissionDescription}
                    onChange={handleChange}
                    placeholder="Explain what this permission allows a user to do..."
                    maxLength={255}
                  />
                  <span>{permission.permissionDescription.length} / 255</span>
                </div>
              </PermissionFormField>
            </Col>
          </Row>
        </Card>

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">2</span>
            <div>
              <h2>Assignment and status</h2>
              <p>Choose where this permission is used and who receives it.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="permission-project" label="Project" required>
                <Form.Select
                  className="permission-input"
                  name="projectId"
                  value={permission.projectId}
                  onChange={handleChange}
                  disabled={loadingOptions || projects.length === 0}
                  required
                >
                  <option value="" disabled>{loadingOptions ? "Loading projects..." : "Select project"}</option>
                  {projects.map((project) => (
                    <option key={project.id} value={project.id}>{formatProjectName(project)}</option>
                  ))}
                </Form.Select>
              </PermissionFormField>
            </Col>

            <Col md={6}>
              <PermissionFormField controlId="permission-role" label="Role" required>
                <Form.Select
                  className="permission-input"
                  name="roleId"
                  value={permission.roleId}
                  onChange={handleChange}
                  disabled={loadingOptions || roles.length === 0}
                  required
                >
                  <option value="" disabled>{loadingOptions ? "Loading roles..." : "Select role"}</option>
                  {roles.map((role) => (
                    <option key={role.id} value={role.id}>{role.roleName || `Role #${role.id}`}</option>
                  ))}
                </Form.Select>
              </PermissionFormField>
            </Col>

            <Col xs={12}>
              <div className="permission-status-control">
                <div>
                  <strong>Permission status</strong>
                  <span>Inactive permissions remain stored but are marked as unavailable.</span>
                </div>
                <Form.Check
                  type="switch"
                  id="permission-status"
                  label={permission.status ? "Active" : "Inactive"}
                  checked={permission.status}
                  onChange={(event) => setPermission((current) => ({ ...current, status: event.target.checked }))}
                />
              </div>
            </Col>
          </Row>
        </Card>

        <div className="permission-audit-note">
          <IconInfoCircle size={19} />
          <span><IconClock size={16} /> Creation time is recorded automatically by the server.</span>
        </div>

        <div className="permission-form-actions">
          <Button className="permission-secondary-button" onClick={() => navigate("/permission/list")} disabled={saving}>
            Cancel
          </Button>
          <Button type="submit" className="permission-primary-button" disabled={saving || loadingOptions || noOptions}>
            {saving ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
            {saving ? "Creating..." : "Create Permission"}
          </Button>
        </div>
      </Form>
    </PermissionPage>
  );
}

function formatProjectName(project) {
  const projectCode = project.projectCode ? `${project.projectCode} - ` : "";
  return `${projectCode}${project.projectName || `Project #${project.id}`}`;
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to save permission. Please check the information and try again.";
}

export default CreatePermissionPage;
