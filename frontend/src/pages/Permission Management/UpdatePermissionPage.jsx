import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Container, Form, Spinner, Stack } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/UpdatePermissionPage.css";
import {
  listPermissionProjects,
  updatePermission,
  viewPermission,
} from "../../config/axiosConfig.js";
import { UpdatePermissionFormField } from "./PermissionComponents.jsx";

const initialPermission = {
  permissionName: "",
  permissionCode: "",
  permissionModule: "",
  projectId: "",
};

function UpdatePermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("edit");
  const [permission, setPermission] = useState(initialPermission);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadPage = async () => {
      if (!permissionId) {
        if (isActive) {
          setError("Permission id is missing. Please choose a permission from the list.");
          setLoading(false);
        }
        return;
      }

      try {
        setLoading(true);
        setError("");
        const [permissionResponse, projectResponse] = await Promise.all([
          viewPermission(permissionId),
          listPermissionProjects(),
        ]);
        const permissionPayload = permissionResponse.data?.data ?? permissionResponse.data;
        const projectPayload = projectResponse.data?.data ?? projectResponse.data;

        if (isActive) {
          setPermission({
            permissionName: permissionPayload?.permissionName || "",
            permissionCode: permissionPayload?.permissionCode || "",
            permissionModule: permissionPayload?.permissionModule || "",
            projectId: permissionPayload?.projectId ? String(permissionPayload.projectId) : "",
          });
          setProjects(Array.isArray(projectPayload) ? projectPayload : []);
        }
      } catch (requestError) {
        console.error("Unable to load permission for update:", requestError);

        if (isActive) {
          setError(getErrorMessage(requestError));
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
        projectId: Number(permission.projectId),
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

  if (loading) {
    return (
      <Container fluid as="main" className="update-page">
        <Card className="update-card">
          <div className="update-state"><Spinner animation="border" /> Loading permission...</div>
        </Card>
      </Container>
    );
  }

  if (error && !permissionId) {
    return (
      <Container fluid as="main" className="update-page">
        <Card className="update-card"><Alert variant="danger" className="m-4">{error}</Alert></Card>
      </Container>
    );
  }

  return (
    <Container fluid as="main" className="update-page">
      <Card className="update-card">
        <Card.Header className="update-header">
          <Card.Title as="h1">Update Permission</Card.Title>
          <Card.Text>Update the permission information saved in the system.</Card.Text>
        </Card.Header>

        <Form className="update-form" onSubmit={handleSubmit}>
          {error && <Alert variant="danger" className="mb-0">{error}</Alert>}

          <UpdatePermissionFormField label="Permission Name" required>
            <Form.Control
              className="update-input"
              name="permissionName"
              value={permission.permissionName}
              onChange={handleChange}
              maxLength={50}
              required
            />
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Permission Code" required>
            <Form.Control
              className="update-input"
              name="permissionCode"
              value={permission.permissionCode}
              onChange={handleChange}
              maxLength={50}
              required
            />
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Permission Module" required>
            <Form.Control
              className="update-input"
              name="permissionModule"
              value={permission.permissionModule}
              onChange={handleChange}
              maxLength={255}
              required
            />
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Project" required>
            <Form.Select
              className="update-input"
              name="projectId"
              value={permission.projectId}
              onChange={handleChange}
              disabled={projects.length === 0}
              required
            >
              <option value="" disabled>Select project</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {formatProjectName(project)}
                </option>
              ))}
            </Form.Select>
          </UpdatePermissionFormField>

          <Stack direction="horizontal" className="update-actions">
            <Button
              type="button"
              variant="light"
              className="update-cancel-button"
              disabled={saving}
              onClick={() => navigate(`/permission/list?view=${permissionId}`)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              className="update-primary-button"
              disabled={saving || projects.length === 0}
            >
              {saving ? "Saving..." : "Update Permission"}
            </Button>
          </Stack>
        </Form>
      </Card>
    </Container>
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
