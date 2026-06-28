import { useNavigate } from "react-router-dom";
import "./Permission Management Styles/ListPermissionPage.css";
import StatusBadgeComponent from "./ListPermissionComponents/StatusBadgeComponent.jsx";

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
    <div className="list-page">
      <div className="list-card">
        <div className="list-header">
          <div>
            <h1>Permissions</h1>
            <p>Manage system permissions and access controls.</p>
          </div>

          <button
            type="button"
            className="list-primary-button"
            onClick={() => navigate("/permission/create")}
          >
            + New Permission
          </button>
        </div>

        <div className="list-toolbar">
          <input className="list-search" placeholder="Search permissions..." />

          <select className="list-select" defaultValue="All">
            <option>All</option>
            <option>Contracts</option>
            <option>Reports</option>
          </select>

          <select className="list-select" defaultValue="All">
            <option>All</option>
            <option>Active</option>
            <option>Inactive</option>
          </select>

          <button type="button" className="list-outline-button">
            Filters
          </button>

          <button type="button" className="list-outline-button">
            Refresh
          </button>
        </div>

        <div className="list-table-wrapper">
          <table className="list-table">
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
                      <button
                        type="button"
                        className="list-name-button"
                        onClick={() => navigate("/permission/view")}
                      >
                        <span className="list-icon">{icon}</span>
                        <strong>{name}</strong>
                      </button>
                    </td>
                    <td>{moduleName}</td>
                    <td>{users}</td>
                    <td>
                      <StatusBadgeComponent status={status} />
                    </td>
                    <td>{updatedAt}</td>
                    <td>
                      <button
                        type="button"
                        className="list-action-button"
                        onClick={() => navigate("/permission/update")}
                      >
                        ...
                      </button>
                    </td>
                  </tr>
                )
              )}
            </tbody>
          </table>
        </div>

        <div className="list-footer">
          <span>Showing 1 to 8 of 8 results</span>

          <div className="list-pages">
            <button type="button">{"<"}</button>
            <button type="button" className="active">
              1
            </button>
            <button type="button">{">"}</button>

            <select defaultValue="10 / page">
              <option>10 / page</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ListPermissionPage;
