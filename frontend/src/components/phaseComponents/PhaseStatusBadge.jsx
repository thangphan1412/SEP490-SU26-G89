import { Badge } from "react-bootstrap";
import "../../assets/styles/css/phaseStyles/PhaseComponents.css";

function PhaseStatusBadge({ status }) {
  const normalizedStatus = String(status || "Not set").trim().toLowerCase().replaceAll("_", " ");
  const classByStatus = {
    planning: "phase-status--planning",
    "in progress": "phase-status--progress",
    active: "phase-status--progress",
    completed: "phase-status--completed",
    done: "phase-status--completed",
    approved: "phase-status--completed",
    "on hold": "phase-status--hold",
    "in review": "phase-status--review",
    draft: "phase-status--draft",
    pending: "phase-status--draft",
    cancelled: "phase-status--cancelled",
    canceled: "phase-status--cancelled",
    overdue: "phase-status--cancelled",
  };

  return (
    <Badge className={`phase-status ${classByStatus[normalizedStatus] || "phase-status--draft"}`}>
      {status || "Not set"}
    </Badge>
  );
}

export default PhaseStatusBadge;
