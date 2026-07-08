import { Badge, Button, Form } from "react-bootstrap";

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

export function CreatePermissionScopeCard({ title, description, active = false }) {
  return (
    <Button
      type="button"
      variant="light"
      className="create-scope-card"
      aria-pressed={active}
    >
      <span className={active ? "create-radio active" : "create-radio"} />
      <span>
        <strong>{title}</strong>
        <small>{description}</small>
      </span>
    </Button>
  );
}

export function CreatePermissionTag({ text }) {
  return (
    <Badge bg="" as="span" className="create-tag">
      {text} x
    </Badge>
  );
}

export function ListPermissionStatusBadge({ status }) {
  const badgeClass =
    status === "Active" ? "list-status active" : "list-status inactive";

  return (
    <Badge bg="" as="span" className={badgeClass}>
      {status}
    </Badge>
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

export function UpdatePermissionRadioText({ text, active = false }) {
  return (
    <Button
      type="button"
      variant="link"
      className="update-radio-text"
      aria-pressed={active}
    >
      <span className={active ? "update-radio active" : "update-radio"} />
      {text}
    </Button>
  );
}

export function UpdatePermissionTag({ text }) {
  return (
    <Badge bg="" as="span" className="update-tag">
      {text} x
    </Badge>
  );
}

export function ViewPermissionInfo({ label, value, pill = false }) {
  return (
    <div>
      <p className="view-info-label">{label}</p>
      {pill ? (
        <Badge bg="" as="span" className="view-pill">
          {value}
        </Badge>
      ) : (
        <p className="view-info-value">{value}</p>
      )}
    </div>
  );
}

export function ViewPermissionRole({ text }) {
  return (
    <Badge bg="" as="span" className="view-pill">
      {text}
    </Badge>
  );
}

export function ViewPermissionStatusBadge({ text }) {
  return (
    <Badge bg="" as="span" className="view-status">
      {text}
    </Badge>
  );
}
