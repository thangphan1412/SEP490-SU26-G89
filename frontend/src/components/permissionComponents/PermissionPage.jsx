import { Card, Container } from "react-bootstrap";
import { IconShieldCheck } from "@tabler/icons-react";
import useHideMainHeader from "./useHideMainHeader.js";
import "../../assets/styles/css/permissionStyles/PermissionComponents.css";

function PermissionPage({ title, description, action, children }) {
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

export default PermissionPage;
