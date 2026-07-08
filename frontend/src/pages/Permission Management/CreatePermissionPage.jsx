import { useNavigate } from "react-router-dom";
import { Button, Card, Container, Form, Stack } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/CreatePermissionPage.css";
import {
  CreatePermissionFormField,
  CreatePermissionScopeCard,
  CreatePermissionTag,
} from "./PermissionComponents.jsx";

function CreatePermissionPage() {
  const navigate = useNavigate();

  return (
    <Container fluid as="main" className="create-page">
      <Card className="create-card">
        <Card.Header className="create-header">
          <Card.Title as="h1">Create Permission</Card.Title>
          <Card.Text>Define a new permission and assign its scope.</Card.Text>
        </Card.Header>

        <Form className="create-form">
          <CreatePermissionFormField label="Permission Name" required>
            <Form.Control
              className="create-input"
              placeholder="Enter permission name"
            />
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Module" required>
            <Form.Select className="create-input" defaultValue="">
              <option value="" disabled>
                Select module
              </option>
              <option>Contracts</option>
              <option>Reports</option>
              <option>Users</option>
            </Form.Select>
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Description">
            <div className="create-textarea-box">
              <Form.Control
                as="textarea"
                className="create-textarea"
                placeholder="Describe what this permission allows users to do."
                maxLength={255}
              />
              <span>0 / 255</span>
            </div>
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Permission Scope" required>
            <div className="create-scope-grid">
              <CreatePermissionScopeCard
                title="System Wide"
                description="Applies across the entire system"
                active
              />
              <CreatePermissionScopeCard
                title="Module Specific"
                description="Applies to a specific module"
              />
            </div>
          </CreatePermissionFormField>

          <CreatePermissionFormField label="Access Level" required>
            <Form.Select className="create-input" defaultValue="">
              <option value="" disabled>
                Select access level
              </option>
              <option>Read Only</option>
              <option>Write</option>
              <option>Admin</option>
            </Form.Select>
          </CreatePermissionFormField>

          <div className="create-status-row">
            <span className="create-label">Status</span>
            <Form.Check
              type="switch"
              id="create-permission-status"
              className="create-status-switch"
              defaultChecked
              label="Active"
            />
          </div>

          <CreatePermissionFormField label="Assign to Roles (Optional)">
            <Stack direction="horizontal" className="create-role-box">
              <CreatePermissionTag text="Contract Manager" />
              <CreatePermissionTag text="Legal Team" />
              <CreatePermissionTag text="Compliance Officer" />
            </Stack>
          </CreatePermissionFormField>

          <Stack direction="horizontal" className="create-actions">
            <Button
              type="button"
              variant="light"
              className="create-cancel-button"
              onClick={() => navigate("/permission/list")}
            >
              Cancel
            </Button>
            <Button type="button" className="create-primary-button">
              Create Permission
            </Button>
          </Stack>
        </Form>
      </Card>
    </Container>
  );
}

export default CreatePermissionPage;
