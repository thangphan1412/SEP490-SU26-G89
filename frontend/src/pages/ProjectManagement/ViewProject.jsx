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
import {
    hasAnyProjectAction,
    hasProjectAction,
    PROJECT_ACTIONS,
} from "../../components/permissionComponents/permissionAccess.js";
import "../../assets/styles/css/projectStyles/ViewProject.css";

// Hiển thị ký hiệu trống cho giá trị chưa được cung cấp.
function showValue(value) {
    return value === null || value === undefined || value === "" ? "-" : value;
}

// Chuyển ngày ISO từ API sang định dạng dd/mm/yyyy để hiển thị.
function formatDate(value) {
    const normalizedValue = String(value ?? "").trim();

    if (!normalizedValue) {
        return "-";
    }

    const dateMatch = /^(\d{4})-(\d{2})-(\d{2})(?:[T\s].*)?$/.exec(normalizedValue);

    if (!dateMatch) {
        return normalizedValue;
    }

    return `${dateMatch[3]}/${dateMatch[2]}/${dateMatch[1]}`;
}

// Chuẩn hóa văn bản để so khớp bộ lọc không phân biệt hoa thường.
function normalizeText(value) {
    return String(value || "").trim().toLowerCase();
}

// Hiển thị một hàng thông tin chi tiết, hỗ trợ định dạng trạng thái.
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

// Hiển thị một hàng thông báo khi bảng không có dữ liệu.
function EmptyRow({ colSpan, message }) {
    return (
        <tr>
            <td colSpan={colSpan} className="view-project-empty-cell">
                {message}
            </td>
        </tr>
    );
}

