import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Stack } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    listProjectEmployees,
    updateProject,
    viewProject,
} from "../../services/projectService/projectApi.js";
import CancelButton from "../../components/projectComponents/CancelButton.jsx";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import StatusBadge from "../../components/projectComponents/StatusBadge.jsx";
import {
    hasAnyProjectAction,
    hasProjectAction,
    PROJECT_ACTIONS,
} from "../../components/permissionComponents/permissionAccess.js";
import {
    createClientId,
    getEmployeeDescription,
    getEmployeeName,
    getEmployeeSearchText,
    getFilterOptions,
    getPhaseDateError,
    getPhaseStartMinDate,
    getProjectErrorMessage,
    isCompletedProjectStatus,
} from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/ProjectDetail.css";
import "../../assets/styles/css/projectStyles/UpdateProject.css";

// Hiển thị biểu mẫu cập nhật dự án theo các action người dùng được cấp.
function UpdateProject() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const [project, setProject] = useState(null);
    const [access, setAccess] = useState(null);
    const [employees, setEmployees] = useState([]);
    const [permissionOptions, setPermissionOptions] = useState([]);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [memberSearch, setMemberSearch] = useState("");
    const [memberStatusFilter, setMemberStatusFilter] = useState("");
    const [pendingMemberIds, setPendingMemberIds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [saving, setSaving] = useState(false);

    // Tải dữ liệu dự án và tùy chọn thành viên mỗi khi project id thay đổi.
    useEffect(function () {
        const requestController = new AbortController();

        // Gọi API, kiểm tra quyền và chuẩn hóa dữ liệu vào state biểu mẫu.
        async function loadPageData() {
            // Dừng tải và báo lỗi khi URL không có project id.
            if (!projectId) {
                setLoadError("Project id is missing. Please choose a project from the list.");
                setLoading(false);
                return;
            }

            try {
                const projectData = await viewProject(
                    projectId,
                    requestController.signal
                );

                if (requestController.signal.aborted) {
                    return;
                }

                const projectAccess = projectData?.currentUserAccess;
                const canManageMembers = hasProjectAction(
                    projectAccess,
                    PROJECT_ACTIONS.MANAGE_MEMBERS
                );
                const canUpdateProject = hasAnyProjectAction(
                    projectAccess,
                    [
                        PROJECT_ACTIONS.EDIT_PROJECT,
                        PROJECT_ACTIONS.MANAGE_MEMBERS,
                    ]
                );

                // Từ chối màn hình khi người dùng không có action cập nhật nào.
                if (!canUpdateProject) {
                    setProject(null);
                    setLoadError(
                        "You do not have permission to update this project."
                    );
                    return;
                }

                let employeeData = [];

                // Chỉ tải danh sách nhân viên khi được quản lý thành viên.
                if (canManageMembers) {
                    employeeData = await listProjectEmployees(
                        requestController.signal
                    );
                }

                if (requestController.signal.aborted) {
                    return;
                }

                // Không cho phép mở biểu mẫu cập nhật dự án đã hoàn thành.
                if (isCompletedProjectStatus(projectData?.projectStatus)) {
                    setProject(null);
                    setLoadError("Completed projects cannot be updated.");
                    return;
                }

                const projectUsers = Array.isArray(projectData?.users) ? projectData.users : [];
                setAccess(projectAccess);
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
                if (requestController.signal.aborted) {
                    return;
                }

                console.error("Unable to load project update data:", error);
                setProject(null);
                setAccess(null);
                setLoadError("Unable to load this project. Please try again later.");
            } finally {
                if (!requestController.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadPageData();

        return function () {
            requestController.abort();
        };
    }, [projectId]);

    const canEditProject = hasProjectAction(
        access,
        PROJECT_ACTIONS.EDIT_PROJECT
    );
    const canManageMembers = hasProjectAction(
        access,
        PROJECT_ACTIONS.MANAGE_MEMBERS
    );

    // Cập nhật thông tin dự án mà không tự thay đổi ngày của các phase.
    function handleProjectChange(event) {
        const { name, value } = event.target;
        const nextProject = {
            ...project,
            [name]: value,
        };

        setSubmitError(getPhaseDateError(
            nextProject.phases,
            nextProject.projectStartDate,
            nextProject.projectEndDate
        ));
        setProject(nextProject);
    }

    // Thêm phase mới bắt đầu sau ngày kết thúc của phase trước.
    function addPhase() {
        // Yêu cầu đầy đủ ngày bắt đầu và kết thúc dự án.
        if (!project.projectStartDate || !project.projectEndDate) {
            setSubmitError("Select the project start date and end date before adding phases.");
            return;
        }

        // Ngăn thêm phase khi timeline dự án đang bị đảo ngày.
        if (project.projectEndDate < project.projectStartDate) {
            setSubmitError("Project end date must not be before its start date.");
            return;
        }

        const phaseDateError = getPhaseDateError(
            project.phases,
            project.projectStartDate,
            project.projectEndDate
        );

        if (phaseDateError) {
            setSubmitError(phaseDateError);
            return;
        }

        const nextStartDate = getPhaseStartMinDate(
            project.phases,
            project.phases.length,
            project.projectStartDate
        );

        if (nextStartDate > project.projectEndDate) {
            setSubmitError("Shorten the current final phase before adding another phase.");
            return;
        }

        setSubmitError("");
        setProject((currentProject) => ({
            ...currentProject,
            phases: [
                ...currentProject.phases,
                {
                    id: null,
                    clientId: createClientId(),
                    title: "",
                    description: "",
                    startDate: nextStartDate,
                    endDate: currentProject.projectEndDate,
                },
            ],
        }));
    }

    // Cập nhật phase và kiểm tra thứ tự ngày giữa các phase.
    function updatePhase(clientId, event) {
        const { name, value } = event.target;
        const phases = project.phases.map((phase) =>
            phase.clientId === clientId
                ? { ...phase, [name]: value }
                : phase
        );

        setSubmitError(getPhaseDateError(
            phases,
            project.projectStartDate,
            project.projectEndDate
        ));
        setProject({ ...project, phases });
    }

    // Xóa phase mà không thay đổi khoảng ngày của các phase còn lại.
    function removePhase(clientId) {
        const phases = project.phases.filter(
            (phase) => phase.clientId !== clientId
        );

        setSubmitError(getPhaseDateError(
            phases,
            project.projectStartDate,
            project.projectEndDate
        ));
        setProject({ ...project, phases });
    }

    // Đặt lại bộ lọc và mở modal chọn thành viên.
    function openMemberModal() {
        setMemberSearch("");
        setMemberStatusFilter("");
        setPendingMemberIds([]);
        setShowMemberModal(true);
    }

    // Đóng modal và xóa danh sách thành viên đang chọn tạm.
    function closeMemberModal() {
        setShowMemberModal(false);
        setPendingMemberIds([]);
    }

    // Thêm hoặc loại một người dùng khỏi danh sách chọn tạm.
    function togglePendingMember(userId) {
        setPendingMemberIds((currentIds) =>
            currentIds.includes(userId)
                ? currentIds.filter((id) => id !== userId)
                : [...currentIds, userId]
        );
    }

    // Thêm các người dùng đã chọn vào danh sách thành viên dự án.
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

    // Loại một người dùng khỏi danh sách thành viên dự án.
    function removeMember(userId) {
        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.filter((member) => member.userId !== userId),
        }));
    }

    // Cập nhật quyền được chọn cho một thành viên dự án.
    function changeMemberPermission(userId, selectedValue) {
        const permissionId = selectedValue || null;

        setProject((currentProject) => ({
            ...currentProject,
            members: currentProject.members.map((member) =>
                member.userId === userId ? { ...member, permissionId } : member
            ),
        }));
    }

    // Kiểm tra timeline rồi gửi các phần dự án mà người dùng được phép cập nhật.
    async function handleSubmit(event) {
        event.preventDefault();

        if (canEditProject) {
            const phaseDateError = getPhaseDateError(
                project.phases,
                project.projectStartDate,
                project.projectEndDate
            );

            // Dừng gửi khi ít nhất một phase có khoảng ngày không hợp lệ.
            if (phaseDateError) {
                setSubmitError(phaseDateError);
                return;
            }
        }

        try {
            setSaving(true);
            setSubmitError("");
            await updateProject(projectId, {
                projectName: canEditProject
                    ? project.projectName.trim()
                    : null,
                projectCode: canEditProject
                    ? project.projectCode.trim()
                    : null,
                projectStartDate: canEditProject
                    ? project.projectStartDate
                    : null,
                projectEndDate: canEditProject
                    ? project.projectEndDate
                    : null,
                projectDescription: canEditProject
                    ? project.projectDescription.trim()
                    : null,
                phases: canEditProject
                    ? project.phases.map((phase) => ({
                        id: phase.id,
                        title: phase.title.trim(),
                        description: phase.description.trim(),
                        startDate: phase.startDate,
                        endDate: phase.endDate,
                    }))
                    : null,
                members: canManageMembers ? project.members : null,
            });

            navigate("/project-management/view?id=" + projectId);
        } catch (error) {
            console.error("Unable to update project:", error);
            setSubmitError(
                getProjectErrorMessage(
                    error,
                    "Unable to update the project. Please check the information and try again."
                )
            );
        } finally {
            setSaving(false);
        }
    }

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
    const memberStatusOptions = getFilterOptions(availableEmployees, "status");

    // Kiểm tra nhân viên có khớp từ khóa và trạng thái đang lọc hay không.
    function employeeMatchesFilters(employee) {
        const matchesSearch = getEmployeeSearchText(employee).includes(normalizedMemberSearch);
        const matchesStatus = !memberStatusFilter || employee.status === memberStatusFilter;

        return matchesSearch && matchesStatus;
    }

    const visibleAvailableEmployees = availableEmployees.filter(employeeMatchesFilters);

    // Hiển thị một lựa chọn nhân viên trong modal thêm thành viên.
    function renderAvailableEmployee(employee) {
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
                    <small>{employee.status || "Unknown"}</small>
                </span>
            </label>
        );
    }

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
        <div className="project-detail-page update-project-page">
        <PagePanel
            title="Update Project"
            description="Keep your project details, timeline, and team up to date."
            action={pageAction}
        >
            {loading ? (
                <Card as="section" className="project-management-card project-detail-state" role="status">
                    <span className="project-detail-section-icon"><Icon name="edit" size={23} /></span>
                    <h2>Loading project</h2>
                    <p>Preparing your project for editing...</p>
                </Card>
            ) : loadError ? (
                <Card as="section" className="project-management-card project-detail-state" role="alert">
                    <span className="project-detail-section-icon"><Icon name="info" size={23} color="#b42318" /></span>
                    <h2>Unable to edit project</h2>
                    <p>{loadError}</p>
                </Card>
            ) : (
                <Form id="update-project-form" className="update-project-form" onSubmit={handleSubmit}>
                    <div className="update-project-context">
                        <div className="update-project-context-identity">
                            <span className="project-detail-section-icon"><Icon name="building" size={23} color="#3659d9" /></span>
                            <div>
                                <span className="project-detail-eyebrow">Editing project</span>
                                <h2>{project.projectName || "Untitled project"}</h2>
                                <span className="project-detail-code">{project.projectCode || "No project code"}</span>
                            </div>
                        </div>
                        <StatusBadge status={project.projectStatus} />
                    </div>

                    {submitError && (
                        <Alert variant="danger" className="update-project-alert">
                            {submitError}
                        </Alert>
                    )}

                    {canEditProject && (
                    <Card as="section" className="project-management-card">
                        <div className="update-project-section-header">
                            <div className="project-detail-section-heading">
                                <span className="project-detail-section-icon"><Icon name="document" size={21} color="#3659d9" /></span>
                                <div>
                                    <Card.Title as="h2" className="project-management-card-title">Basic Information</Card.Title>
                                    <p className="project-detail-section-note">Define the project and its overall schedule.</p>
                                </div>
                            </div>
                            <span className="update-project-required-note"><span aria-hidden="true">*</span> Required fields</span>
                        </div>

                        <Row className="update-project-form-grid">
                            <Form.Group as={Col} md={6} controlId="projectName">
                                <Form.Label className="project-management-field-label">Project Name <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                <Form.Control required maxLength={50} name="projectName" value={project.projectName} onChange={handleProjectChange} className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectCode">
                                <Form.Label className="project-management-field-label">Project Code <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                <Form.Control required maxLength={50} name="projectCode" value={project.projectCode} onChange={handleProjectChange} placeholder="Example: PRJ-2026-Thời trang mùa đông" className="project-management-input" />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectStartDate">
                                <Form.Label className="project-management-field-label">Start Date <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                <Form.Control
                                    type="date"
                                    required
                                    name="projectStartDate"
                                    max={project.projectEndDate}
                                    value={project.projectStartDate}
                                    onChange={handleProjectChange}
                                    className="project-management-input"
                                />
                            </Form.Group>

                            <Form.Group as={Col} md={6} controlId="projectEndDate">
                                <Form.Label className="project-management-field-label">End Date <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                <Form.Control
                                    type="date"
                                    required
                                    min={project.projectStartDate}
                                    name="projectEndDate"
                                    value={project.projectEndDate}
                                    onChange={handleProjectChange}
                                    className="project-management-input"
                                />
                            </Form.Group>

                        </Row>

                        <Form.Group className="update-project-full-width" controlId="projectDescription">
                            <Form.Label className="project-management-field-label">Description <span className="update-project-optional">Optional</span></Form.Label>
                            <Form.Control as="textarea" maxLength={255} name="projectDescription" value={project.projectDescription} onChange={handleProjectChange} placeholder="Add a short description of this project..." className="project-management-textarea" />
                            <div className="update-project-counter">{project.projectDescription.length} / 255</div>
                        </Form.Group>
                        <div className="update-project-record-meta">
                            <span><Icon name="users" size={15} color="#667085" /> Created by <strong>{project.projectCreatedBy || "-"}</strong></span>
                            <span><Icon name="calendar" size={15} color="#667085" /> Created on <strong>{project.projectCreatedAt ? project.projectCreatedAt.slice(0, 10).split("-").reverse().join("/") : "-"}</strong></span>
                        </div>
                    </Card>
                    )}

                    {canEditProject && (
                    <Card as="section" className="project-management-card">
                        <div className="update-project-section-header">
                            <div className="project-detail-section-heading">
                                <span className="project-detail-section-icon"><Icon name="chart" size={21} color="#3659d9" /></span>
                                <div>
                                    <Card.Title as="h2" className="project-management-card-title">Project Phases</Card.Title>
                                    <p className="project-detail-section-note">Phases are optional. Each phase must start after the previous phase ends.</p>
                                </div>
                            </div>
                            <Button type="button" variant="light" className="update-project-add-button" onClick={addPhase}>
                                <Icon name="plus" size={18} /> Add Phase
                            </Button>
                        </div>

                        {project.phases.length === 0 ? (
                            <div className="update-project-empty-state">
                                <span className="project-detail-section-icon"><Icon name="chart" size={22} color="#3659d9" /></span>
                                <div><strong>No phases added</strong><p>You can save the project now and add phases later.</p></div>
                            </div>
                        ) : (
                            <div className="update-project-phase-list">
                                {project.phases.map((phase, index) => (
                                    <div key={phase.clientId} className="update-project-phase-card">
                                        <div className="update-project-phase-heading">
                                            <strong>Phase {index + 1}</strong>
                                            <Button type="button" variant="link" aria-label={`Remove phase ${index + 1}`} onClick={() => removePhase(phase.clientId)}><Icon name="trash" size={15} color="currentColor" /> Remove</Button>
                                        </div>
                                        <Row className="g-3">
                                            <Form.Group as={Col} md={6} controlId={`update-phase-title-${phase.clientId}`}>
                                                <Form.Label className="project-management-field-label">Title <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                                <Form.Control required maxLength={150} name="title" value={phase.title} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-input" />
                                            </Form.Group>
                                            <Form.Group as={Col} md={3} controlId={`update-phase-start-${phase.clientId}`}>
                                                <Form.Label className="project-management-field-label">Start Date <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                                <Form.Control
                                                    type="date"
                                                    required
                                                    name="startDate"
                                                    min={getPhaseStartMinDate(project.phases, index, project.projectStartDate)}
                                                    max={phase.endDate || project.projectEndDate}
                                                    value={phase.startDate}
                                                    onChange={(event) => updatePhase(phase.clientId, event)}
                                                    className="project-management-input"
                                                    aria-label={`Phase ${index + 1} start date`}
                                                />
                                            </Form.Group>
                                            <Form.Group as={Col} md={3} controlId={`update-phase-end-${phase.clientId}`}>
                                                <Form.Label className="project-management-field-label">End Date <span className="update-project-required-mark" aria-hidden="true">*</span></Form.Label>
                                                <Form.Control
                                                    type="date"
                                                    required
                                                    name="endDate"
                                                    min={phase.startDate || project.projectStartDate}
                                                    max={project.projectEndDate}
                                                    value={phase.endDate}
                                                    onChange={(event) => updatePhase(phase.clientId, event)}
                                                    className="project-management-input"
                                                    aria-label={`Phase ${index + 1} end date`}
                                                />
                                            </Form.Group>
                                            <Form.Group as={Col} xs={12} controlId={`update-phase-description-${phase.clientId}`}>
                                                <Form.Label className="project-management-field-label">Description</Form.Label>
                                                <Form.Control as="textarea" maxLength={500} name="description" value={phase.description} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-textarea update-project-phase-description" />
                                            </Form.Group>
                                        </Row>
                                    </div>
                                ))}
                            </div>
                        )}
                    </Card>
                    )}

                    {canManageMembers && (
                    <Card as="section" className="project-management-card">
                        <div className="update-project-section-header">
                            <div className="project-detail-section-heading">
                                <span className="project-detail-section-icon"><Icon name="users" size={21} color="#3659d9" /></span>
                                <div>
                                    <Card.Title as="h2" className="project-management-card-title">Project Members</Card.Title>
                                    <p className="project-detail-section-note">Manage the team and assign each member's project permission.</p>
                                </div>
                            </div>
                            <div className="update-project-member-header-actions">
                                <span className="update-project-selected-count">{project.members.length} members</span>
                                <Button type="button" variant="light" className="update-project-add-button" onClick={openMemberModal}>
                                    <Icon name="plus" size={18} /> Add Members
                                </Button>
                            </div>
                        </div>

                        {currentProjectMembers.length === 0 ? (
                            <div className="update-project-empty-state">
                                <span className="project-detail-section-icon"><Icon name="users" size={22} color="#3659d9" /></span>
                                <div><strong>Build your project team</strong><p>Add members and choose the permissions they need.</p></div>
                            </div>
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
                                            <Form.Label htmlFor={`update-member-permission-${member.userId}`} className="project-management-field-label">Permission</Form.Label>
                                            <Form.Select
                                                id={`update-member-permission-${member.userId}`}
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
                    )}

                    <div className="update-project-form-footer">
                        <div className="update-project-save-note">
                            <Icon name="info" size={19} color="#667085" />
                            <span>Changes are applied when you save.</span>
                        </div>
                        {pageAction}
                    </div>
                </Form>
            )}

            {canManageMembers && (
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
                                placeholder="Search by name or email..."
                                className="update-project-modal-filter-input"
                            />
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
                            {visibleAvailableEmployees.map(renderAvailableEmployee)}
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
            )}
        </PagePanel>
        </div>
    );
}

