import { Card, Container } from "react-bootstrap";
import { IconTimelineEvent } from "@tabler/icons-react";
import "../../assets/styles/css/phaseStyles/PhaseComponents.css";

function PhasePage({ title, description, action, children }) {
  return (
    <Container fluid as="main" className="phase-page">
      <Card as="section" className="phase-panel">
        <Card.Header className="phase-page-header">
          <div className="phase-heading">
            <span className="phase-heading-icon">
              <IconTimelineEvent size={28} stroke={1.8} />
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

export default PhasePage;
