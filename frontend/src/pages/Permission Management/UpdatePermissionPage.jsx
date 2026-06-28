import { useNavigate } from "react-router-dom";
import "./Permission Management Styles/UpdatePermissionPage.css";
import FormFieldComponent from "./UpdatePermissionComponents/FormFieldComponent.jsx";
import RadioTextComponent from "./UpdatePermissionComponents/RadioTextComponent.jsx";
import TagComponent from "./UpdatePermissionComponents/TagComponent.jsx";

function UpdatePermissionPage() {
  const navigate = useNavigate();

  return (
    <div className="update-page">
      <div className="update-card">
        <div className="update-header">
          <h1>Update Permission</h1>
          <p>Modify permission details and settings.</p>
        </div>

        <form className="update-form">
          <FormFieldComponent label="Permission Name" required>
            <input className="update-input" defaultValue="Edit Contracts" />
          </FormFieldComponent>

          <FormFieldComponent label="Module" required>
            <select className="update-input" defaultValue="Contracts">
              <option>Contracts</option>
              <option>Reports</option>
              <option>Users</option>
            </select>
          </FormFieldComponent>

          <FormFieldComponent label="Description">
            <div className="update-textarea-box">
              <textarea
                className="update-textarea"
                defaultValue="Allows users to edit existing contract records and update contract details."
                maxLength={255}
              />
              <span>65 / 255</span>
            </div>
          </FormFieldComponent>

          <FormFieldComponent label="Permission Scope">
            <div className="update-radio-group">
              <RadioTextComponent text="System Wide" active />
              <RadioTextComponent text="Module Specific" />
            </div>
          </FormFieldComponent>

          <FormFieldComponent label="Access Level" required>
            <select className="update-input" defaultValue="Write">
              <option>Read Only</option>
              <option>Write</option>
              <option>Admin</option>
            </select>
          </FormFieldComponent>

          <div className="update-status-row">
            <span className="update-label">Status</span>
            <span className="update-toggle" />
            <span>Active</span>
          </div>

          <FormFieldComponent label="Assign to Roles (Optional)">
            <div className="update-role-box">
              <TagComponent text="Contract Manager" />
              <TagComponent text="Legal Team" />
            </div>
          </FormFieldComponent>

          <div className="update-actions">
            <button
              type="button"
              className="update-cancel-button"
              onClick={() => navigate("/permission/list")}
            >
              Cancel
            </button>
            <button type="button" className="update-primary-button">
              Update Permission
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default UpdatePermissionPage;
