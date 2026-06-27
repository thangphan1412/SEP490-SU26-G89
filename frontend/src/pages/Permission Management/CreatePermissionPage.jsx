import { useNavigate } from "react-router-dom";
import "./Permission Management Styles/CreatePermissionPage.css";
import FormFieldComponent from "./CreatePermissionComponents/FormFieldComponent.jsx";
import ScopeCardComponent from "./CreatePermissionComponents/ScopeCardComponent.jsx";
import TagComponent from "./CreatePermissionComponents/TagComponent.jsx";

function CreatePermissionPage() {
  const navigate = useNavigate();

  return (
    <div className="create-page">
      <div className="create-card">
        <div className="create-header">
          <h1>Create Permission</h1>
          <p>Define a new permission and assign its scope.</p>
        </div>

        <form className="create-form">
          <FormFieldComponent label="Permission Name" required>
            <input className="create-input" placeholder="Enter permission name" />
          </FormFieldComponent>

          <FormFieldComponent label="Module" required>
            <select className="create-input" defaultValue="">
              <option value="" disabled>
                Select module
              </option>
              <option>Contracts</option>
              <option>Reports</option>
              <option>Users</option>
            </select>
          </FormFieldComponent>

          <FormFieldComponent label="Description">
            <div className="create-textarea-box">
              <textarea
                className="create-textarea"
                placeholder="Describe what this permission allows users to do."
                maxLength={255}
              />
              <span>0 / 255</span>
            </div>
          </FormFieldComponent>

          <FormFieldComponent label="Permission Scope" required>
            <div className="create-scope-grid">
              <ScopeCardComponent
                title="System Wide"
                description="Applies across the entire system"
                active
              />
              <ScopeCardComponent
                title="Module Specific"
                description="Applies to a specific module"
              />
            </div>
          </FormFieldComponent>

          <FormFieldComponent label="Access Level" required>
            <select className="create-input" defaultValue="">
              <option value="" disabled>
                Select access level
              </option>
              <option>Read Only</option>
              <option>Write</option>
              <option>Admin</option>
            </select>
          </FormFieldComponent>

          <div className="create-status-row">
            <span className="create-label">Status</span>
            <span className="create-toggle" />
            <span>Active</span>
          </div>

          <FormFieldComponent label="Assign to Roles (Optional)">
            <div className="create-role-box">
              <TagComponent text="Contract Manager" />
              <TagComponent text="Legal Team" />
              <TagComponent text="Compliance Officer" />
            </div>
          </FormFieldComponent>

          <div className="create-actions">
            <button
              type="button"
              className="create-cancel-button"
              onClick={() => navigate("/permission/list")}
            >
              Cancel
            </button>
            <button type="button" className="create-primary-button">
              Create Permission
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreatePermissionPage;
