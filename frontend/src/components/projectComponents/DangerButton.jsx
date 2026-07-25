import { Button } from "react-bootstrap";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function DangerButton({ children, onClick, ...buttonProps }) {
  return (
    <Button type="button" variant="light" className="project-management-danger-button" onClick={onClick} {...buttonProps}>
      {children}
    </Button>
  );
}

export default DangerButton;
