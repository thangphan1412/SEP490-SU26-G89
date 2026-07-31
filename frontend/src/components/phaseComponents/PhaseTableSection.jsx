import { Card } from "react-bootstrap";
import "../../assets/styles/css/phaseStyles/PhaseComponents.css";

function PhaseTableSection({
  icon,
  title,
  description,
  count = 0,
  action,
  children,
}) {
  return (
    <Card as="section" className="phase-table-card">
      <div className="phase-table-heading">
        <span className="phase-table-icon">{icon}</span>
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <div className="phase-table-actions">
          <span className="phase-table-count">{count}</span>
          {action}
        </div>
      </div>
      {children}
    </Card>
  );
}

export default PhaseTableSection;
