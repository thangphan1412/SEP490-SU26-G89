import { useLayoutEffect } from "react";
import { Badge, Card, Container, Form } from "react-bootstrap";
import { IconShieldCheck } from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/PermissionComponents.css";

function useHideMainHeader() {
  useLayoutEffect(() => {
    const header = document.querySelector(".header-container-fluid");

    if (!header) {
      return undefined;
    }

    const wasHidden = header.hidden;
    header.hidden = true;

    return () => {
      header.hidden = wasHidden;
    };
  }, []);
}

export function PermissionPage({ title, description, action, children }) {
  useHideMainHeader();

  return (
    <Container fluid as="main" className="permission-page">
      <Card as="section" className="permission-panel">
        <Card.Header className="permission-page-header">
          <div className="permission-heading">
            <span className="permission-heading-icon">
              <IconShieldCheck size={28} stroke={1.8} />
            </span>
            <div>
              <h1>{title}</h1>
              <p>{description}</p>
            </div>
          </div>
          {action}
        </Card.Header>
        {children}
      </Card>
    </Container>
  );
}

export function PermissionFormField({ controlId, label, required = false, hint, children }) {
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

export function PermissionStatusBadge({ status }) {
  if (status === null || status === undefined) {
    return <Badge className="permission-status permission-status--unset">Not set</Badge>;
  }

  const statusClass = status ? "permission-status--active" : "permission-status--inactive";
  return <Badge className={`permission-status ${statusClass}`}>{status ? "Active" : "Inactive"}</Badge>;
}

export function ViewPermissionInfo({ label, value }) {
  const displayValue = value === null || value === undefined || value === "" ? "-" : value;

  return (
    <div className="permission-info-item">
      <p className="permission-info-label">{label}</p>
      <p className="permission-info-value">{displayValue}</p>
    </div>
  );
}
