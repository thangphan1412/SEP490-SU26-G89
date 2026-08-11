import { Badge } from "react-bootstrap";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function StatusBadge({ status }) {
  const normalizedStatus = String(status || "Unknown").trim().toLowerCase().replaceAll("_", " ");
  const classByStatus = {
    active: "project-management-status-badge--active",
    approved: "project-management-status-badge--active",
    signed: "project-management-status-badge--active",
    planning: "project-management-status-badge--planning",
    "in progress": "project-management-status-badge--planning",
    "in review": "project-management-status-badge--hold",
    "on hold": "project-management-status-badge--hold",
    completed: "project-management-status-badge--completed",
    done: "project-management-status-badge--completed",
    draft: "project-management-status-badge--draft",
    inactive: "project-management-status-badge--draft",
    cancelled: "project-management-status-badge--danger",
    canceled: "project-management-status-badge--danger",
    overdue: "project-management-status-badge--danger",
  };

  return (
    <Badge
      bg=""
      as="span"
      className={`project-management-status-badge ${classByStatus[normalizedStatus] || "project-management-status-badge--draft"}`}
    >
      {status || "Unknown"}
    </Badge>
  );
}

export default StatusBadge;
