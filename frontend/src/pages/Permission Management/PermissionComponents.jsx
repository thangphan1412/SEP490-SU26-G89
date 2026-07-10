import { Form } from "react-bootstrap";

export function CreatePermissionFormField({ label, required = false, children }) {
  return (
    <Form.Group className="create-field">
      <Form.Label className="create-label">
        {label} {required && <span className="create-required">*</span>}
      </Form.Label>
      {children}
    </Form.Group>
  );
}

export function UpdatePermissionFormField({ label, required = false, children }) {
  return (
    <Form.Group className="update-field">
      <Form.Label className="update-label">
        {label} {required && <span className="update-required">*</span>}
      </Form.Label>
      {children}
    </Form.Group>
  );
}

export function ViewPermissionInfo({ label, value }) {
  return (
    <div>
      <p className="view-info-label">{label}</p>
      <p className="view-info-value">{value || "-"}</p>
    </div>
  );
}
