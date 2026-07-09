import { useEffect, useState } from "react";
import { Alert, Card, Col, Form, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { createProject, listProjectEmployees } from "../../config/axiosConfig.js";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton } from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/CreateProject.css";

const today = getTodayValue();

const initialProject = {
    projectName: "",
    projectCode: "",
    projectStartDate: "",
    projectEndDate: "",
    projectCreatedAt: today,
    projectDescription: "",
    projectStatus: "Planning",
    employeeIds: [],
};

const statusOptions = [
    "Planning",
    "Active",
    "On Hold",
    "Completed",
    "Cancelled",
];

function CreateProject({ onCreateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);
    const [employees, setEmployees] = useState([]);
    const [loadingEmployees, setLoadingEmployees] = useState(true);
    const [employeeError, setEmployeeError] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        let isActive = true;

        const loadEmployees = async () => {
            try {
                const response = await listProjectEmployees();
                const payload = response.data?.data ?? response.data;

                if (isActive) {
                    setEmployees(Array.isArray(payload) ? payload : []);
                    setEmployeeError("");
                }
            } catch (error) {
                console.error("Unable to load employees:", error);

                if (isActive) {
                    setEmployees([]);
                    setEmployeeError("Unable to load employees. Please try again later.");
                }
            } finally {
                if (isActive) {
                    setLoadingEmployees(false);
                }
            }
        };

        loadEmployees();

        return () => {
            isActive = false;
        };
    }, []);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setProject((currentProject) => ({ ...currentProject, [name]: value }));
    };

    const handleEmployeeChange = (event, employeeId) => {
        const checked = event.target.checked;

        setProject((currentProject) => {
            const currentEmployeeIds = currentProject.employeeIds;
            const employeeIds = checked
                ? [...currentEmployeeIds, employeeId]
                : currentEmployeeIds.filter((id) => id !== employeeId);

            return { ...currentProject, employeeIds: employeeIds };
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setSubmitError("");

        if (project.projectEndDate && project.projectStartDate && project.projectEndDate < project.projectStartDate) {
            setSubmitError("End date must be after start date.");
            return;
        }

        try {
            setSaving(true);
            const response = await createProject({
                ...project,
                projectName: project.projectName.trim(),
                projectCode: project.projectCode.trim(),
                projectDescription: project.projectDescription.trim(),
            });
            const createdProject = response.data?.data ?? response.data;

            onCreateProject?.(createdProject);

            if (createdProject?.id) {
                navigate(`/project-management/view?id=${createdProject.id}`);
                return;
            }

            navigate("/project-management/list");
        } catch (error) {
            console.error("Unable to create project:", error);
            setSubmitError(getSubmitErrorMessage(error));
        } finally {
            setSaving(false);
        }
    };

    const selectedEmployeeIds = new Set(project.employeeIds);
    const selectedEmployeeCount = project.employeeIds.length;

    const pageAction = (
        <Stack direction="horizontal" className="project-management-actions">
            <CancelButton
                disabled={saving}
                onClick={() => navigate("/project-management/list")}
            />
            <PrimaryButton type="submit" form="create-project-form" disabled={saving}>
                <Icon name="plus" size={19} color="#fff" />
                <span>{saving ? "Creating..." : "Create Project"}</span>
            </PrimaryButton>
        </Stack>
    );

    return (
        <PagePanel
            title="Create Project"
            description="Create a project with basic information and assigned employees."
            action={pageAction}
        >
            <Form id="create-project-form" onSubmit={handleSubmit}>
                {submitError && (
                    <Alert variant="danger" className="create-project-alert">
                        {submitError}
                    </Alert>
                )}

                <Card as="section" className="project-management-card">
                    <Card.Title as="h2" className="project-management-card-title">Project Information</Card.Title>

                    <Row className="create-project-form-grid">
                        <Form.Group as={Col} md={6} controlId="projectName">
                            <Form.Label className="project-management-field-label">Project Name</Form.Label>
                            <Form.Control
                                required
                                maxLength={50}
                                name="projectName"
                                value={project.projectName}
                                onChange={handleChange}
                                placeholder="Enter project name"
                                className="project-management-input"
                            />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectCode">
                            <Form.Label className="project-management-field-label">Code</Form.Label>
                            <Form.Control
                                required
                                name="projectCode"
                                value={project.projectCode}
                                onChange={handleChange}
                                placeholder="Enter project code"
                                className="project-management-input"
                            />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectStartDate">
                            <Form.Label className="project-management-field-label">Start Date</Form.Label>
                            <div className="create-project-input-wrap">
                                <Form.Control
                                    required
                                    type="date"
                                    name="projectStartDate"
                                    value={project.projectStartDate}
                                    onChange={handleChange}
                                    className="project-management-input"
                                />
                                <span className="create-project-right-icon"><Icon name="calendar" size={18} color="#53617e" /></span>
                            </div>
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectEndDate">
                            <Form.Label className="project-management-field-label">End Date</Form.Label>
                            <div className="create-project-input-wrap">
                                <Form.Control
                                    required
                                    type="date"
                                    name="projectEndDate"
                                    value={project.projectEndDate}
                                    onChange={handleChange}
                                    className="project-management-input"
                                />
                                <span className="create-project-right-icon"><Icon name="calendar" size={18} color="#53617e" /></span>
                            </div>
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectCreatedAt">
                            <Form.Label className="project-management-field-label">Create At</Form.Label>
                            <div className="create-project-input-wrap">
                                <Form.Control
                                    required
                                    type="date"
                                    name="projectCreatedAt"
                                    value={project.projectCreatedAt}
                                    onChange={handleChange}
                                    className="project-management-input"
                                />
                                <span className="create-project-right-icon"><Icon name="calendar" size={18} color="#53617e" /></span>
                            </div>
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectStatus">
                            <Form.Label className="project-management-field-label">Status</Form.Label>
                            <div className="create-project-input-wrap">
                                <Form.Select
                                    name="projectStatus"
                                    value={project.projectStatus}
                                    onChange={handleChange}
                                    className="project-management-input"
                                >
                                    {statusOptions.map((status) => (
                                        <option key={status} value={status}>{status}</option>
                                    ))}
                                </Form.Select>
                                <span className="create-project-right-icon"><Icon name="chevron" size={18} color="#243452" /></span>
                            </div>
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectCreatedBy">
                            <Form.Label className="project-management-field-label">Created By</Form.Label>
                            <Form.Control
                                disabled
                                value="Admin"
                                className="project-management-input create-project-readonly-input"
                            />
                        </Form.Group>
                    </Row>

                    <Form.Group className="create-project-full-width" controlId="description">
                        <Form.Label className="project-management-field-label">Description</Form.Label>
                        <Form.Control
                            as="textarea"
                            maxLength={255}
                            name="projectDescription"
                            value={project.projectDescription}
                            onChange={handleChange}
                            placeholder="Enter project description..."
                            className="project-management-textarea"
                        />
                        <div className="create-project-counter">{project.projectDescription.length} / 255</div>
                    </Form.Group>
                </Card>

                <Card as="section" className="project-management-card">
                    <div className="create-project-section-header">
                        <div>
                            <Card.Title as="h2" className="project-management-card-title">Project Employees</Card.Title>
                            <p className="create-project-section-note">Choose employees to add to this project.</p>
                        </div>

                        <span className="create-project-selected-count">
                            {selectedEmployeeCount} selected
                        </span>
                    </div>

                    {employeeError ? (
                        <Alert variant="warning" className="create-project-employee-alert">
                            {employeeError}
                        </Alert>
                    ) : loadingEmployees ? (
                        <div className="create-project-employee-state">Loading employees...</div>
                    ) : employees.length === 0 ? (
                        <div className="create-project-employee-state">No employees found.</div>
                    ) : (
                        <div className="create-project-employee-grid">
                            {employees.map((employee) => (
                                <label key={employee.id} className="create-project-employee-option">
                                    <Form.Check
                                        type="checkbox"
                                        id={`project-employee-${employee.id}`}
                                        checked={selectedEmployeeIds.has(employee.id)}
                                        onChange={(event) => handleEmployeeChange(event, employee.id)}
                                        className="create-project-employee-check"
                                    />
                                    <span className="project-management-icon-circle create-project-employee-avatar">
                                        <Icon name="users" size={20} />
                                    </span>
                                    <span className="create-project-employee-text">
                                        <strong>{getEmployeeName(employee)}</strong>
                                        <small>{getEmployeeDescription(employee)}</small>
                                    </span>
                                </label>
                            ))}
                        </div>
                    )}
                </Card>

                <InfoAlert>Created by is fixed as Admin until login is completed.</InfoAlert>
            </Form>
        </PagePanel>
    );
}

function getEmployeeName(employee) {
    const fullName = `${employee.firstName || ""} ${employee.lastName || ""}`.trim();
    return fullName || employee.email || `Employee #${employee.id}`;
}

function getTodayValue() {
    const now = new Date();
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);

    return localDate.toISOString().slice(0, 10);
}

function getEmployeeDescription(employee) {
    return [employee.email, employee.role, employee.status]
        .filter(Boolean)
        .join(" | ") || "No employee detail";
}

function getSubmitErrorMessage(error) {
    return error.response?.data?.message
        || error.response?.data?.detail
        || error.response?.data?.error
        || "Unable to create project. Please check the information and try again.";
}

export default CreateProject;
