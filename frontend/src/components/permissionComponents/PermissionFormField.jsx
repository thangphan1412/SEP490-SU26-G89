import { Form } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/PermissionComponents.css";

function PermissionFormField({ controlId, label, required = false, hint, children }) {
  return (
    <Form.Group controlId={controlId} className="permission-field">
      <Form.Label>
        {label} {required && <span className="permission-required">*</span>}
      </Form.Label>
      {children}
      {hint && <Form.Text>{hint}</Form.Text>}
    </Form.Group>
  );
}

export default PermissionFormField;
