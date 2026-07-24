import { Button } from "react-bootstrap";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function CancelButton({ children = "Cancel", onClick, ...buttonProps }) {
  return (
    <Button type="button" variant="light" className="project-management-cancel-button" onClick={onClick} {...buttonProps}>
      {children}
    </Button>
  );
}

export default CancelButton;
