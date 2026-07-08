import { useNavigate } from "react-router-dom";
import { Button, Card, Container, Stack, Table } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/ViewPermissionPage.css";
import {
  ViewPermissionInfo,
  ViewPermissionRole,
  ViewPermissionStatusBadge,
} from "./PermissionComponents.jsx";

const auditRows = [
  ["Created", "Alex Morgan", "May 10, 2025 10:15 AM", "Permission created"],
  ["Updated", "Alex Morgan", "May 22, 2025 02:30 PM", "Updated description"],
  ["Status Changed", "Alex Morgan", "May 22, 2025 02:31 PM", "Active permission"],
];

function ViewPermissionPage() {
  const navigate = useNavigate();

  return (
    <Container fluid as="main" className="view-page">
      <Card className="view-card">
        <Card.Header className="view-header">
          <div className="view-title-row">
            <Button
              type="button"
              variant="light"
              className="view-back-button"
              onClick={() => navigate("/permission/list")}
            >
              {"<"}
            </Button>

            <div>
              <Card.Title as="h1">View Permission</Card.Title>
              <Card.Text>View permission details and assigned roles.</Card.Text>
            </div>
          </div>

          <Stack direction="horizontal" className="view-actions">
            <Button
              type="button"
              className="view-primary-button"
              onClick={() => navigate("/permission/update")}
            >
              Edit Permission
            </Button>
            <Button type="button" variant="light" className="view-more-button">
              ...
            </Button>
          </Stack>
        </Card.Header>

        <Card as="section" className="view-section">
          <div className="view-section-title-row">
            <Card.Title as="h2">Permission Overview</Card.Title>
            <ViewPermissionStatusBadge text="Active" />
          </div>

          <div className="view-overview-grid">
            <div className="view-shield">OK</div>

            <div className="view-info-column">
              <ViewPermissionInfo label="Permission Name" value="View Contracts" />
              <ViewPermissionInfo label="Module" value="Contracts" />
              <ViewPermissionInfo
                label="Description"
                value="Allows users to view contract records and details."
              />
            </div>

            <div className="view-info-column">
              <ViewPermissionInfo label="Access Level" value="Read Only" pill />
              <ViewPermissionInfo label="Scope" value="System Wide" />
            </div>

            <div className="view-info-column">
              <ViewPermissionInfo label="Created By" value="Alex Morgan" />
              <p className="view-date">May 10, 2025 10:15 AM</p>
              <ViewPermissionInfo label="Updated By" value="Alex Morgan" />
              <p className="view-date">May 22, 2025 02:30 PM</p>
            </div>
          </div>
        </Card>

        <Card as="section" className="view-section">
          <Card.Title as="h2">Assigned Roles (4)</Card.Title>
          <Stack direction="horizontal" className="view-role-list">
            <ViewPermissionRole text="Contract Manager" />
            <ViewPermissionRole text="Contract Viewer" />
            <ViewPermissionRole text="Admin" />
            <ViewPermissionRole text="Compliance Officer" />
          </Stack>
        </Card>

        <Card as="section" className="view-section">
          <Card.Title as="h2">Audit Trail</Card.Title>
          <Table hover responsive={false} className="view-table mb-0">
            <thead>
              <tr>
                <th>Action</th>
                <th>By</th>
                <th>Date & Time</th>
                <th>Details</th>
              </tr>
            </thead>

            <tbody>
              {auditRows.map(([action, by, date, detail]) => (
                <tr key={`${action}-${date}`}>
                  <td>{action}</td>
                  <td>{by}</td>
                  <td>{date}</td>
                  <td>{detail}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card>
      </Card>
    </Container>
  );
}

export default ViewPermissionPage;
