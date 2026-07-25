import { Alert, Stack } from "react-bootstrap";
import Icon from "./Icon.jsx";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function InfoAlert({ children }) {
  return (
    <Alert variant="primary" className="project-management-info-alert">
      <Stack direction="horizontal" gap={2}>
        <Icon name="info" size={20} />
        <span>{children}</span>
      </Stack>
    </Alert>
  );
}

export default InfoAlert;