// Chuyển phase từ API thành cấu trúc state có client id ổn định.
function mapPhases(phases) {
    // Trả danh sách rỗng khi dữ liệu phase không đúng định dạng.
    if (!Array.isArray(phases)) {
        return [];
    }

    const mappedPhases = [];

    for (const phase of phases) {
        mappedPhases.push({
            id: phase.id,
            clientId: "phase-existing-" + phase.id,
            title: phase.title || "",
            description: phase.description || "",
            startDate: phase.startDate || "",
            endDate: phase.endDate || "",
        });
    }

    return mappedPhases;
}

// Hợp nhất danh sách nhân viên khả dụng với thành viên đã có trong dự án.
function mergeEmployees(employeeData, projectUsers) {
    const employees = Array.isArray(employeeData) ? [...employeeData] : [];
    const employeeIds = new Set(employees.map((employee) => employee.id));

    for (const user of projectUsers) {
        if (!employeeIds.has(user.userId)) {
            employees.push({
                id: user.userId,
                email: user.email,
                userName: user.userName,
                status: user.userStatus,
            });
        }
    }

    return employees;
}

// Định dạng tên, mã và trạng thái quyền thành nhãn lựa chọn.
function getPermissionLabel(permission) {
    const name = permission.permissionName || "Permission #" + permission.id;
    const code = permission.permissionCode ? " (" + permission.permissionCode + ")" : "";
    const inactive = permission.status === false ? " - Inactive" : "";
    return name + code + inactive;
}

export default UpdateProject;
