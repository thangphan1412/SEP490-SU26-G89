import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {
    createProject,
    listProjectEmployees,
    listProjectRoles,
} from "../../services/projectService/projectApi.js";
import CancelButton from "../../components/projectComponents/CancelButton.jsx";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import {
    addOneDay,
    calculatePhaseStartDatesForDisplay,
    createClientId,
    employeeHasRole,
    getApiErrorMessage,
    getEmployeeDescription,
    getEmployeeName,
    getEmployeeRoleNames,
    getEmployeeSearchText,
    getFilterOptions,
    CREATE_PROJECT_STATUS_OPTIONS as statusOptions,
    PHASE_STATUS_OPTIONS as phaseStatusOptions,
} from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/CreateProject.css";

const initialProject = {
    projectName: "",
    projectCode: "",
    projectStartDate: "",
    projectEndDate: "",
    projectDescription: "",
    projectStatus: "Planning",
    phases: [],
    members: [],
};

function CreateProject({ onCreateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);
    const [employees, setEmployees] = useState([]);
    const [memberRoleOptions, setMemberRoleOptions] = useState([]);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [memberSearch, setMemberSearch] = useState("");
    const [memberRoleFilter, setMemberRoleFilter] = useState("");
    const [memberStatusFilter, setMemberStatusFilter] = useState("");
    const [pendingMemberIds, setPendingMemberIds] = useState([]);
    const [loadingEmployees, setLoadingEmployees] = useState(true);
    const [employeeError, setEmployeeError] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [saving, setSaving] = useState(false);

    useEffect(function () {
        let isActive = true;
        const requestController = new AbortController();

        async function loadMemberOptions() {
            try {
                const [employeeData, roleData] = await Promise.all([
                    listProjectEmployees(requestController.signal),
                    listProjectRoles(requestController.signal),
                ]);

                if (isActive) {
                    setEmployees(Array.isArray(employeeData) ? employeeData : []);
                    setMemberRoleOptions(Array.isArray(roleData) ? roleData : []);
                    setEmployeeError("");
                }
            } catch (error) {
                if (!isActive) {
                    return;
                }

                console.error("Unable to load employees:", error);
                setEmployees([]);
                setEmployeeError("Unable to load employees. Please try again later.");
            } finally {
                if (isActive) {
                    setLoadingEmployees(false);
                }
            }
        }

        loadMemberOptions();

        return function () {
            isActive = false;
            requestController.abort();
        };
    }, []);

    function handleChange(event) {
        const { name, value } = event.target;
        setProject(function (currentProject) {
            let phases = currentProject.phases;

            if (name === "projectStartDate" && phases.length > 0) {
                phases = calculatePhaseStartDatesForDisplay(phases, value);
            }

            if (name === "projectEndDate" && phases.length > 0) {
                phases = phases.map((phase, index) =>
                    index === phases.length - 1 ? { ...phase, endDate: value } : phase
                );
            }

            return { ...currentProject, [name]: value, phases };
        });
    }

    function addPhase() {
        if (!project.projectStartDate || !project.projectEndDate) {
            setSubmitError("Select the project start date and end date before adding phases.");
            return;
        }

        if (project.projectEndDate < project.projectStartDate) {
            setSubmitError("Project end date must not be before its start date.");
            return;
        }

        const lastPhase = project.phases[project.phases.length - 1];
        const nextStartDate = lastPhase
            ? addOneDay(lastPhase.endDate)
            : project.projectStartDate;

        if (!nextStartDate || nextStartDate > project.projectEndDate) {
            setSubmitError("Shorten the current final phase before adding another phase.");
            return;
        }

        setSubmitError("");
        setProject((currentProject) => ({
            ...currentProject,
            phases: [
                ...currentProject.phases,
                {
                    clientId: createClientId(),
                    title: "",
                    description: "",
                    startDate: nextStartDate,
                    endDate: currentProject.projectEndDate,
                    status: "Planning",
                },
            ],
        }));
    }

    function updatePhase(clientId, event) {
        const { name, value } = event.target;

        setProject(function (currentProject) {
            let phases = currentProject.phases.map((phase) =>
                phase.clientId === clientId
                    ? { ...phase, [name]: value }
                    : phase
            );

            if (name === "endDate") {
                phases = calculatePhaseStartDatesForDisplay(
                    phases,
                    currentProject.projectStartDate
                );
            }

            return { ...currentProject, phases };
        });
    }

    function removePhase(clientId) {
        setProject(function (currentProject) {
            let phases = currentProject.phases.filter((phase) => phase.clientId !== clientId);

            if (phases.length > 0) {
                phases = phases.map((phase, index) =>
                    index === phases.length - 1
                        ? { ...phase, endDate: currentProject.projectEndDate }
                        : phase
                );
                phases = calculatePhaseStartDatesForDisplay(
                    phases,
                    currentProject.projectStartDate
                );
            }

            return { ...currentProject, phases };
        });
    }

    function openMemberModal() {
        setMemberSearch("");
        setMemberRoleFilter("");
        setMemberStatusFilter("");
        setPendingMemberIds([]);
        setShowMemberModal(true);
    }

    function closeMemberModal() {
        setShowMemberModal(false);
        setPendingMemberIds([]);
    }

    function togglePendingMember(userId) {
        setPendingMemberIds((currentIds) =>
            currentIds.includes(userId)
                ? currentIds.filter((id) => id !== userId)
                : [...currentIds, userId]
        );
    }

    function addSelectedMembers() {
        setProject(function (currentProject) {
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
    }

    function removeMember(userId) {
        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.filter((member) => member.userId !== userId),
        }));
    }

    async function handleSubmit(event) {
        event.preventDefault();

        try {
            setSaving(true);
            setSubmitError("");
            const createdProject = await createProject({
                projectName: project.projectName.trim(),
                projectCode: project.projectCode.trim(),
                projectStartDate: project.projectStartDate,
                projectEndDate: project.projectEndDate,
                projectDescription: project.projectDescription.trim(),
                projectStatus: project.projectStatus,
                phases: project.phases.map((phase) => ({
                    id: null,
                    title: phase.title.trim(),
                    description: phase.description.trim(),
                    endDate: phase.endDate,
                    status: phase.status,
                })),
                members: project.members,
            });

            onCreateProject?.(createdProject);
            navigate(createdProject?.id
                ? "/project-management/view?id=" + createdProject.id
                : "/project-management/list");
        } catch (error) {
            console.error("Unable to create project:", error);
            setSubmitError(getApiErrorMessage(
                error,
                "Unable to create project. Please check the information and try again."
            ));
        } finally {
            setSaving(false);
        }
    }

    const selectedMemberIds = new Set(project.members.map((member) => member.userId));
    const employeeById = new Map(employees.map((employee) => [employee.id, employee]));
    const currentProjectMembers = project.members.map((member) => ({
        ...member,
        employee: employeeById.get(member.userId) || {
            id: member.userId,
            userName: "User #" + member.userId,
        },
    }));
    const availableEmployees = employees.filter((employee) => !selectedMemberIds.has(employee.id));
    const normalizedMemberSearch = memberSearch.trim().toLowerCase();
    const memberStatusOptions = getFilterOptions(availableEmployees, "status");

    function employeeMatchesFilters(employee) {
        const matchesSearch = getEmployeeSearchText(employee).includes(normalizedMemberSearch);
        const matchesRole = !memberRoleFilter || employeeHasRole(employee, memberRoleFilter);
        const matchesStatus = !memberStatusFilter || employee.status === memberStatusFilter;

        return matchesSearch && matchesRole && matchesStatus;
    }

    const visibleAvailableEmployees = availableEmployees.filter(employeeMatchesFilters);

    function renderAvailableEmployee(employee) {
        const isSelected = pendingMemberIds.includes(employee.id);

        return (
            <label
                key={employee.id}
                htmlFor={"create-project-add-member-" + employee.id}
                className={isSelected
                    ? "create-project-modal-user selected"
                    : "create-project-modal-user"}
            >
                <Form.Check
                    type="checkbox"
                    id={"create-project-add-member-" + employee.id}
                    checked={isSelected}
                    onChange={() => togglePendingMember(employee.id)}
                    className="create-project-modal-user-check"
                />
                <span className="project-management-icon-circle create-project-modal-user-avatar">
                    <Icon name="users" size={19} />
                </span>
                <span className="create-project-modal-user-info">
                    <strong>{getEmployeeName(employee)}</strong>
                    <small>{employee.email || "No email"}</small>
                </span>
                <span className="create-project-modal-user-meta">
                    <small>{getEmployeeRoleNames(employee).join(", ") || "No assigned role"}</small>
                    <small>{employee.status || "Unknown"}</small>
                </span>
            </label>
        );
    }

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
                            <p className="create-project-section-note">Phases must cover the full project timeline without gaps or overlapping dates.</p>
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
                                            <Form.Control readOnly required type="date" name="startDate" value={phase.startDate} className="project-management-input create-project-phase-start-input" />
                                            <Form.Text className="create-project-phase-date-note">
                                                Preview only. The server calculates this date when saving.
                                            </Form.Text>
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
                            <p className="create-project-section-note">Only selected members are shown here. Use Add Members to choose users from the database.</p>
                        </div>
                        <div className="create-project-member-header-actions">
                            <span className="create-project-selected-count">{project.members.length} members</span>
                            <Button
                                type="button"
                                variant="light"
                                className="create-project-add-button"
                                disabled={loadingEmployees || Boolean(employeeError)}
                                onClick={openMemberModal}
                            >
                                <Icon name="plus" size={18} /> Add Members
                            </Button>
                        </div>
                    </div>

                    {employeeError ? (
                        <Alert variant="warning" className="create-project-employee-alert">{employeeError}</Alert>
                    ) : loadingEmployees ? (
                        <div className="create-project-empty-state">Loading employees...</div>
                    ) : currentProjectMembers.length === 0 ? (
                        <div className="create-project-empty-state">No members have been added to this project.</div>
                    ) : (
                        <div className="create-project-member-list">
                            {currentProjectMembers.map((member) => (
                                <div key={member.userId} className="create-project-member-row">
                                    <span className="project-management-icon-circle create-project-employee-avatar">
                                        <Icon name="users" size={20} />
                                    </span>
                                    <div className="create-project-employee-text">
                                        <strong>{getEmployeeName(member.employee)}</strong>
                                        <small>{getEmployeeDescription(member.employee)}</small>
                                    </div>
                                    <span className="create-project-unassigned-permission">Permission: Not assigned</span>
                                    <Button
                                        type="button"
                                        variant="link"
                                        className="create-project-remove-member-button"
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

            </Form>

            <Modal
                show={showMemberModal}
                onHide={closeMemberModal}
                centered
                size="lg"
                className="create-project-member-modal"
            >
                <Modal.Header closeButton>
                    <div>
                        <Modal.Title>Add Project Members</Modal.Title>
                        <p className="create-project-modal-description">Select users who have not been added to this project.</p>
                    </div>
                </Modal.Header>

                <Modal.Body>
                    <div className="create-project-modal-filter-grid">
                        <Form.Group className="create-project-modal-search" controlId="create-project-member-search">
                            <Form.Label className="project-management-field-label">Search</Form.Label>
                            <Form.Control
                                value={memberSearch}
                                onChange={(event) => setMemberSearch(event.target.value)}
                                placeholder="Search by name, email, role..."
                                className="create-project-modal-filter-input"
                            />
                        </Form.Group>

                        <Form.Group controlId="create-project-member-role-filter">
                            <Form.Label className="project-management-field-label">Role</Form.Label>
                            <Form.Select
                                value={memberRoleFilter}
                                onChange={(event) => setMemberRoleFilter(event.target.value)}
                                className="create-project-modal-filter-input"
                            >
                                <option value="">All roles</option>
                                {memberRoleOptions.map((role) => (
                                    <option key={role.id} value={role.id}>{role.roleName}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group controlId="create-project-member-status-filter">
                            <Form.Label className="project-management-field-label">Status</Form.Label>
                            <Form.Select
                                value={memberStatusFilter}
                                onChange={(event) => setMemberStatusFilter(event.target.value)}
                                className="create-project-modal-filter-input"
                            >
                                <option value="">All statuses</option>
                                {memberStatusOptions.map((status) => (
                                    <option key={status} value={status}>{status}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>
                    </div>

                    <div className="create-project-modal-result-header">
                        <span>{visibleAvailableEmployees.length} available users</span>
                        <span>{pendingMemberIds.length} selected</span>
                    </div>

                    {availableEmployees.length === 0 ? (
                        <div className="create-project-modal-empty">All users have already been added to this project.</div>
                    ) : visibleAvailableEmployees.length === 0 ? (
                        <div className="create-project-modal-empty">No users match the selected filters.</div>
                    ) : (
                        <div className="create-project-modal-user-list">
                            {visibleAvailableEmployees.map(renderAvailableEmployee)}
                        </div>
                    )}
                </Modal.Body>

                <Modal.Footer>
                    <Button type="button" variant="light" className="create-project-modal-cancel-button" onClick={closeMemberModal}>
                        Cancel
                    </Button>
                    <Button
                        type="button"
                        className="create-project-modal-add-button"
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

export default CreateProject;
