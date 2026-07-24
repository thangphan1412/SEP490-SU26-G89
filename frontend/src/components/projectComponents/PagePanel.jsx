import { Card, Container } from "react-bootstrap";
import useHideMainHeader from "./useHideMainHeader.js";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function PagePanel({ title, description, action, children }) {
  useHideMainHeader();

  return (
    <Container fluid as="main" className="project-management-page">
      <Card as="section" className="project-management-panel">
        <Card.Header className="project-management-page-header">
          <div>
            <h1 className="project-management-page-title">{title}</h1>
            <p className="project-management-page-description">{description}</p>
          </div>
          {action}
        </Card.Header>
        {children}
      </Card>
    </Container>
  );
}

export default PagePanel;
