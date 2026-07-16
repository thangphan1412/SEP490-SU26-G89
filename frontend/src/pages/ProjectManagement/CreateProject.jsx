import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { createProject, listProjectEmployees } from "../../config/projectApi/projectApi.js";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton } from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/CreateProject.css";

const statusOptions = ["Planning", "Active", "On Hold", "Completed", "Cancelled"];
const phaseStatusOptions = ["Planning", "In Progress", "On Hold", "Completed"];

const initialProject = {
    projectName: "",
    projectCode: "",
    projectStartDate: "",
    projectEndDate: "",
    projectCreatedAt: getTodayValue(),
    projectDescription: "",
    projectStatus: "Planning",
    phases: [],
    members: [],
};

function CreateProject({ onCreateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);
    const [employees, setEmployees] = useState([]);
    const [employeeSearch, setEmployeeSearch] = useState("");
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

    const addPhase = () => {
        setProject((currentProject) => ({
            ...currentProject,
            phases: [
                ...currentProject.phases,
                {
                    clientId: createClientId(),
                    title: "",
                    description: "",
                    startDate: currentProject.projectStartDate,
                    endDate: currentProject.projectEndDate,
                    status: "Planning",
                    progress: 0,
                },
            ],
        }));
    };

    const updatePhase = (clientId, event) => {
        const { name, value } = event.target;

        setProject((currentProject) => ({
            ...currentProject,
            phases: currentProject.phases.map((phase) =>
                phase.clientId === clientId
                    ? { ...phase, [name]: name === "progress" ? Number(value) : value }
                    : phase
            ),
        }));
    };

    const removePhase = (clientId) => {
        setProject((currentProject) => ({
            ...currentProject,
            phases: currentProject.phases.filter((phase) => phase.clientId !== clientId),
        }));
    };

    const toggleMember = (employeeId) => {
        setProject((currentProject) => {
            const isSelected = currentProject.members.some((member) => member.userId === employeeId);
            const members = isSelected
                ? currentProject.members.filter((member) => member.userId !== employeeId)
                : [...currentProject.members, { userId: employeeId, permissionId: null }];

            return { ...currentProject, members };
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        const validationMessage = validateProject(project);

        if (validationMessage) {
            setSubmitError(validationMessage);
            return;
        }

        try {
            setSaving(true);
            setSubmitError("");
            const response = await createProject({
                projectName: project.projectName.trim(),
                projectCode: project.projectCode.trim(),
                projectStartDate: project.projectStartDate,
                projectEndDate: project.projectEndDate,
                projectCreatedAt: project.projectCreatedAt,
                projectDescription: project.projectDescription.trim(),
                projectStatus: project.projectStatus,
                phases: project.phases.map((phase) => ({
                    id: null,
                    title: phase.title.trim(),
                    description: phase.description.trim(),
                    startDate: phase.startDate,
                    endDate: phase.endDate,
                    status: phase.status,
                    progress: Number(phase.progress),
                })),
                members: project.members,
            });
            const createdProject = response.data?.data ?? response.data;

            onCreateProject?.(createdProject);
            navigate(createdProject?.id
                ? "/project-management/view?id=" + createdProject.id
                : "/project-management/list");
        } catch (error) {
            console.error("Unable to create project:", error);
            setSubmitError(getErrorMessage(error));
        } finally {
            setSaving(false);
        }
    };

    const normalizedSearch = employeeSearch.trim().toLowerCase();
    const visibleEmployees = employees.filter((employee) =>
        getEmployeeSearchText(employee).includes(normalizedSearch)
    );
    const selectedMemberIds = new Set(project.members.map((member) => member.userId));

    const pageAction = (
        <Stack direction="horizontal" className="project-management-actions">
            <CancelButton disabled={saving} onClick={() => navigate("/project-management/list")} />
            <PrimaryButton type="submit" form="create-project-form" disabled={saving}>
                <Icon name="plus" size={19} color="#fff" />
                <span>{saving ? "Creating..." : "Create Project"}</span>
            </PrimaryButton>
        </Stack>
    );

    return (
        <PagePanel
            title="Create Project"
            description="Create the project, its phases, and the initial member permissions."
            action={pageAction}
        >
            <Form id="create-project-form" onSubmit={handleSubmit}>
                {submitError && <Alert variant="danger" className="create-project-alert">{submitError}</Alert>}

                <Card as="section" className="project-management-card">
                    <Card.Title as="h2" className="project-management-card-title">Basic Information</Card.Title>

                    <Row className="create-project-form-grid">
                        <Form.Group as={Col} md={6} controlId="projectName">
                            <Form.Label className="project-management-field-label">Project Name</Form.Label>
                            <Form.Control required maxLength={50} name="projectName" value={project.projectName} onChange={handleChange} placeholder="Enter project name" className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectCode">
                            <Form.Label className="project-management-field-label">Project Code</Form.Label>
                            <Form.Control required maxLength={50} name="projectCode" value={project.projectCode} onChange={handleChange} placeholder="Example: PRJ-2026-001" className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectStartDate">
                            <Form.Label className="project-management-field-label">Start Date</Form.Label>
                            <Form.Control required type="date" name="projectStartDate" value={project.projectStartDate} onChange={handleChange} className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectEndDate">
                            <Form.Label className="project-management-field-label">End Date</Form.Label>
                            <Form.Control required type="date" min={project.projectStartDate} name="projectEndDate" value={project.projectEndDate} onChange={handleChange} className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectStatus">
                            <Form.Label className="project-management-field-label">Status</Form.Label>
                            <Form.Select name="projectStatus" value={project.projectStatus} onChange={handleChange} className="project-management-input">
                                {statusOptions.map((status) => <option key={status}>{status}</option>)}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectCreatedAt">
                            <Form.Label className="project-management-field-label">Created At</Form.Label>
                            <Form.Control disabled type="date" value={project.projectCreatedAt} className="project-management-input create-project-readonly-input" />
                        </Form.Group>
                    </Row>

                    <Form.Group className="create-project-full-width" controlId="projectDescription">
                        <Form.Label className="project-management-field-label">Description</Form.Label>
                        <Form.Control as="textarea" maxLength={255} name="projectDescription" value={project.projectDescription} onChange={handleChange} placeholder="Describe the purpose and expected result of this project..." className="project-management-textarea" />
                        <div className="create-project-counter">{project.projectDescription.length} / 255</div>
                    </Form.Group>
                </Card>

                <Card as="section" className="project-management-card">
                    <div className="create-project-section-header">
                        <div>
                            <Card.Title as="h2" className="project-management-card-title">Project Phases</Card.Title>
                            <p className="create-project-section-note">Break the project into clear date-based phases.</p>
                        </div>
                        <Button type="button" variant="light" className="create-project-add-button" onClick={addPhase}>
                            <Icon name="plus" size={18} /> Add Phase
                        </Button>
                    </div>

                    {project.phases.length === 0 ? (
                        <div className="create-project-empty-state">No phases yet. Select “Add Phase” to create one.</div>
                    ) : (
                        <div className="create-project-phase-list">
                            {project.phases.map((phase, index) => (
                                <div key={phase.clientId} className="create-project-phase-card">
                                    <div className="create-project-phase-heading">
                                        <strong>Phase {index + 1}</strong>
                                        <Button type="button" variant="link" onClick={() => removePhase(phase.clientId)}>Remove</Button>
                                    </div>
                                    <Row className="g-3">
                                        <Col md={6}>
                                            <Form.Label className="project-management-field-label">Title</Form.Label>
                                            <Form.Control required maxLength={150} name="title" value={phase.title} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-input" />
                                        </Col>
                                        <Col md={3}>
                                            <Form.Label className="project-management-field-label">Start Date</Form.Label>
                                            <Form.Control required type="date" name="startDate" min={project.projectStartDate} max={project.projectEndDate} value={phase.startDate} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-input" />
                                        </Col>
                                        <Col md={3}>
                                            <Form.Label className="project-management-field-label">End Date</Form.Label>
                                            <Form.Control required type="date" name="endDate" min={phase.startDate || project.projectStartDate} max={project.projectEndDate} value={phase.endDate} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-input" />
                                        </Col>
                                        <Col md={4}>
                                            <Form.Label className="project-management-field-label">Status</Form.Label>
                                            <Form.Select name="status" value={phase.status} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-input">
                                                {phaseStatusOptions.map((status) => <option key={status}>{status}</option>)}
                                            </Form.Select>
                                        </Col>
                                        <Col md={8}>
                                            <Form.Label className="project-management-field-label">Progress: {phase.progress}%</Form.Label>
                                            <Form.Range name="progress" min="0" max="100" value={phase.progress} onChange={(event) => updatePhase(phase.clientId, event)} />
                                        </Col>
                                        <Col xs={12}>
                                            <Form.Label className="project-management-field-label">Description</Form.Label>
                                            <Form.Control as="textarea" maxLength={500} name="description" value={phase.description} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-textarea create-project-phase-description" />
                                        </Col>
                                    </Row>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>

                <Card as="section" className="project-management-card">
                    <div className="create-project-section-header">
                        <div>
                            <Card.Title as="h2" className="project-management-card-title">Project Members</Card.Title>
                            <p className="create-project-section-note">Select initial members. Permissions can be assigned after the project has database permissions.</p>
                        </div>
                        <span className="create-project-selected-count">{project.members.length} selected</span>
                    </div>

                    <Form.Control className="create-project-employee-search" value={employeeSearch} onChange={(event) => setEmployeeSearch(event.target.value)} placeholder="Search employee by name, email, role..." />

                    {employeeError ? (
                        <Alert variant="warning" className="create-project-employee-alert">{employeeError}</Alert>
                    ) : loadingEmployees ? (
                        <div className="create-project-empty-state">Loading employees...</div>
                    ) : visibleEmployees.length === 0 ? (
                        <div className="create-project-empty-state">No employees match the search.</div>
                    ) : (
                        <div className="create-project-employee-grid">
                            {visibleEmployees.map((employee) => {
                                const selected = selectedMemberIds.has(employee.id);

                                return (
                                    <div key={employee.id} className={selected ? "create-project-employee-option selected" : "create-project-employee-option"}>
                                        <Form.Check type="checkbox" id={"project-employee-" + employee.id} checked={selected} onChange={() => toggleMember(employee.id)} className="create-project-employee-check" />
                                        <span className="project-management-icon-circle create-project-employee-avatar"><Icon name="users" size={20} /></span>
                                        <div className="create-project-employee-text">
                                            <strong>{getEmployeeName(employee)}</strong>
                                            <small>{getEmployeeDescription(employee)}</small>
                                            {selected && (
                                                <span className="create-project-unassigned-permission">Permission: Not assigned</span>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </Card>

                <InfoAlert>No permissions are generated automatically. Create project permissions in Permission Management, then assign them in Update Project.</InfoAlert>
            </Form>
        </PagePanel>
    );
}

function createClientId() {
    return "phase-" + Date.now() + "-" + Math.random().toString(16).slice(2);
}

function getEmployeeName(employee) {
    const fullName = ((employee.firstName || "") + " " + (employee.lastName || "")).trim();
    return fullName || employee.email || "Employee #" + employee.id;
}

function getEmployeeDescription(employee) {
    return [employee.email, employee.role, employee.status].filter(Boolean).join(" | ") || "No employee detail";
}

function getEmployeeSearchText(employee) {
    return [employee.firstName, employee.lastName, employee.email, employee.role, employee.status]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
}

function getTodayValue() {
    const now = new Date();
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
}

function validateProject(project) {
    if (project.projectEndDate < project.projectStartDate) {
        return "Project end date must not be before its start date.";
    }

    for (const phase of project.phases) {
        if (phase.endDate < phase.startDate) {
            return "Each phase end date must not be before its start date.";
        }

        if (phase.startDate < project.projectStartDate || phase.endDate > project.projectEndDate) {
            return "Every phase must stay inside the project date range.";
        }
    }

    return "";
}

function getErrorMessage(error) {
    return error.response?.data?.message
        || error.response?.data?.detail
        || error.response?.data?.error
        || "Unable to create project. Please check the information and try again.";
}

export default CreateProject;
