import { useState } from "react";
import { Card, Col, Form, InputGroup, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton } from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/CreateProject.css";

const initialProject = {
    projectName: "",
    projectCode: "",
    department: "",
    owner: "",
    status: "Planning",
    priority: "High",
    startDate: "May 22, 2025",
    endDate: "Aug 22, 2025",
    budget: "",
    location: "",
    description: "",
};

const formFields = [
    ["Project Name", "projectName", "input", "Enter project name"],
    ["Project Code", "projectCode", "input", "Enter project code"],
    ["Responsible Department", "department", "select", "Select department", ["IT", "Procurement", "HR", "Legal", "Operations"]],
    ["Project Owner", "owner", "select", "Select owner", ["Alex Morgan", "Jamie Lee", "Taylor Smith"]],
    ["Status", "status", "select", "Planning", ["Planning", "Active", "On Hold"]],
    ["Priority", "priority", "select", "High", ["High", "Medium", "Low"]],
    ["Start Date", "startDate", "date", "May 22, 2025"],
    ["End Date", "endDate", "date", "Aug 22, 2025"],
    ["Budget", "budget", "currency", "Enter budget amount"],
    ["Location / Event Venue", "location", "input", "Enter location or venue"],
];

const scopeItems = [
    ["Contract Linkage", "Link related contracts and documents to this project.", "link"],
    ["Team Assignment", "Assign internal and external team members.", "users"],
    ["Timeline Setup", "Define key milestones and project schedule.", "calendar"],
    ["Approval Flow", "Configure approval workflow and authorization.", "shield"],
];

function CreateProject({ onCreateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setProject((currentProject) => ({ ...currentProject, [name]: value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onCreateProject?.(project);
    };

    const renderField = ([label, name, type, placeholder, options]) => (
        <Form.Group as={Col} md={6} key={name} controlId={name}>
            <Form.Label className="project-management-field-label">{label}</Form.Label>
            <div className="create-project-input-wrap">
                {type === "select" ? (
                    <>
                        <Form.Select name={name} value={project[name]} onChange={handleChange} className="project-management-input">
                            <option value="">{placeholder}</option>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </Form.Select>
                        <span className="create-project-right-icon"><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : type === "currency" ? (
                    <InputGroup className="create-project-currency-wrap">
                        <InputGroup.Text className="create-project-currency-symbol">$</InputGroup.Text>
                        <Form.Control name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} className="create-project-currency-input" />
                    </InputGroup>
                ) : (
                    <>
                        <Form.Control name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} className="project-management-input" />
                        {type === "date" && <span className="create-project-right-icon"><Icon name="calendar" size={18} color="#53617e" /></span>}
                    </>
                )}
            </div>
        </Form.Group>
    );

    return (
        <PagePanel
            title="Create Project"
            description="Set up a new project, event, or initiative and assign ownership."
            action={<Stack direction="horizontal" className="project-management-actions"><CancelButton onClick={() => navigate("/project-management/list")} /><PrimaryButton type="submit" form="create-project-form"><span>Create Project</span></PrimaryButton></Stack>}
        >
            <Form id="create-project-form" onSubmit={handleSubmit}>
                <Card as="section" className="project-management-card">
                    <Card.Title as="h2" className="project-management-card-title">Project Information</Card.Title>
                    <Row className="create-project-form-grid">{formFields.map(renderField)}</Row>
                    <Form.Group className="create-project-full-width" controlId="description">
                        <Form.Label className="project-management-field-label">Description</Form.Label>
                        <Form.Control as="textarea" name="description" value={project.description} onChange={handleChange} placeholder="Enter project description..." className="project-management-textarea" />
                        <div className="create-project-counter">0 / 1000</div>
                    </Form.Group>
                </Card>

                <Card as="section" className="project-management-card">
                    <Card.Title as="h2" className="project-management-card-title">Project Scope & Tracking</Card.Title>
                    <Row className="create-project-scope-grid">
                        {scopeItems.map(([title, description, icon]) => (
                            <Col xs={12} md={6} lg={3} key={title}>
                                <Stack direction="horizontal" className="create-project-scope-item">
                                    <span className="project-management-icon-circle"><Icon name={icon} size={27} /></span>
                                    <div><h3 className="create-project-scope-title">{title}</h3><p className="create-project-scope-text">{description}</p></div>
                                </Stack>
                            </Col>
                        ))}
                    </Row>
                </Card>
                <InfoAlert>Please review the project information before creating the record.</InfoAlert>
            </Form>
        </PagePanel>
    );
}

export default CreateProject;
