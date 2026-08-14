import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Modal, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {
    createProject,
    listProjectEmployees,
    listProjectUserStatuses,
} from "../../services/projectService/projectApi.js";
import CancelButton from "../../components/projectComponents/CancelButton.jsx";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import {
    addOneDay,
    calculatePhaseStartDatesForDisplay,
    createClientId,
    getEmployeeDescription,
    getEmployeeName,
    getEmployeeSearchText,
} from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/CreateProject.css";

const initialProject = {
    projectName: "",
    projectCode: "",
    projectStartDate: "",
    projectEndDate: "",
    projectDescription: "",
    phases: [],
    members: [],
};

// Hiển thị biểu mẫu tạo dự án cùng phase và thành viên ban đầu.
function CreateProject() {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);
    const [employees, setEmployees] = useState([]);
    const [memberStatusOptions, setMemberStatusOptions] = useState([]);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [memberSearch, setMemberSearch] = useState("");
    const [memberStatusFilter, setMemberStatusFilter] = useState("");
    const [pendingMemberIds, setPendingMemberIds] = useState([]);
    const [loadingEmployees, setLoadingEmployees] = useState(true);
    const [employeeError, setEmployeeError] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [saving, setSaving] = useState(false);
    const todayDate = getTodayDate();
    let minimumEndDate = todayDate;

    if (project.projectStartDate > todayDate) {
        minimumEndDate = project.projectStartDate;
    }

    // Tải dữ liệu nhân viên và trạng thái phục vụ chọn thành viên.
    useEffect(function () {
        const requestController = new AbortController();

        // Gọi song song các API tùy chọn thành viên và đồng bộ vào state.
        async function loadMemberOptions() {
            try {
                const [employeeData, statusData] = await Promise.all([
                    listProjectEmployees(requestController.signal),
                    listProjectUserStatuses(requestController.signal),
                ]);

                if (requestController.signal.aborted) {
                    return;
                }

                let validEmployees = [];
                let validStatuses = [];

                if (Array.isArray(employeeData)) {
                    validEmployees = employeeData;
                }

                if (Array.isArray(statusData)) {
                    validStatuses = statusData;
                }

                setEmployees(validEmployees);
                setMemberStatusOptions(validStatuses);
                setEmployeeError("");
            } catch (error) {
                if (requestController.signal.aborted) {
                    return;
                }

                console.error("Unable to load employees:", error);
                setEmployees([]);
                setEmployeeError("Unable to load employees. Please try again later.");
            } finally {
                if (!requestController.signal.aborted) {
                    setLoadingEmployees(false);
                }
            }
        }

        loadMemberOptions();

        return function () {
            requestController.abort();
        };
    }, []);

    // Kiểm tra và đồng bộ trường thông tin dự án vừa thay đổi.
    function handleChange(event) {
        const { name, value } = event.target;

        // Ngăn ngày bắt đầu mới nằm sau ngày kết thúc hiện tại.
        if (
            name === "projectStartDate"
            && value
            && project.projectEndDate
            && value > project.projectEndDate
        ) {
            setSubmitError("Project start date must not be after its end date.");
            return;
        }

        // Yêu cầu chọn ngày bắt đầu trước khi chọn ngày kết thúc.
        if (name === "projectEndDate" && !project.projectStartDate) {
            setSubmitError("Select the project start date before selecting its end date.");
            return;
        }

        // Ngăn ngày kết thúc dự án nằm trong quá khứ.
        if (
            name === "projectEndDate"
            && value
            && value < getTodayDate()
        ) {
            setSubmitError("Project end date must not be before today.");
            return;
        }

        // Ngăn ngày kết thúc nằm trước ngày bắt đầu dự án.
        if (
            name === "projectEndDate"
            && value
            && value < project.projectStartDate
        ) {
            setSubmitError("Project end date must not be before its start date.");
            return;
        }

        if (name === "projectStartDate" || name === "projectEndDate") {
            setSubmitError("");
        }

        setProject(function (currentProject) {
            let phases = currentProject.phases;
            let projectEndDate = currentProject.projectEndDate;

            if (name === "projectStartDate" && phases.length > 0) {
                phases = calculatePhaseStartDatesForDisplay(phases, value);
            }

            if (name === "projectStartDate" && !value) {
                projectEndDate = "";
            }

            if (name === "projectEndDate" && phases.length > 0) {
                phases = phases.map(function (phase, index) {
                    const isLastPhase = index === phases.length - 1;

                    if (isLastPhase) {
                        return { ...phase, endDate: value };
                    }

                    return phase;
                });
            }

            return {
                ...currentProject,
                projectEndDate,
                [name]: value,
                phases,
            };
        });
    }

    // Thêm phase mới nối tiếp phase cuối và kết thúc tại ngày kết thúc dự án.
    function addPhase() {
        // Yêu cầu đầy đủ timeline dự án trước khi thêm phase.
        if (!project.projectStartDate || !project.projectEndDate) {
            setSubmitError("Select the project start date and end date before adding phases.");
            return;
        }

        const lastPhase = project.phases[project.phases.length - 1];
        let nextStartDate = project.projectStartDate;

        if (lastPhase) {
            nextStartDate = addOneDay(lastPhase.endDate);
        }

        // Ngăn thêm phase khi timeline hiện tại đã phủ hết dự án.
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
                },
            ],
        }));
    }

    // Cập nhật một phase và tính lại ngày bắt đầu của các phase kế tiếp.
    function updatePhase(clientId, event) {
        const { name, value } = event.target;

        setProject(function (currentProject) {
            let phases = currentProject.phases.map(function (phase) {
                if (phase.clientId === clientId) {
                    return { ...phase, [name]: value };
                }

                return phase;
            });

            if (name === "endDate") {
                phases = calculatePhaseStartDatesForDisplay(
                    phases,
                    currentProject.projectStartDate
                );
            }

            return { ...currentProject, phases };
        });
    }

    // Xóa một phase rồi nối lại timeline của các phase còn lại.
    function removePhase(clientId) {
        setProject(function (currentProject) {
            let phases = currentProject.phases.filter((phase) => phase.clientId !== clientId);

            if (phases.length > 0) {
                phases = phases.map(function (phase, index) {
                    const isLastPhase = index === phases.length - 1;

                    if (isLastPhase) {
                        return {
                            ...phase,
                            endDate: currentProject.projectEndDate,
                        };
                    }

                    return phase;
                });
                phases = calculatePhaseStartDatesForDisplay(
                    phases,
                    currentProject.projectStartDate
                );
            }

            return { ...currentProject, phases };
        });
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
        setPendingMemberIds(function (currentIds) {
            const isAlreadySelected = currentIds.includes(userId);

            if (isAlreadySelected) {
                return currentIds.filter((id) => id !== userId);
            }

            return [...currentIds, userId];
        });
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

    // Kiểm tra dữ liệu, chuẩn hóa payload rồi gửi yêu cầu tạo dự án.
    async function handleSubmit(event) {
        event.preventDefault();

        // Từ chối gửi khi ngày kết thúc dự án nằm trong quá khứ.
        if (
            project.projectEndDate
            && project.projectEndDate < getTodayDate()
        ) {
            setSubmitError("Project end date must not be before today.");
            return;
        }

        try {
            setSaving(true);
            setSubmitError("");
            const createdProject = await createProject({
                projectName: project.projectName.trim(),
                projectCode: project.projectCode.trim(),
                projectStartDate: project.projectStartDate,
                projectEndDate: project.projectEndDate,
                projectDescription: project.projectDescription.trim(),
                phases: project.phases.map((phase) => ({
                    id: null,
                    title: phase.title.trim(),
                    description: phase.description.trim(),
                    endDate: phase.endDate,
                })),
                members: project.members,
            });

            let destination = "/project-management/list";

            if (createdProject && createdProject.id) {
                destination = "/project-management/view?id=" + createdProject.id;
            }

            navigate(destination);
        } catch (error) {
            console.error("Unable to create project:", error);
            setSubmitError(
                "Unable to create project. Please check the information and try again."
            );
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
        let employeeClassName = "create-project-modal-user";

        if (isSelected) {
            employeeClassName = "create-project-modal-user selected";
        }

        return (
            <label
                key={employee.id}
                htmlFor={"create-project-add-member-" + employee.id}
                className={employeeClassName}
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
                    <small>{employee.status || "Unknown"}</small>
                </span>
            </label>
        );
    }

    // Hiển thị danh sách biểu mẫu phase hoặc trạng thái rỗng.
    function renderPhaseContent() {
        // Hiển thị hướng dẫn khi dự án chưa có phase.
        if (project.phases.length === 0) {
            return (
                <div className="create-project-empty-state">
                    No phases yet. Select "Add Phase" to create one.
                </div>
            );
        }

        return (
            <div className="create-project-phase-list">
                {project.phases.map((phase, index) => (
                    <div key={phase.clientId} className="create-project-phase-card">
                        <div className="create-project-phase-heading">
                            <strong>Phase {index + 1}</strong>
                            <Button
                                type="button"
                                variant="link"
                                onClick={() => removePhase(phase.clientId)}
                            >
                                Remove
                            </Button>
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
                            <Col xs={12}>
                                <Form.Label className="project-management-field-label">Description</Form.Label>
                                <Form.Control as="textarea" maxLength={500} name="description" value={phase.description} onChange={(event) => updatePhase(phase.clientId, event)} className="project-management-textarea create-project-phase-description" />
                            </Col>
                        </Row>
                    </div>
                ))}
            </div>
        );
    }

    // Hiển thị danh sách thành viên đã chọn hoặc trạng thái tải tương ứng.
    function renderProjectMemberContent() {
        // Ưu tiên hiển thị lỗi tải dữ liệu nhân viên.
        if (employeeError) {
            return (
                <Alert variant="warning" className="create-project-employee-alert">
                    {employeeError}
                </Alert>
            );
        }

        // Hiển thị trạng thái chờ trong khi tải nhân viên.
        if (loadingEmployees) {
            return (
                <div className="create-project-empty-state">Loading employees...</div>
            );
        }

        // Hiển thị hướng dẫn khi chưa chọn thành viên bổ sung.
        if (currentProjectMembers.length === 0) {
            return (
                <div className="create-project-empty-state">
                    No additional members have been selected. The signed-in
                    user will be added automatically after creation.
                </div>
            );
        }

        return (
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
                        <span className="create-project-unassigned-permission">
                            Permission: Not assigned
                        </span>
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
        );
    }

    // Hiển thị kết quả nhân viên khả dụng theo bộ lọc trong modal.
    function renderAvailableEmployeeContent() {
        // Báo khi mọi người dùng đã được thêm vào dự án.
        if (availableEmployees.length === 0) {
            return (
                <div className="create-project-modal-empty">
                    All users have already been added to this project.
                </div>
            );
        }

        // Báo khi không có người dùng khớp bộ lọc hiện tại.
        if (visibleAvailableEmployees.length === 0) {
            return (
                <div className="create-project-modal-empty">
                    No users match the selected filters.
                </div>
            );
        }

        return (
            <div className="create-project-modal-user-list">
                {visibleAvailableEmployees.map(renderAvailableEmployee)}
            </div>
        );
    }

    let createButtonText = "Create Project";

    if (saving) {
        createButtonText = "Creating...";
    }

    const pageAction = (
        <Stack direction="horizontal" className="project-management-actions">
            <CancelButton disabled={saving} onClick={() => navigate("/project-management/list")} />
            <PrimaryButton type="submit" form="create-project-form" disabled={saving}>
                <Icon name="plus" size={19} color="#fff" />
                <span>{createButtonText}</span>
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
                            <Form.Control required maxLength={50} name="projectCode" value={project.projectCode} onChange={handleChange} placeholder="Example: PRJ-2026-Thời trang mùa đông" className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectStartDate">
                            <Form.Label className="project-management-field-label">Start Date</Form.Label>
                            <Form.Control required type="date" name="projectStartDate" max={project.projectEndDate} value={project.projectStartDate} onChange={handleChange} className="project-management-input" />
                        </Form.Group>

                        <Form.Group as={Col} md={6} controlId="projectEndDate">
                            <Form.Label className="project-management-field-label">End Date</Form.Label>
                            <Form.Control required disabled={!project.projectStartDate} type="date" min={minimumEndDate} name="projectEndDate" value={project.projectEndDate} onChange={handleChange} className="project-management-input" />
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

                    {renderPhaseContent()}
                </Card>

                <Card as="section" className="project-management-card">
                    <div className="create-project-section-header">
                        <div>
                            <Card.Title as="h2" className="project-management-card-title">Project Members</Card.Title>
                            <p className="create-project-section-note">
                                The signed-in user will be added automatically with the
                                Project Full Access permission after the project is created.
                                Use Add Members to choose additional users.
                            </p>
                        </div>
                        <div className="create-project-member-header-actions">
                            <span className="create-project-selected-count">
                                {project.members.length} additional members
                            </span>
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

                    {renderProjectMemberContent()}
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
                                placeholder="Search by name or email..."
                                className="create-project-modal-filter-input"
                            />
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

                    {renderAvailableEmployeeContent()}
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

// Tạo chuỗi ngày hiện tại theo định dạng YYYY-MM-DD cho input date.
function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");

    return year + "-" + month + "-" + day;
}

export default CreateProject;
