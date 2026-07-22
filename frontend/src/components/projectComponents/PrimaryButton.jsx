import { Button } from "react-bootstrap";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

function PrimaryButton({ children, onClick, type = "button", ...buttonProps }) {
  return (
    <Button type={type} className="project-management-primary-button" onClick={onClick} {...buttonProps}>
      {children}
    </Button>
  );
}

export default PrimaryButton;
