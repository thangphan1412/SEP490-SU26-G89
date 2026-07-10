import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Card, Container, Form, Stack } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/CreatePermissionPage.css";
import { createPermission, listPermissionProjects } from "../../config/axiosConfig.js";
import { CreatePermissionFormField } from "./PermissionComponents.jsx";

const initialPermission = {
  permissionName: "",
  permissionCode: "",
  permissionModule: "",
  projectId: "",
};

function CreatePermissionPage() {
  const navigate = useNavigate();
  const [permission, setPermission] = useState(initialPermission);
  const [projects, setProjects] = useState([]);
  const [loadingProjects, setLoadingProjects] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadProjects = async () => {
      try {
        const response = await listPermissionProjects();
        const payload = response.data?.data ?? response.data;

        if (isActive) {
          setProjects(Array.isArray(payload) ? payload : []);
        }
      } catch (requestError) {
        console.error("Unable to load projects for permission:", requestError);

        if (isActive) {
          setError("Unable to load projects. Please try again later.");
        }
      } finally {
        if (isActive) {
          setLoadingProjects(false);
        }
      }
    };

    loadProjects();

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
        projectId: Number(permission.projectId),
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

  return (
    <Container fluid as="main" className="create-page">
      <Card className="create-card">
        <Card.Header className="create-header">
          <Card.Title as="h1">Create Permission</Card.Title>
          <Card.Text>Enter the information stored for this permission.</Card.Text>
        </Card.Header>

        <Form className="create-form" onSubmit={handleSubmit}>
          {error && <Alert variant="danger" className="mb-0">{error}</Alert>}

          <CreatePermissionFormField label="Permission Name" required>
            <Form.Control
              className="create-input"
              name="permissionName"
              value={permission.permissionName}
              onChange={handleChange}
              placeholder="Enter permission name"
              maxLength={50}
              required
            />
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Permission Code" required>
            <Form.Control
              className="create-input"
              name="permissionCode"
              value={permission.permissionCode}
              onChange={handleChange}
              placeholder="Example: CONTRACT_VIEW"
              maxLength={50}
              required
            />
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Permission Module" required>
            <Form.Control
              className="create-input"
              name="permissionModule"
              value={permission.permissionModule}
              onChange={handleChange}
              placeholder="Example: CONTRACT"
              maxLength={255}
              required
            />
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Project" required>
            <Form.Select
              className="create-input"
              name="projectId"
              value={permission.projectId}
              onChange={handleChange}
              disabled={loadingProjects || projects.length === 0}
              required
            >
              <option value="" disabled>
                {loadingProjects ? "Loading projects..." : "Select project"}
              </option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {formatProjectName(project)}
                </option>
              ))}
            </Form.Select>
          </CreatePermissionFormField>

          <Stack direction="horizontal" className="create-actions">
            <Button
              type="button"
              variant="light"
              className="create-cancel-button"
              disabled={saving}
              onClick={() => navigate("/permission/list")}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              className="create-primary-button"
              disabled={saving || loadingProjects || projects.length === 0}
            >
              {saving ? "Creating..." : "Create Permission"}
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
    || "Unable to save permission. Please check the information and try again.";
}

export default CreatePermissionPage;
