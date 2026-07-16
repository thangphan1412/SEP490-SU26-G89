import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Stack } from "react-bootstrap";
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

function UpdateProject({ onUpdateProject }) {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const [project, setProject] = useState(null);
    const [employees, setEmployees] = useState([]);
    const [permissionOptions, setPermissionOptions] = useState([]);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [memberSearch, setMemberSearch] = useState("");
    const [memberRoleFilter, setMemberRoleFilter] = useState("");
    const [memberStatusFilter, setMemberStatusFilter] = useState("");
    const [pendingMemberIds, setPendingMemberIds] = useState([]);
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
                        permissionId: user.permissionId ?? null,
                    })),
                });
                setEmployees(mergeEmployees(employeeData, projectUsers));

                const projectPermissions = Array.isArray(projectData?.availablePermissions)
                    ? projectData.availablePermissions.filter((permission) => permission.id)
                    : [];
                setPermissionOptions(projectPermissions);
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

    const openMemberModal = () => {
        setMemberSearch("");
        setMemberRoleFilter("");
        setMemberStatusFilter("");
        setPendingMemberIds([]);
        setShowMemberModal(true);
    };

    const closeMemberModal = () => {
        setShowMemberModal(false);
        setPendingMemberIds([]);
    };

    const togglePendingMember = (userId) => {
        setPendingMemberIds((currentIds) =>
            currentIds.includes(userId)
                ? currentIds.filter((id) => id !== userId)
                : [...currentIds, userId]
        );
    };

    const addSelectedMembers = () => {
        setProject((currentProject) => {
            const currentMemberIds = new Set(
                currentProject.members.map((member) => member.userId)
            );
            const newMembers = pendingMemberIds
                .filter((userId) => !currentMemberIds.has(userId))
                .map((userId) => ({ userId, permissionId: null }));

            return {
                ...currentProject,
                members: [...currentProject.members, ...newMembers],
            };
        });

        closeMemberModal();
    };

    const removeMember = (userId) => {
        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.filter((member) => member.userId !== userId),
        }));
    };

    const changeMemberPermission = (userId, selectedValue) => {
        const permissionId = selectedValue ? Number(selectedValue) : null;

        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.map((member) =>
                member.userId === userId ? { ...member, permissionId } : member
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

    const selectedMemberIds = new Set(project?.members.map((member) => member.userId) || []);
    const employeeById = new Map(employees.map((employee) => [employee.id, employee]));
    const currentProjectMembers = (project?.members || []).map((member) => ({
        ...member,
        employee: employeeById.get(member.userId) || {
            id: member.userId,
            userName: "User #" + member.userId,
        },
    }));
    const availableEmployees = employees.filter((employee) => !selectedMemberIds.has(employee.id));
    const normalizedMemberSearch = memberSearch.trim().toLowerCase();
    const memberRoleOptions = getFilterOptions(availableEmployees, "role");
    const memberStatusOptions = getFilterOptions(availableEmployees, "status");
    const visibleAvailableEmployees = availableEmployees.filter((employee) => {
        const matchesSearch = getEmployeeSearchText(employee).includes(normalizedMemberSearch);
        const matchesRole = !memberRoleFilter || employee.role === memberRoleFilter;
        const matchesStatus = !memberStatusFilter || employee.status === memberStatusFilter;

        return matchesSearch && matchesRole && matchesStatus;
    });

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
                                <p className="update-project-section-note">Only current project members are shown here. You can update permission or remove a member before saving.</p>
                            </div>
                            <div className="update-project-member-header-actions">
                                <span className="update-project-selected-count">{project.members.length} members</span>
                                <Button type="button" variant="light" className="update-project-add-button" onClick={openMemberModal}>
                                    <Icon name="plus" size={18} /> Add Members
                                </Button>
                            </div>
                        </div>

                        {currentProjectMembers.length === 0 ? (
                            <div className="update-project-empty-state">No members have been added to this project.</div>
                        ) : (
                            <div className="update-project-member-list">
                                {currentProjectMembers.map((member) => (
                                    <div key={member.userId} className="update-project-member-row">
                                        <span className="project-management-icon-circle update-project-employee-avatar">
                                            <Icon name="users" size={20} />
                                        </span>
                                        <div className="update-project-employee-text">
                                            <strong>{getEmployeeName(member.employee)}</strong>
                                            <small>{getEmployeeDescription(member.employee)}</small>
                                        </div>
                                        <div className="update-project-member-permission">
                                            <Form.Label className="project-management-field-label">Permission</Form.Label>
                                            <Form.Select
                                                aria-label={"Permission for " + getEmployeeName(member.employee)}
                                                disabled={permissionOptions.length === 0}
                                                value={member.permissionId ?? ""}
                                                onChange={(event) => changeMemberPermission(member.userId, event.target.value)}
                                                className="update-project-permission-select"
                                            >
                                                <option value="">
                                                    {permissionOptions.length === 0
                                                        ? "No permissions configured"
                                                        : "Not assigned"}
                                                </option>
                                                {permissionOptions.map((permission) => (
                                                    <option
                                                        key={permission.id}
                                                        value={permission.id}
                                                        disabled={permission.status === false
                                                            && member.permissionId !== permission.id}
                                                    >
                                                        {getPermissionLabel(permission)}
                                                    </option>
                                                ))}
                                            </Form.Select>
                                        </div>
                                        <Button
                                            type="button"
                                            variant="link"
                                            className="update-project-remove-member-button"
                                            onClick={() => removeMember(member.userId)}
                                            aria-label={"Remove " + getEmployeeName(member.employee)}
                                        >
                                            <Icon name="trash" size={17} color="#b42318" />
                                            Remove
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </Card>

                    <InfoAlert>Only permissions stored in the database for this project are available. Members may remain Not assigned.</InfoAlert>
                </Form>
            )}

            <Modal
                show={showMemberModal}
                onHide={closeMemberModal}
                centered
                size="lg"
                className="update-project-member-modal"
            >
                <Modal.Header closeButton>
                    <div>
                        <Modal.Title>Add Project Members</Modal.Title>
                        <p className="update-project-modal-description">Select users who are not currently part of this project.</p>
                    </div>
                </Modal.Header>

                <Modal.Body>
                    <div className="update-project-modal-filter-grid">
                        <Form.Group className="update-project-modal-search" controlId="add-member-search">
                            <Form.Label className="project-management-field-label">Search</Form.Label>
                            <Form.Control
                                value={memberSearch}
                                onChange={(event) => setMemberSearch(event.target.value)}
                                placeholder="Search by name, email, role..."
                                className="update-project-modal-filter-input"
                            />
                        </Form.Group>

                        <Form.Group controlId="add-member-role-filter">
                            <Form.Label className="project-management-field-label">Role</Form.Label>
                            <Form.Select
                                value={memberRoleFilter}
                                onChange={(event) => setMemberRoleFilter(event.target.value)}
                                className="update-project-modal-filter-input"
                            >
                                <option value="">All roles</option>
                                {memberRoleOptions.map((role) => <option key={role} value={role}>{role}</option>)}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group controlId="add-member-status-filter">
                            <Form.Label className="project-management-field-label">Status</Form.Label>
                            <Form.Select
                                value={memberStatusFilter}
                                onChange={(event) => setMemberStatusFilter(event.target.value)}
                                className="update-project-modal-filter-input"
                            >
                                <option value="">All statuses</option>
                                {memberStatusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
                            </Form.Select>
                        </Form.Group>
                    </div>

                    <div className="update-project-modal-result-header">
                        <span>{visibleAvailableEmployees.length} available users</span>
                        <span>{pendingMemberIds.length} selected</span>
                    </div>

                    {availableEmployees.length === 0 ? (
                        <div className="update-project-modal-empty">All users have already been added to this project.</div>
                    ) : visibleAvailableEmployees.length === 0 ? (
                        <div className="update-project-modal-empty">No users match the selected filters.</div>
                    ) : (
                        <div className="update-project-modal-user-list">
                            {visibleAvailableEmployees.map((employee) => {
                                const isSelected = pendingMemberIds.includes(employee.id);

                                return (
                                    <label
                                        key={employee.id}
                                        htmlFor={"add-project-member-" + employee.id}
                                        className={isSelected
                                            ? "update-project-modal-user selected"
                                            : "update-project-modal-user"}
                                    >
                                        <Form.Check
                                            type="checkbox"
                                            id={"add-project-member-" + employee.id}
                                            checked={isSelected}
                                            onChange={() => togglePendingMember(employee.id)}
                                            className="update-project-modal-user-check"
                                        />
                                        <span className="project-management-icon-circle update-project-modal-user-avatar">
                                            <Icon name="users" size={19} />
                                        </span>
                                        <span className="update-project-modal-user-info">
                                            <strong>{getEmployeeName(employee)}</strong>
                                            <small>{employee.email || "No email"}</small>
                                        </span>
                                        <span className="update-project-modal-user-meta">
                                            <small>{employee.role || "No role"}</small>
                                            <small>{employee.status || "Unknown"}</small>
                                        </span>
                                    </label>
                                );
                            })}
                        </div>
                    )}
                </Modal.Body>

                <Modal.Footer>
                    <Button type="button" variant="light" className="update-project-modal-cancel-button" onClick={closeMemberModal}>
                        Cancel
                    </Button>
                    <Button
                        type="button"
                        className="update-project-modal-add-button"
                        disabled={pendingMemberIds.length === 0}
                        onClick={addSelectedMembers}
                    >
                        <Icon name="plus" size={18} color="#fff" />
                        Add Members ({pendingMemberIds.length})
                    </Button>
                </Modal.Footer>
            </Modal>
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

function getFilterOptions(employees, fieldName) {
    return [...new Set(
        employees
            .map((employee) => employee[fieldName])
            .filter(Boolean)
    )].sort((firstValue, secondValue) => firstValue.localeCompare(secondValue));
}

function getPermissionLabel(permission) {
    const name = permission.permissionName || "Permission #" + permission.id;
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
