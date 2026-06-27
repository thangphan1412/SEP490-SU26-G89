import { useNavigate } from "react-router-dom";
import "./Permission Management Styles/ViewPermissionPage.css";
import InfoComponent from "./ViewPermissionComponents/InfoComponent.jsx";
import RoleComponent from "./ViewPermissionComponents/RoleComponent.jsx";
import StatusBadgeComponent from "./ViewPermissionComponents/StatusBadgeComponent.jsx";

const auditRows = [
  ["Created", "Alex Morgan", "May 10, 2025 10:15 AM", "Permission created"],
  ["Updated", "Alex Morgan", "May 22, 2025 02:30 PM", "Updated description"],
  ["Status Changed", "Alex Morgan", "May 22, 2025 02:31 PM", "Active permission"],
];

function ViewPermissionPage() {
  const navigate = useNavigate();

  return (
    <div className="view-page">
      <div className="view-card">
        <div className="view-header">
          <div className="view-title-row">
            <button
              type="button"
              className="view-back-button"
              onClick={() => navigate("/permission/list")}
            >
              {"<"}
            </button>

            <div>
              <h1>View Permission</h1>
              <p>View permission details and assigned roles.</p>
            </div>
          </div>

          <div className="view-actions">
            <button
              type="button"
              className="view-primary-button"
              onClick={() => navigate("/permission/update")}
            >
              Edit Permission
            </button>
            <button type="button" className="view-more-button">
              ...
            </button>
          </div>
        </div>

        <section className="view-section">
          <div className="view-section-title-row">
            <h2>Permission Overview</h2>
            <StatusBadgeComponent text="Active" />
          </div>

          <div className="view-overview-grid">
            <div className="view-shield">OK</div>

            <div className="view-info-column">
              <InfoComponent label="Permission Name" value="View Contracts" />
              <InfoComponent label="Module" value="Contracts" />
              <InfoComponent
                label="Description"
                value="Allows users to view contract records and details."
              />
            </div>

            <div className="view-info-column">
              <InfoComponent label="Access Level" value="Read Only" pill />
              <InfoComponent label="Scope" value="System Wide" />
            </div>

            <div className="view-info-column">
              <InfoComponent label="Created By" value="Alex Morgan" />
              <p className="view-date">May 10, 2025 10:15 AM</p>
              <InfoComponent label="Updated By" value="Alex Morgan" />
              <p className="view-date">May 22, 2025 02:30 PM</p>
            </div>
          </div>
        </section>

        <section className="view-section">
          <h2>Assigned Roles (4)</h2>
          <div className="view-role-list">
            <RoleComponent text="Contract Manager" />
            <RoleComponent text="Contract Viewer" />
            <RoleComponent text="Admin" />
            <RoleComponent text="Compliance Officer" />
          </div>
        </section>

        <section className="view-section">
          <h2>Audit Trail</h2>
          <table className="view-table">
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
          </table>
        </section>
      </div>
    </div>
  );
}

export default ViewPermissionPage;
