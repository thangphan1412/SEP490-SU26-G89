import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Stack } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    listProjectEmployees,
    updateProject,
    viewProject,
} from "../../config/projectApi/projectApi.js";
import {
    CancelButton,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
} from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/UpdateProject.css";

const projectStatusOptions = ["Planning", "Active", "On Hold", "Completed", "Cancelled"];
const phaseStatusOptions = ["Planning", "In Progress", "On Hold", "Completed"];
const fallbackPermissionOptions = [
    { value: "VIEWER", permissionName: "Viewer", permissionCode: "PROJECT_VIEWER", status: true },
    { value: "MEMBER", permissionName: "Member", permissionCode: "PROJECT_MEMBER", status: true },
    { value: "MANAGER", permissionName: "Manager", permissionCode: "PROJECT_MANAGER", status: true },
];

function UpdateProject({ onUpdateProject }) {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const [project, setProject] = useState(null);
    const [employees, setEmployees] = useState([]);
    const [permissionOptions, setPermissionOptions] = useState(fallbackPermissionOptions);
    const [employeeSearch, setEmployeeSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        let isActive = true;

        const loadPageData = async () => {
            if (!projectId) {
                setLoadError("Project id is missing. Please choose a project from the list.");
                setLoading(false);
                return;
            }

            try {
                const [projectResponse, employeeResponse] = await Promise.all([
                    viewProject(projectId),
                    listProjectEmployees(),
                ]);
                const projectData = projectResponse.data?.data ?? projectResponse.data;
                const employeeData = employeeResponse.data?.data ?? employeeResponse.data;

                if (!isActive) {
                    return;
                }

                const projectUsers = Array.isArray(projectData?.users) ? projectData.users : [];
                setProject({
                    projectName: projectData?.projectName || "",
                    projectCode: projectData?.projectCode || "",
                    projectStartDate: projectData?.projectStartDate || "",
                    projectEndDate: projectData?.projectEndDate || "",
                    projectDescription: projectData?.projectDescription || "",
                    projectStatus: projectData?.projectStatus || "Planning",
                    projectCreatedBy: projectData?.projectCreatedBy || "",
                    projectCreatedAt: projectData?.projectCreatedAt || "",
                    phases: mapPhases(projectData?.phases),
                    members: projectUsers.map((user) => ({
                        userId: user.userId,
                        permissionValue: user.permissionValue || "MEMBER",
                    })),
                });
                setEmployees(mergeEmployees(employeeData, projectUsers));

                const projectPermissions = Array.isArray(projectData?.availablePermissions)
                    ? projectData.availablePermissions.filter((permission) => permission.value)
                    : [];
                setPermissionOptions(
                    projectPermissions.length > 0 ? projectPermissions : fallbackPermissionOptions
                );
                setLoadError("");
            } catch (error) {
                console.error("Unable to load project update data:", error);

                if (isActive) {
                    setProject(null);
                    setLoadError("Unable to load this project. Please try again later.");
                }
            } finally {
                if (isActive) {
                    setLoading(false);
                }
            }
        };

        loadPageData();

        return () => {
            isActive = false;
        };
    }, [projectId]);

    const handleProjectChange = (event) => {
        const { name, value } = event.target;
        setProject((currentProject) => ({ ...currentProject, [name]: value }));
    };

    const addPhase = () => {
        setProject((currentProject) => ({
            ...currentProject,
            phases: [
                ...currentProject.phases,
                {
                    id: null,
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

    const toggleMember = (userId) => {
        setProject((currentProject) => {
            const isSelected = currentProject.members.some((member) => member.userId === userId);
            const defaultPermission = permissionOptions.some((option) => option.value === "MEMBER")
                ? "MEMBER"
                : permissionOptions[0]?.value || "MEMBER";
            const members = isSelected
                ? currentProject.members.filter((member) => member.userId !== userId)
                : [...currentProject.members, { userId, permissionValue: defaultPermission }];

            return { ...currentProject, members };
        });
    };

    const changeMemberPermission = (userId, permissionValue) => {
        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.map((member) =>
                member.userId === userId ? { ...member, permissionValue } : member
            ),
        }));
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
            const response = await updateProject(projectId, {
                projectName: project.projectName.trim(),
                projectCode: project.projectCode.trim(),
                projectStartDate: project.projectStartDate,
                projectEndDate: project.projectEndDate,
                projectDescription: project.projectDescription.trim(),
                projectStatus: project.projectStatus,
                phases: project.phases.map((phase) => ({
                    id: phase.id,
                    title: phase.title.trim(),
                    description: phase.description.trim(),
                    startDate: phase.startDate,
                    endDate: phase.endDate,
                    status: phase.status,
                    progress: Number(phase.progress),
                })),
                members: project.members,
            });
            const updatedProject = response.data?.data ?? response.data;

            onUpdateProject?.(updatedProject);
            navigate("/project-management/view?id=" + projectId);
        } catch (error) {
            console.error("Unable to update project:", error);
            setSubmitError(getErrorMessage(error));
        } finally {
            setSaving(false);
        }
    };

    const normalizedSearch = employeeSearch.trim().toLowerCase();
    const visibleEmployees = employees.filter((employee) =>
        getEmployeeSearchText(employee).includes(normalizedSearch)
    );
    const selectedMemberIds = new Set(project?.members.map((member) => member.userId) || []);

    const pageAction = (
        <Stack direction="horizontal" className="project-management-actions">
            <CancelButton
                disabled={saving}
                onClick={() => navigate(projectId
                    ? "/project-management/view?id=" + projectId
                    : "/project-management/list")}
            />
            <PrimaryButton
                type="submit"
                form="update-project-form"
                disabled={saving || loading || !project}
            >
                <Icon name="save" size={19} color="#fff" />
                <span>{saving ? "Saving..." : "Save Changes"}</span>
            </PrimaryButton>
        </Stack>
    );

    return (
        <PagePanel
            title="Update Project"
            description="Update project information, phases, members, and each member permission."
            action={pageAction}
        >
            {loading ? (
                <Card as="section" className="project-management-card">
                    <p className="update-project-state-text">Loading project...</p>
                </Card>
            ) : loadError ? (
                <Card as="section" className="project-management-card">
                    <p className="update-project-state-text update-project-state-text--error">
                        {loadError}
                    </p>
                </Card>
            ) : (
                <Form id="update-project-form" onSubmit={handleSubmit}>
                    {submitError && (
                        <Alert variant="danger" className="update-project-alert">
                            {submitError}
                        </Alert>
                    )}

                    <Card as="section" className="project-management-card">
                        <Card.Title as="h2" className="project-management-card-title">
                            Basic Information
                        </Card.Title>

                        <Row className="update-project-form-grid">
                            <Form.Group as={Col} md={6} controlId="projectName">
                                <Form.Label className="project-management-field-label">Project Name</Form.Label>
                                <Form.Control required maxLength={50} name="projectName" value={project.projectName} onChange={handleProjectChange} className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectCode">
                                <Form.Label className="project-management-field-label">Project Code</Form.Label>
                                <Form.Control required maxLength={50} name="projectCode" value={project.projectCode} onChange={handleProjectChange} className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectStartDate">
                                <Form.Label className="project-management-field-label">Start Date</Form.Label>
                                <Form.Control required type="date" name="projectStartDate" value={project.projectStartDate} onChange={handleProjectChange} className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectEndDate">
                                <Form.Label className="project-management-field-label">End Date</Form.Label>
                                <Form.Control required type="date" min={project.projectStartDate} name="projectEndDate" value={project.projectEndDate} onChange={handleProjectChange} className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectStatus">
                                <Form.Label className="project-management-field-label">Status</Form.Label>
                                <Form.Select name="projectStatus" value={project.projectStatus} onChange={handleProjectChange} className="project-management-input">
                                    {projectStatusOptions.map((status) => <option key={status}>{status}</option>)}
                                </Form.Select>
                            </Form.Group>

                            <Form.Group as={Col} md={3} controlId="projectCreatedBy">
                                <Form.Label className="project-management-field-label">Created By</Form.Label>
                                <Form.Control disabled value={project.projectCreatedBy || "-"} className="project-management-input update-project-readonly-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={3} controlId="projectCreatedAt">
                                <Form.Label className="project-management-field-label">Created At</Form.Label>
                                <Form.Control disabled value={project.projectCreatedAt || "-"} className="project-management-input update-project-readonly-input" />
                            </Form.Group>
                        </Row>

                        <Form.Group className="update-project-full-width" controlId="projectDescription">
                            <Form.Label className="project-management-field-label">Description</Form.Label>
                            <Form.Control as="textarea" maxLength={255} name="projectDescription" value={project.projectDescription} onChange={handleProjectChange} className="project-management-textarea" />
                            <div className="update-project-counter">{project.projectDescription.length} / 255</div>
                        </Form.Group>
                    </Card>

                    <Card as="section" className="project-management-card">
                        <div className="update-project-section-header">
                            <div>
                                <Card.Title as="h2" className="project-management-card-title">Project Phases</Card.Title>
                                <p className="update-project-section-note">Phase dates must stay inside the project date range.</p>
                            </div>
                            <Button type="button" variant="light" className="update-project-add-button" onClick={addPhase}>
                                <Icon name="plus" size={18} /> Add Phase
                            </Button>
                        </div>

                        {project.phases.length === 0 ? (
                            <div className="update-project-empty-state">No phases have been added.</div>
                        ) : (
                            <div className="update-project-phase-list">
                                {project.phases.map((phase, index) => (
                                    <div key={phase.clientId} className="update-project-phase-card">
                                        <div className="update-project-phase-heading">
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
                                                <Form.Control as="textarea" maxLength={500} name="description" value={phase.description} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-textarea update-project-phase-description" />
                                            </Col>
                                        </Row>
                                    </div>
                                ))}
                            </div>
                        )}
                    </Card>

                    <Card as="section" className="project-management-card">
                        <div className="update-project-section-header">
                            <div>
                                <Card.Title as="h2" className="project-management-card-title">Project Members</Card.Title>
                                <p className="update-project-section-note">Select a person, then edit that person's permission directly below their information.</p>
                            </div>
                            <span className="update-project-selected-count">{project.members.length} selected</span>
                        </div>

                        <Form.Control className="update-project-employee-search" value={employeeSearch} onChange={(event) => setEmployeeSearch(event.target.value)} placeholder="Search employee by name, email, role..." />

                        {visibleEmployees.length === 0 ? (
                            <div className="update-project-empty-state">No employees match the search.</div>
                        ) : (
                            <div className="update-project-employee-grid">
                                {visibleEmployees.map((employee) => {
                                    const selected = selectedMemberIds.has(employee.id);
                                    const member = project.members.find((item) => item.userId === employee.id);

                                    return (
                                        <div key={employee.id} className={selected ? "update-project-employee-option selected" : "update-project-employee-option"}>
                                            <Form.Check type="checkbox" id={"update-project-employee-" + employee.id} checked={selected} onChange={() => toggleMember(employee.id)} className="update-project-employee-check" />
                                            <span className="project-management-icon-circle update-project-employee-avatar"><Icon name="users" size={20} /></span>
                                            <div className="update-project-employee-text">
                                                <strong>{getEmployeeName(employee)}</strong>
                                                <small>{getEmployeeDescription(employee)}</small>
                                                <Form.Select
                                                    aria-label={"Permission for " + getEmployeeName(employee)}
                                                    disabled={!selected}
                                                    value={member?.permissionValue || "MEMBER"}
                                                    onChange={(event) => changeMemberPermission(employee.id, event.target.value)}
                                                    className="update-project-permission-select"
                                                >
                                                    {permissionOptions.map((permission) => (
                                                        <option key={permission.value} value={permission.value}>
                                                            {getPermissionLabel(permission)}
                                                        </option>
                                                    ))}
                                                </Form.Select>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </Card>

                    <InfoAlert>Removing a phase that already has tasks or deliverables will be blocked to protect project data.</InfoAlert>
                </Form>
            )}
        </PagePanel>
    );
}

function mapPhases(phases) {
    if (!Array.isArray(phases)) {
        return [];
    }

    return phases.map((phase) => ({
        id: phase.id,
        clientId: "phase-existing-" + phase.id,
        title: phase.title || "",
        description: phase.description || "",
        startDate: phase.startDate || "",
        endDate: phase.endDate || "",
        status: phase.status || "Planning",
        progress: Number(phase.progress || 0),
    }));
}

function mergeEmployees(employeeData, projectUsers) {
    const employees = Array.isArray(employeeData) ? [...employeeData] : [];
    const employeeIds = new Set(employees.map((employee) => employee.id));

    projectUsers.forEach((user) => {
        if (!employeeIds.has(user.userId)) {
            employees.push({
                id: user.userId,
                email: user.email,
                userName: user.userName,
                role: user.role,
                status: user.userStatus,
            });
        }
    });

    return employees;
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

function createClientId() {
    return "phase-" + Date.now() + "-" + Math.random().toString(16).slice(2);
}

function getEmployeeName(employee) {
    const fullName = ((employee.firstName || "") + " " + (employee.lastName || "")).trim();
    return fullName || employee.userName || employee.email || "Employee #" + employee.id;
}

function getEmployeeDescription(employee) {
    return [employee.email, employee.role, employee.status].filter(Boolean).join(" | ") || "No employee detail";
}

function getEmployeeSearchText(employee) {
    return [
        employee.firstName,
        employee.lastName,
        employee.userName,
        employee.email,
        employee.role,
        employee.status,
    ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
}

function getPermissionLabel(permission) {
    const name = permission.permissionName || permission.value;
    const code = permission.permissionCode ? " (" + permission.permissionCode + ")" : "";
    const inactive = permission.status === false ? " - Inactive" : "";
    return name + code + inactive;
}

function getErrorMessage(error) {
    return error.response?.data?.message
        || error.response?.data?.error
        || "Unable to update the project. Please check the information and try again.";
}

export default UpdateProject;
