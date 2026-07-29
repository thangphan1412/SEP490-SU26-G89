import { useEffect, useState } from "react";
import { Alert, Button, Card, Form, ProgressBar, Stack, Table } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { deleteProject, viewProject } from "../../services/projectService/projectApi.js";
import DangerButton from "../../components/projectComponents/DangerButton.jsx";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PermissionConfigureModal from "../../components/projectComponents/PermissionConfigureModal.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import StatusBadge from "../../components/projectComponents/StatusBadge.jsx";
import { isCompletedProjectStatus } from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/ViewProject.css";

const PROJECT_ACCESS_DENIED_MESSAGE =
    "Bạn không được quyền xem project này!";

function showValue(value) {
    return value === null || value === undefined || value === "" ? "-" : value;
}

function normalizeText(value) {
    return String(value || "").trim().toLowerCase();
}

function DetailRow({ label, value, isStatus = false }) {
    return (
        <div className="view-project-detail-row">
            <span className="view-project-detail-label">{label}</span>
            {isStatus ? (
                <StatusBadge status={value} />
            ) : (
                <span className="view-project-detail-value">{showValue(value)}</span>
            )}
        </div>
    );
}

function EmptyRow({ colSpan, message }) {
    return (
        <tr>
            <td colSpan={colSpan} className="view-project-empty-cell">
                {message}
            </td>
        </tr>
    );
}

