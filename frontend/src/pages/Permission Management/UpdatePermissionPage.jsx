import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconClock, IconDeviceFloppy, IconInfoCircle } from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/UpdatePermissionPage.css";
import {
  listPermissionProjects,
  listPermissionRoles,
  updatePermission,
  viewPermission,
} from "../../config/permissionApi/permissionApi.js";
import {
  PermissionFormField,
  PermissionPage,
} from "./PermissionComponents.jsx";
import { formatPermissionDate } from "./permissionUtils.js";

const initialPermission = {
  permissionName: "",
  permissionCode: "",
  permissionModule: "",
  permissionDescription: "",
  projectId: "",
  roleId: "",
  status: true,
  createdAt: null,
};

function UpdatePermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("edit");
  const [permission, setPermission] = useState(initialPermission);
  const [projects, setProjects] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadPage = async () => {
      if (!permissionId) {
        if (isActive) {
          setError("Permission id is missing. Please choose a permission from the list.");
          setLoadFailed(true);
          setLoading(false);
        }
        return;
      }

      try {
        setLoading(true);
        setError("");
        setLoadFailed(false);
        const [permissionResponse, projectResponse, roleResponse] = await Promise.all([
          viewPermission(permissionId),
          listPermissionProjects(),
          listPermissionRoles(),
        ]);
        const permissionPayload = permissionResponse.data?.data ?? permissionResponse.data;
        const projectPayload = projectResponse.data?.data ?? projectResponse.data;
        const rolePayload = roleResponse.data?.data ?? roleResponse.data;

        if (isActive) {
          setPermission({
            permissionName: permissionPayload?.permissionName || "",
            permissionCode: permissionPayload?.permissionCode || "",
            permissionModule: permissionPayload?.permissionModule || "",
            permissionDescription: permissionPayload?.permissionDescription || "",
            projectId: permissionPayload?.projectId ? String(permissionPayload.projectId) : "",
            roleId: permissionPayload?.roleId ? String(permissionPayload.roleId) : "",
            status: permissionPayload?.status ?? true,
            createdAt: permissionPayload?.createdAt || null,
          });
          setProjects(Array.isArray(projectPayload) ? projectPayload : []);
          setRoles(Array.isArray(rolePayload) ? rolePayload : []);
        }
      } catch (requestError) {
        console.error("Unable to load permission for update:", requestError);
        if (isActive) {
          setError(getErrorMessage(requestError));
          setLoadFailed(true);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    };

    loadPage();

    return () => {
      isActive = false;
    };
  }, [permissionId]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setPermission((currentPermission) => ({
      ...currentPermission,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!permissionId) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      const response = await updatePermission(permissionId, {
        permissionName: permission.permissionName.trim(),
        permissionCode: permission.permissionCode.trim(),
        permissionModule: permission.permissionModule.trim(),
        permissionDescription: permission.permissionDescription.trim(),
        projectId: Number(permission.projectId),
        roleId: Number(permission.roleId),
        status: permission.status,
      });
      const updatedPermission = response.data?.data ?? response.data;
      navigate(`/permission/list?view=${updatedPermission?.id || permissionId}`);
    } catch (requestError) {
      console.error("Unable to update permission:", requestError);
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const goBack = () => {
    navigate(permissionId ? `/permission/list?view=${permissionId}` : "/permission/list");
  };
  const backAction = (
    <Button className="permission-secondary-button" onClick={goBack}>
      <IconArrowLeft size={18} /> Back
    </Button>
  );

  if (loading) {
    return (
      <PermissionPage title="Update Permission" description="Loading the permission information..." action={backAction}>
        <div className="permission-page-state"><Spinner animation="border" /> Loading permission...</div>
      </PermissionPage>
    );
  }

  if (loadFailed) {
    return (
      <PermissionPage title="Update Permission" description="The permission could not be opened." action={backAction}>
        <Alert variant="danger" className="permission-update-load-error">{error}</Alert>
      </PermissionPage>
    );
  }

  const noOptions = projects.length === 0 || roles.length === 0;

  return (
    <PermissionPage
      title="Update Permission"
      description="Review the current values and save only the changes you need."
      action={backAction}
    >
      <Form className="permission-update-form" onSubmit={handleSubmit}>
        {error && <Alert variant="danger">{error}</Alert>}
        {noOptions && (
          <Alert variant="warning">At least one project and one role are required to save this permission.</Alert>
        )}

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">1</span>
            <div>
              <h2>Permission information</h2>
              <p>Keep the name, code, module, and description clear for other users.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="update-permission-name" label="Permission Name" required>
                <Form.Control className="permission-input" name="permissionName" value={permission.permissionName} onChange={handleChange} maxLength={50} required />
              </PermissionFormField>
            </Col>
            <Col md={6}>
              <PermissionFormField controlId="update-permission-code" label="Permission Code" required hint="The code must remain unique.">
                <Form.Control className="permission-input" name="permissionCode" value={permission.permissionCode} onChange={handleChange} maxLength={50} required />
              </PermissionFormField>
            </Col>
            <Col md={6}>
              <PermissionFormField controlId="update-permission-module" label="Permission Module" required>
                <Form.Control className="permission-input" name="permissionModule" value={permission.permissionModule} onChange={handleChange} maxLength={255} required />
              </PermissionFormField>
            </Col>
            <Col xs={12}>
              <PermissionFormField controlId="update-permission-description" label="Description">
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
              <p>Change the project, role, or availability when business rules change.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="update-permission-project" label="Project" required>
                <Form.Select className="permission-input" name="projectId" value={permission.projectId} onChange={handleChange} disabled={projects.length === 0} required>
                  <option value="" disabled>Select project</option>
                  {projects.map((project) => (
                    <option key={project.id} value={project.id}>{formatProjectName(project)}</option>
                  ))}
                </Form.Select>
              </PermissionFormField>
            </Col>
            <Col md={6}>
              <PermissionFormField controlId="update-permission-role" label="Role" required>
                <Form.Select className="permission-input" name="roleId" value={permission.roleId} onChange={handleChange} disabled={roles.length === 0} required>
                  <option value="" disabled>Select role</option>
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
                  <span>Turn this off when the permission should no longer be available.</span>
                </div>
                <Form.Check
                  type="switch"
                  id="update-permission-status"
                  label={permission.status ? "Active" : "Inactive"}
                  checked={permission.status}
                  onChange={(event) => setPermission((current) => ({ ...current, status: event.target.checked }))}
                />
              </div>
            </Col>
          </Row>
        </Card>

        <div className="permission-update-audit">
          <IconInfoCircle size={19} />
          <div>
            <strong>Audit information</strong>
            <span><IconClock size={16} /> Created: {formatPermissionDate(permission.createdAt)}</span>
          </div>
        </div>

        <div className="permission-form-actions">
          <Button className="permission-secondary-button" onClick={goBack} disabled={saving}>Cancel</Button>
          <Button type="submit" className="permission-primary-button" disabled={saving || noOptions}>
            {saving ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
            {saving ? "Saving..." : "Save Changes"}
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
    || "Unable to load or update permission. Please try again later.";
}

export default UpdatePermissionPage;
