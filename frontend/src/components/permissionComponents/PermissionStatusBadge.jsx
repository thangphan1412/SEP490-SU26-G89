import { Badge } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/PermissionComponents.css";

function PermissionStatusBadge({ status }) {
  if (status === null || status === undefined) {
    return <Badge className="permission-status permission-status--unset">Not set</Badge>;
  }

  const statusClass = status ? "permission-status--active" : "permission-status--inactive";
  return <Badge className={`permission-status ${statusClass}`}>{status ? "Active" : "Inactive"}</Badge>;
}

export default PermissionStatusBadge;