// Hiển thị chi tiết dự án theo quyền truy cập của người dùng hiện tại.
function ViewProject() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const openPermissionConfigure =
        searchParams.get("openPermissionConfigure") === "true";
    const [project, setProject] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [actionError, setActionError] = useState("");
    const [deleting, setDeleting] = useState(false);
    const [userSearch, setUserSearch] = useState("");
    const [contractSearch, setContractSearch] = useState("");
    const [contractStatus, setContractStatus] = useState("");
    const [showPermissionConfigure, setShowPermissionConfigure] = useState(
        openPermissionConfigure
    );

    // Tải lại chi tiết dự án mỗi khi project id thay đổi.
    useEffect(function () {
        const requestController = new AbortController();

        // Gọi API và đồng bộ chi tiết dự án vào state trang.
        async function loadProject() {
            try {
                setLoading(true);
                setError("");
                const payload = await viewProject(projectId, requestController.signal);

                if (requestController.signal.aborted) {
                    return;
                }

                setProject(payload);
            } catch (apiError) {
                if (requestController.signal.aborted) {
                    return;
                }

                console.error("Unable to load project detail:", apiError);
                setProject(null);

                // Hiển thị thông báo riêng khi backend từ chối quyền truy cập.
                if (apiError.response?.status === 403) {
                    setError("Bạn không được quyền xem project này!");
                } else {
                    setError("Unable to load this project. Please try again later.");
                }
            } finally {
                if (!requestController.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadProject();

        return function () {
            requestController.abort();
        };
    }, [projectId]);

    // Xác nhận rồi gửi yêu cầu xóa hoặc hủy dự án đang xem.
    async function handleDelete() {
        const confirmed = window.confirm(
            "Delete this project? If it has contracts, it will be kept and its status will be changed to Cancelled. If it has no contracts, it will be permanently deleted."
        );

        // Hủy thao tác khi người dùng không xác nhận xóa.
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
            setActionError(
                "Unable to delete this project. It may still contain linked data."
            );
        } finally {
            setDeleting(false);
        }
    }

    // Kiểm tra quyền xem rồi điều hướng tới chi tiết phase.
    function openPhase(phase) {
        // Từ chối mở phase bị khóa theo trạng thái và phạm vi truy cập.
        if (!canViewPhase(phase, access)) {
            setActionError(
                "Only a PLANNING or IN_PROGRESS phase can be accessed."
            );
            return;
        }

        setActionError("");
        navigate(`/phase-management/view/${projectId}/${phase.id}`);
    }

    // Hỗ trợ mở phase bằng bàn phím trên hàng bảng.
    function handlePhaseKeyDown(event, phase) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            openPhase(phase);
        }
    }

    // Xóa từ khóa và trạng thái lọc hợp đồng.
    function clearContractFilters() {
        setContractSearch("");
        setContractStatus("");
    }

    const projectPhases = project?.phases ?? [];
    const projectUsers = project?.users ?? [];
    const projectContracts = project?.contracts ?? [];
    const userSearchText = normalizeText(userSearch);
    const contractSearchText = normalizeText(contractSearch);
    const contractStatusText = normalizeText(contractStatus);

    // Kiểm tra thành viên có khớp từ khóa tìm kiếm hay không.
    function userMatchesSearch(user) {
        const values = [
            user.userName,
            user.email,
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

    // Kiểm tra hợp đồng có khớp từ khóa và trạng thái đang lọc hay không.
    function contractMatchesFilters(contract) {
        const matchesName = [contract.contractTitle, contract.contractNumber]
            .some((value) => normalizeText(value).includes(contractSearchText));
        const matchesStatus = !contractStatusText
            || normalizeText(contract.contractStatus) === contractStatusText;

        return matchesName && matchesStatus;
    }

    const filteredContracts = projectContracts.filter(contractMatchesFilters);

    let projectStatus = "";
    let access = null;

    if (project) {
        projectStatus = project.projectStatus;
        access = project.currentUserAccess;
    }

    const completedProject = isCompletedProjectStatus(projectStatus);
    const canEditProject = hasProjectAction(
        access,
        PROJECT_ACTIONS.EDIT_PROJECT
    );
    const canManageMembers = hasProjectAction(
        access,
        PROJECT_ACTIONS.MANAGE_MEMBERS
    );
    let isExecutiveViewer = false;

    if (access) {
        isExecutiveViewer = access.isExecutiveViewer === true;
    }

    const canViewMembers = canManageMembers
        || isExecutiveViewer;
    const canOpenUpdate = canEditProject
        || canManageMembers;
    const canViewContracts = hasProjectAction(
        access,
        PROJECT_ACTIONS.VIEW_CONTRACTS
    );
    const canManageContracts = canViewContracts && hasAnyProjectAction(
        access,
        [
            PROJECT_ACTIONS.CREATE_CONTRACTS,
            PROJECT_ACTIONS.EDIT_CONTRACTS,
            PROJECT_ACTIONS.DELETE_CONTRACTS,
        ]
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
                    {canManageMembers && (
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
                    {canEditProject && (
                        <DangerButton
                            disabled={deleting || completedProject}
                            onClick={handleDelete}
                        >
                            <Icon name="trash" size={18} color="#b42318" />
                            {deleting ? "Deleting..." : "Delete"}
                        </DangerButton>
                    )}
                    {canOpenUpdate && (
                        <PrimaryButton
                            disabled={completedProject}
                            onClick={() => navigate("/project-management/update?id=" + projectId)}
                        >
                            <Icon name="edit" size={20} color="#ffffff" />
                            Edit Project
                        </PrimaryButton>
                    )}
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
                            <DetailRow label="Start Date" value={formatDate(project.projectStartDate)} />
                            <DetailRow label="End Date" value={formatDate(project.projectEndDate)} />
                            <DetailRow label="Created By" value={project.projectCreatedBy} />
                            <DetailRow label="Created At" value={formatDate(project.projectCreatedAt)} />
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
                                                className={canViewPhase(phase, access)
                                                    ? "view-project-row view-project-phase-row"
                                                    : "view-project-row view-project-phase-row view-project-phase-row--locked"}
                                                role="button"
                                                tabIndex={0}
                                                aria-disabled={!canViewPhase(phase, access)}
                                                title={canViewPhase(phase, access)
                                                    ? "Open phase"
                                                    : "This phase is available only when its status is PLANNING or IN_PROGRESS"}
                                                onClick={() => openPhase(phase)}
                                                onKeyDown={(event) => handlePhaseKeyDown(event, phase)}
                                            >
                                                <td className="view-project-td view-project-phase-title">{showValue(phase.title)}</td>
                                                <td className="view-project-td">
                                                    <span className="view-project-schedule">{formatDate(phase.startDate)}</span>
                                                    <small>to {formatDate(phase.endDate)}</small>
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

                    {canViewMembers && (
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
                                    placeholder="Search by name, email, status, or permission..."
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
                                        <th className="view-project-th">User Status</th>
                                        <th className="view-project-th">Permission</th>
                                        <th className="view-project-th">Join Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectUsers.length === 0 ? (
                                        <EmptyRow colSpan={5} message="No members are assigned to this project." />
                                    ) : filteredUsers.length === 0 ? (
                                        <EmptyRow colSpan={5} message="No members match your search." />
                                    ) : (
                                        filteredUsers.map((user) => (
                                            <tr key={user.userId} className="view-project-row">
                                                <td className="view-project-td view-project-user-name">{showValue(user.userName)}</td>
                                                <td className="view-project-td">{showValue(user.email)}</td>
                                                <td className="view-project-td"><StatusBadge status={user.userStatus} /></td>
                                                <td className="view-project-td">
                                                    <div className="view-project-permission-cell">
                                                        <strong>{showValue(user.permissionName)}</strong>
                                                        {user.permissionCode && <small>{user.permissionCode}</small>}
                                                    </div>
                                                </td>
                                                <td className="view-project-td">{formatDate(user.joinDate)}</td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                    )}

                    {canViewContracts && (
                    <Card as="section" className="project-management-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-management-card-title">
                                Project Contracts
                            </Card.Title>
                            <div className="d-flex flex-wrap align-items-center gap-2">
                                <span className="view-project-result-count">
                                    {filteredContracts.length} / {projectContracts.length} contracts
                                </span>
                                {canManageContracts && (
                                    <Button
                                        type="button"
                                        variant="outline-primary"
                                        size="sm"
                                        className="d-inline-flex align-items-center gap-2"
                                        onClick={() => navigate("/contract-management/list")}
                                    >
                                        <Icon name="document" size={16} />
                                        Manage Contract
                                    </Button>
                                )}
                            </div>
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
                    )}
                </>
            )}

            {canManageMembers && (
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

// Chuẩn hóa tiến độ thành số nguyên trong khoảng từ 0 đến 100.
function clampProgress(progress) {
    const numberValue = Number(progress || 0);
    return Math.min(100, Math.max(0, Math.round(numberValue)));
}

// Kiểm tra phase có cho phép chuẩn bị hoặc thực hiện task hay không.
function phaseSupportsTaskPreparation(status) {
    const normalizedStatus = String(status || "").trim().toUpperCase();
    return normalizedStatus === "PLANNING"
        || normalizedStatus === "IN_PROGRESS";
}

// Kiểm tra phase có thể mở theo trạng thái hoặc quyền xem toàn dự án.
function canViewPhase(phase, access) {
    // Phase PLANNING và IN_PROGRESS có thể mở để quản lý task.
    if (phaseSupportsTaskPreparation(phase.status)) {
        return true;
    }

    // Từ chối phase khác trạng thái khi chưa có thông tin truy cập.
    if (!access) {
        return false;
    }

    return access.isExecutiveViewer === true;
}

export default ViewProject;
