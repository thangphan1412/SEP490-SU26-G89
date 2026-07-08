import { useNavigate } from "react-router-dom";
import { Button, Card, Container, Form, Stack, Table } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/ListPermissionPage.css";
import { ListPermissionStatusBadge } from "./PermissionComponents.jsx";

const permissions = [
  ["View Contracts", "Contracts", 28, "Active", "May 22, 2025", "View"],
  ["Create Contracts", "Contracts", 16, "Active", "May 22, 2025", "Add"],
  ["Edit Contracts", "Contracts", 14, "Active", "May 21, 2025", "Edit"],
  ["Delete Contracts", "Contracts", 6, "Inactive", "May 20, 2025", "Del"],
  ["Manage Parties", "Parties", 22, "Active", "May 21, 2025", "Users"],
  ["View Reports", "Reports", 31, "Active", "May 20, 2025", "Rpt"],
  ["Manage Users", "Users", 5, "Active", "May 19, 2025", "Usr"],
  ["System Settings", "Settings", 3, "Inactive", "May 18, 2025", "Set"],
];

function ListPermissionPage() {
  const navigate = useNavigate();

  return (
    <Container fluid as="main" className="list-page">
      <Card className="list-card">
        <Card.Header className="list-header">
          <div>
            <Card.Title as="h1">Permissions</Card.Title>
            <Card.Text>Manage system permissions and access controls.</Card.Text>
          </div>

          <Button
            type="button"
            className="list-primary-button"
            onClick={() => navigate("/permission/create")}
          >
            + New Permission
          </Button>
        </Card.Header>

        <div className="list-toolbar">
          <Form.Control
            className="list-search"
            placeholder="Search permissions..."
          />

          <Form.Select className="list-select" defaultValue="All">
            <option>All</option>
            <option>Contracts</option>
            <option>Reports</option>
          </Form.Select>

          <Form.Select className="list-select" defaultValue="All">
            <option>All</option>
            <option>Active</option>
            <option>Inactive</option>
          </Form.Select>

          <Button type="button" variant="light" className="list-outline-button">
            Filters
          </Button>

          <Button type="button" variant="light" className="list-outline-button">
            Refresh
          </Button>
        </div>

        <div className="list-table-wrapper">
          <Table hover responsive={false} className="list-table mb-0">
            <thead>
              <tr>
                <th>Permission Name</th>
                <th>Module</th>
                <th>Users</th>
                <th>Status</th>
                <th>Updated At</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {permissions.map(
                ([name, moduleName, users, status, updatedAt, icon]) => (
                  <tr key={name}>
                    <td>
                      <Button
                        type="button"
                        variant="link"
                        className="list-name-button"
                        onClick={() => navigate("/permission/view")}
                      >
                        <span className="list-icon">{icon}</span>
                        <strong>{name}</strong>
                      </Button>
                    </td>
                    <td>{moduleName}</td>
                    <td>{users}</td>
                    <td>
                      <ListPermissionStatusBadge status={status} />
                    </td>
                    <td>{updatedAt}</td>
                    <td>
                      <Button
                        type="button"
                        variant="light"
                        className="list-action-button"
                        onClick={() => navigate("/permission/update")}
                      >
                        ...
                      </Button>
                    </td>
                  </tr>
                )
              )}
            </tbody>
          </Table>
        </div>

        <div className="list-footer">
          <span>Showing 1 to 8 of 8 results</span>

          <Stack direction="horizontal" className="list-pages">
            <Button type="button" variant="light">{"<"}</Button>
            <Button type="button" variant="light" className="active">
              1
            </Button>
            <Button type="button" variant="light">{">"}</Button>

            <Form.Select defaultValue="10 / page">
              <option>10 / page</option>
            </Form.Select>
          </Stack>
        </div>
      </Card>
    </Container>
  );
}

export default ListPermissionPage;
