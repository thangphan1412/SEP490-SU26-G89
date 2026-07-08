import { useNavigate } from "react-router-dom";
import { Button, Card, Container, Form, Stack } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/UpdatePermissionPage.css";
import {
  UpdatePermissionFormField,
  UpdatePermissionRadioText,
  UpdatePermissionTag,
} from "./PermissionComponents.jsx";

function UpdatePermissionPage() {
  const navigate = useNavigate();

  return (
    <Container fluid as="main" className="update-page">
      <Card className="update-card">
        <Card.Header className="update-header">
          <Card.Title as="h1">Update Permission</Card.Title>
          <Card.Text>Modify permission details and settings.</Card.Text>
        </Card.Header>

        <Form className="update-form">
          <UpdatePermissionFormField label="Permission Name" required>
            <Form.Control
              className="update-input"
              defaultValue="Edit Contracts"
            />
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Module" required>
            <Form.Select className="update-input" defaultValue="Contracts">
              <option>Contracts</option>
              <option>Reports</option>
              <option>Users</option>
            </Form.Select>
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Description">
            <div className="update-textarea-box">
              <Form.Control
                as="textarea"
                className="update-textarea"
                defaultValue="Allows users to edit existing contract records and update contract details."
                maxLength={255}
              />
              <span>65 / 255</span>
            </div>
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Permission Scope">
            <div className="update-radio-group">
              <UpdatePermissionRadioText text="System Wide" active />
              <UpdatePermissionRadioText text="Module Specific" />
            </div>
          </UpdatePermissionFormField>

          <UpdatePermissionFormField label="Access Level" required>
            <Form.Select className="update-input" defaultValue="Write">
              <option>Read Only</option>
              <option>Write</option>
              <option>Admin</option>
            </Form.Select>
          </UpdatePermissionFormField>

          <div className="update-status-row">
            <span className="update-label">Status</span>
            <Form.Check
              type="switch"
              id="update-permission-status"
              className="update-status-switch"
              defaultChecked
              label="Active"
            />
          </div>

          <UpdatePermissionFormField label="Assign to Roles (Optional)">
            <Stack direction="horizontal" className="update-role-box">
              <UpdatePermissionTag text="Contract Manager" />
              <UpdatePermissionTag text="Legal Team" />
            </Stack>
          </UpdatePermissionFormField>

          <Stack direction="horizontal" className="update-actions">
            <Button
              type="button"
              variant="light"
              className="update-cancel-button"
              onClick={() => navigate("/permission/list")}
            >
              Cancel
            </Button>
            <Button type="button" className="update-primary-button">
              Update Permission
            </Button>
          </Stack>
        </Form>
      </Card>
    </Container>
  );
}

export default UpdatePermissionPage;