function ViewProject() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const [project, setProject] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [actionError, setActionError] = useState("");
    const [deleting, setDeleting] = useState(false);
    const [userSearch, setUserSearch] = useState("");
    const [contractSearch, setContractSearch] = useState("");
    const [contractStatus, setContractStatus] = useState("");
    const [showPermissionConfigure, setShowPermissionConfigure] = useState(false);

    useEffect(function () {
        let isActive = true;
        const requestController = new AbortController();

        async function loadProject() {
            if (!projectId) {
                setProject(null);
                setError("Project id is missing. Please choose a project from the list.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError("");
                const payload = await viewProject(projectId, requestController.signal);

                if (isActive) {
                    setProject(payload);
                }
            } catch (apiError) {
                if (!isActive) {
                    return;
                }

                console.error("Unable to load project detail:", apiError);
                setProject(null);

                if (apiError.response?.status === 403) {
                    setError(PROJECT_ACCESS_DENIED_MESSAGE);
                } else {
                    setError("Unable to load this project. Please try again later.");
                }
            } finally {
                if (isActive) {
                    setLoading(false);
                }
            }
        }

        loadProject();

        return function () {
            isActive = false;
            requestController.abort();
        };
    }, [projectId]);

    async function handleDelete() {
        const confirmed = window.confirm(
            "Delete this project? If it has contracts, it will be kept and its status will be changed to Cancelled. If it has no contracts, it will be permanently deleted."
        );

        if (!confirmed) {
            return;
        }

        try {
            setDeleting(true);
            setActionError("");
            const message = await deleteProject(projectId);
            window.alert(message || "Project delete request completed.");
            navigate("/project-management/list");
        } catch (apiError) {
            console.error("Unable to delete project:", apiError);
            setActionError(getApiErrorMessage(apiError));
        } finally {
            setDeleting(false);
        }
    }

    function openPhase(phaseId) {
        navigate(`/phase-management/view/${projectId}/${phaseId}`);
    }

    function handlePhaseKeyDown(event, phaseId) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            openPhase(phaseId);
        }
    }

    function clearContractFilters() {
        setContractSearch("");
        setContractStatus("");
    }

    const projectPhases = Array.isArray(project?.phases) ? project.phases : [];
    const projectUsers = Array.isArray(project?.users) ? project.users : [];
    const projectContracts = Array.isArray(project?.contracts) ? project.contracts : [];
    const userSearchText = normalizeText(userSearch);
    const contractSearchText = normalizeText(contractSearch);
    const contractStatusText = normalizeText(contractStatus);

    function userMatchesSearch(user) {
        const values = [
            user.userName,
            user.email,
            user.role,
            user.userStatus,
            user.permissionName,
            user.permissionCode,
        ];

        for (const value of values) {
            if (normalizeText(value).includes(userSearchText)) {
                return true;
            }
        }

        return false;
    }

    const filteredUsers = projectUsers.filter(userMatchesSearch);

    const contractStatusOptions = [...new Set(
        projectContracts
            .map((contract) => contract.contractStatus)
            .filter((status) => normalizeText(status))
    )].sort();

    function contractMatchesFilters(contract) {
        const matchesName = [contract.contractTitle, contract.contractNumber]
            .some((value) => normalizeText(value).includes(contractSearchText));
        const matchesStatus = !contractStatusText
            || normalizeText(contract.contractStatus) === contractStatusText;

        return matchesName && matchesStatus;
    }

    const filteredContracts = projectContracts.filter(contractMatchesFilters);
    const completedProject = isCompletedProjectStatus(
        project?.projectStatus
    );

    const pageAction = (
        <Stack direction="horizontal" gap={2} className="view-project-actions">
            <Button
                type="button"
                variant="light"
                className="view-project-back-button"
                onClick={() => navigate("/project-management/list")}
            >
                Back
            </Button>

            {project && (
                <>
                    {project.currentUserIsCreator && (
                        <Button
                            type="button"
                            variant="outline-primary"
                            className="view-project-permission-configure-button"
                            onClick={() => setShowPermissionConfigure(true)}
                        >
                            <Icon name="shield" size={19} color="#2450f5" />
                            Permission Configure
                        </Button>
                    )}
                    <DangerButton
                        disabled={deleting || completedProject}
                        onClick={handleDelete}
                    >
                        <Icon name="trash" size={18} color="#b42318" />
                        {deleting ? "Deleting..." : "Delete"}
                    </DangerButton>
                    <PrimaryButton
                        disabled={completedProject}
                        onClick={() => navigate("/project-management/update?id=" + projectId)}
                    >
                        <Icon name="edit" size={20} color="#ffffff" />
                        Edit Project
                    </PrimaryButton>
                </>
            )}
        </Stack>
    );

    return (
        <PagePanel
            title="Project Details"
            description="View project information, phases, members, permissions, and contracts."
            action={pageAction}
        >
            {loading ? (
                <Card as="section" className="project-management-card">
                    <p className="view-project-state-text">Loading project...</p>
                </Card>
            ) : error ? (
                <Card as="section" className="project-management-card">
                    <p className="view-project-state-text view-project-state-text--error">
                        {error}
                    </p>
                </Card>
            ) : (
                <>
                    {actionError && (
                        <Alert variant="danger" className="view-project-action-alert">
                            {actionError}
                        </Alert>
                    )}

                    {completedProject && (
                        <Alert variant="warning" className="view-project-action-alert">
                            Completed projects cannot be updated or deleted.
                        </Alert>
                    )}

                    <Card as="section" className="project-management-card">
                        <Card.Title as="h2" className="project-management-card-title">
                            Basic Information
                        </Card.Title>

                        <div className="view-project-info-grid">
                            <DetailRow label="Name" value={project.projectName} />
                            <DetailRow label="Project Code" value={project.projectCode} />
                            <DetailRow label="Status" value={project.projectStatus} isStatus />
                            <DetailRow label="Start Date" value={project.projectStartDate} />
                            <DetailRow label="End Date" value={project.projectEndDate} />
                            <DetailRow label="Created By" value={project.projectCreatedBy} />
                            <DetailRow label="Created At" value={project.projectCreatedAt} />
                            <div className="view-project-description-row">
                                <span className="view-project-detail-label">Description</span>
                                <p className="view-project-description-text">
                                    {showValue(project.projectDescription)}
                                </p>
                            </div>
                        </div>
                    </Card>

                    <Card as="section" className="project-management-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-management-card-title">
                                Project Phases
                            </Card.Title>
                            <span className="view-project-result-count">{projectPhases.length} phases</span>
                        </div>

                        <div className="view-project-table-wrap">
                            <Table hover responsive={false} className="view-project-table view-project-phase-table mb-0">
                                <thead>
                                    <tr>
                                        <th className="view-project-th">Phase</th>
                                        <th className="view-project-th">Schedule</th>
                                        <th className="view-project-th">Status</th>
                                        <th className="view-project-th">Progress</th>
                                        <th className="view-project-th">Description</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectPhases.length === 0 ? (
                                        <EmptyRow colSpan={5} message="No phases belong to this project." />
                                    ) : (
                                        projectPhases.map((phase) => (
                                            <tr
                                                key={phase.id}
                                                className="view-project-row view-project-phase-row"
                                                role="button"
                                                tabIndex={0}
                                                onClick={() => openPhase(phase.id)}
                                                onKeyDown={(event) => handlePhaseKeyDown(event, phase.id)}
                                            >
                                                <td className="view-project-td view-project-phase-title">{showValue(phase.title)}</td>
                                                <td className="view-project-td">
                                                    <span className="view-project-schedule">{showValue(phase.startDate)}</span>
                                                    <small>to {showValue(phase.endDate)}</small>
                                                </td>
                                                <td className="view-project-td"><StatusBadge status={phase.status} /></td>
                                                <td className="view-project-td">
                                                    <div className="view-project-progress-wrap">
                                                        <ProgressBar now={clampProgress(phase.progress)} />
                                                        <span>{clampProgress(phase.progress)}%</span>
                                                    </div>
                                                </td>
                                                <td className="view-project-td view-project-description-cell">{showValue(phase.description)}</td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>

                    <Card as="section" className="project-management-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-management-card-title">
                                Project Members
                            </Card.Title>
                            <span className="view-project-result-count">
                                {filteredUsers.length} / {projectUsers.length} members
                            </span>
                        </div>

                        <div className="view-project-filter-bar">
                            <Form.Group className="view-project-search-box" controlId="project-user-search">
                                <Form.Label className="view-project-filter-label">Search member</Form.Label>
                                <Form.Control
                                    className="view-project-filter-input"
                                    value={userSearch}
                                    placeholder="Search by name, email, role, or permission..."
                                    onChange={(event) => setUserSearch(event.target.value)}
                                />
                            </Form.Group>

                            {userSearch && (
                                <Button type="button" variant="light" className="view-project-clear-button" onClick={() => setUserSearch("")}>
                                    Clear
                                </Button>
                            )}
                        </div>

                        <div className="view-project-table-wrap">
                            <Table hover responsive={false} className="view-project-table view-project-member-table mb-0">
                                <thead>
                                    <tr>
                                        <th className="view-project-th">Member</th>
                                        <th className="view-project-th">Email</th>
                                        <th className="view-project-th">Role</th>
                                        <th className="view-project-th">User Status</th>
                                        <th className="view-project-th">Permission</th>
                                        <th className="view-project-th">Join Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectUsers.length === 0 ? (
                                        <EmptyRow colSpan={6} message="No members are assigned to this project." />
                                    ) : filteredUsers.length === 0 ? (
                                        <EmptyRow colSpan={6} message="No members match your search." />
                                    ) : (
                                        filteredUsers.map((user) => (
                                            <tr key={user.userId} className="view-project-row">
                                                <td className="view-project-td view-project-user-name">{showValue(user.userName)}</td>
                                                <td className="view-project-td">{showValue(user.email)}</td>
                                                <td className="view-project-td">{showValue(user.role)}</td>
                                                <td className="view-project-td"><StatusBadge status={user.userStatus} /></td>
                                                <td className="view-project-td">
                                                    <div className="view-project-permission-cell">
                                                        <strong>{showValue(user.permissionName)}</strong>
                                                        {user.permissionCode && <small>{user.permissionCode}</small>}
                                                    </div>
                                                </td>
                                                <td className="view-project-td">{showValue(user.joinDate)}</td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>

                    <Card as="section" className="project-management-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-management-card-title">
                                Project Contracts
                            </Card.Title>
                            <span className="view-project-result-count">
                                {filteredContracts.length} / {projectContracts.length} contracts
                            </span>
                        </div>

                        <div className="view-project-filter-bar">
                            <Form.Group className="view-project-search-box" controlId="project-contract-search">
                                <Form.Label className="view-project-filter-label">Search contract</Form.Label>
                                <Form.Control
                                    className="view-project-filter-input"
                                    value={contractSearch}
                                    placeholder="Search by contract title or number..."
                                    onChange={(event) => setContractSearch(event.target.value)}
                                />
                            </Form.Group>

                            <Form.Group className="view-project-status-filter" controlId="project-contract-status">
                                <Form.Label className="view-project-filter-label">Status</Form.Label>
                                <Form.Select className="view-project-filter-input" value={contractStatus} onChange={(event) => setContractStatus(event.target.value)}>
                                    <option value="">All statuses</option>
                                    {contractStatusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
                                </Form.Select>
                            </Form.Group>

                            {(contractSearch || contractStatus) && (
                                <Button
                                    type="button"
                                    variant="light"
                                    className="view-project-clear-button"
                                    onClick={clearContractFilters}
                                >
                                    Clear
                                </Button>
                            )}
                        </div>

                        <div className="view-project-table-wrap">
                            <Table hover responsive={false} className="view-project-table mb-0">
                                <thead>
                                    <tr>
                                        <th className="view-project-th">Contract Title</th>
                                        <th className="view-project-th">Contract Number</th>
                                        <th className="view-project-th">Contract Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectContracts.length === 0 ? (
                                        <EmptyRow colSpan={3} message="No contracts belong to this project." />
                                    ) : filteredContracts.length === 0 ? (
                                        <EmptyRow colSpan={3} message="No contracts match your filters." />
                                    ) : (
                                        filteredContracts.map((contract) => (
                                            <tr
                                                key={contract.id}
                                                className="view-project-row"
                                            >
                                                <td className="view-project-td">{showValue(contract.contractTitle)}</td>
                                                <td className="view-project-td">{showValue(contract.contractNumber)}</td>
                                                <td className="view-project-td"><StatusBadge status={contract.contractStatus} /></td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                </>
            )}

            {project?.currentUserIsCreator && (
                <PermissionConfigureModal
                    show={showPermissionConfigure}
                    projectId={projectId}
                    projectName={project.projectName}
                    onHide={() => setShowPermissionConfigure(false)}
                />
            )}
        </PagePanel>
    );
}

function clampProgress(progress) {
    const numberValue = Number(progress || 0);
    return Math.min(100, Math.max(0, Math.round(numberValue)));
}

function getApiErrorMessage(error) {
    return error.response?.data?.message
        || error.response?.data?.error
        || "Unable to delete this project. It may still contain linked data.";
}

export default ViewProject;
