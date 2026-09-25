import { useEffect, useState } from "react";
import {
    Alert,
    Button,
    Form,
    InputGroup,
    Pagination,
    Spinner,
    Table,
} from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import {
    approveProject,
    listProjects,
} from "../../services/projectService/projectApi.js";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import StatusBadge from "../../components/projectComponents/StatusBadge.jsx";
import { PROJECT_STATUS_OPTIONS }
    from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/ListProject.css";

const PROJECT_COLUMN_LABELS = ["Project", "Status", "Timeline", "Created by", "Actions"];

// Tạo danh sách số trang rút gọn và chèn dấu ba chấm khi cần.
function createPageNumbers(currentPage, totalPages) {
    // Hiển thị toàn bộ số trang khi tổng số trang không quá năm.
    if (totalPages <= 5) {
        return Array.from({ length: totalPages }, (_, index) => index);
    }

    const candidates = new Set([
        0,
        totalPages - 1,
        currentPage - 1,
        currentPage,
        currentPage + 1,
    ]);

    const visiblePages = [...candidates]
        .filter((pageNumber) => pageNumber >= 0 && pageNumber < totalPages)
        .sort((first, second) => first - second);

    const pages = [];

    for (let index = 0; index < visiblePages.length; index += 1) {
        const pageNumber = visiblePages[index];

        if (index > 0 && pageNumber - visiblePages[index - 1] > 1) {
            pages.push(`ellipsis-${pageNumber}`);
        }

        pages.push(pageNumber);
    }

    return pages;
}

// Hiển thị danh sách dự án cùng bộ lọc, phân trang và thao tác phê duyệt.
function ListProject() {
    const navigate = useNavigate();
    const location = useLocation();
    const [projects, setProjects] = useState([]);
    const [searchInput, setSearchInput] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [viewOnlyYourProjects, setViewOnlyYourProjects] = useState(false);
    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState(location.state?.successMessage || "");
    const [approvingProjectId, setApprovingProjectId] = useState(null);
    const [reloadVersion, setReloadVersion] = useState(0);

    // Debounce từ khóa tìm kiếm trước khi gọi lại API.
    useEffect(function () {
        const debounceId = window.setTimeout(function () {
            setSearch(searchInput.trim());
            setPage(0);
        }, 500);

        return function () {
            window.clearTimeout(debounceId);
        };
    }, [searchInput]);

    // Tải danh sách dự án theo bộ lọc và trang hiện tại.
    useEffect(function () {
        const requestController = new AbortController();

        // Gọi API và đồng bộ dự án cùng thông tin phân trang vào state.
        async function loadProjects() {
            const requestParams = {
                search: search,
                status: status,
                viewOnlyYourProjects: viewOnlyYourProjects,
                page: page,
            };

            try {
                setLoading(true);
                setError("");
                const payload = await listProjects(
                    requestParams,
                    requestController.signal
                );

                const projectList = payload.items;
                const totalProjectCount = payload.totalElements;
                const totalPageCount = payload.totalPages;

                if (requestController.signal.aborted) {
                    return;
                }

                setProjects(projectList);
                setTotalElements(totalProjectCount);
                setTotalPages(totalPageCount);

                // Đưa trang hiện tại về giới hạn hợp lệ sau khi dữ liệu thay đổi.
                if (totalPageCount > 0 && page >= totalPageCount) {
                    setPage(totalPageCount - 1);
                }
            } catch (error) {
                if (requestController.signal.aborted) {
                    return;
                }
                console.error("Unable to load projects:", error);
                setProjects([]);
                setTotalElements(0);
                setTotalPages(0);
                setError("Unable to load projects. Please try again later.");
            } finally {
                if (!requestController.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadProjects();

        return function () {
            requestController.abort();
        };
    }, [
        page,
        search,
        status,
        viewOnlyYourProjects,
        reloadVersion,
    ]);

    const pageNumbers = createPageNumbers(page, totalPages);

    // Cập nhật bộ lọc trạng thái và quay về trang đầu.
    function handleStatusChange(event) {
        setStatus(event.target.value);
        setPage(0);
    }

    // Bật hoặc tắt phạm vi chỉ hiển thị dự án của người dùng.
    function handleViewOnlyYourProjectsChange(event) {
        setViewOnlyYourProjects(event.target.checked);
        setPage(0);
    }

    // Xóa toàn bộ bộ lọc và đưa danh sách về trang đầu.
    function clearFilters() {
        setSearchInput("");
        setSearch("");
        setStatus("");
        setViewOnlyYourProjects(false);
        setPage(0);
    }

    // Kiểm tra quyền truy cập rồi điều hướng tới chi tiết dự án.
    function openProjectDetail(project) {
        // Bỏ qua bản ghi không có mã dự án.
        if (!project?.id) {
            return;
        }

        // Thông báo khi backend đánh dấu người dùng không được xem dự án.
        if (project.canView === false) {
            const isPendingApproval = String(project.projectStatus || "").toLowerCase() === "on hold";
            window.alert(isPendingApproval
                ? "This project is pending approval. Only the CEO and the Head of the Administrative Department can view it."
                : "You do not have permission to view this project.");
            return;
        }

        navigate(`/project-management/view?id=${project.id}`);
    }

    // Xác nhận rồi gửi yêu cầu phê duyệt dự án được chọn.
    async function handleApproveProject(event, project) {
        event.stopPropagation();

        const confirmed = window.confirm(
            "Approve project " + project.projectName + "?"
        );

        // Hủy thao tác khi người dùng không xác nhận phê duyệt.
        if (!confirmed) {
            return;
        }

        try {
            setApprovingProjectId(project.id);
            setError("");
            setSuccessMessage("");
            const message = await approveProject(project.id);
            setSuccessMessage(message);
            setReloadVersion(function (currentVersion) {
                return currentVersion + 1;
            });
        } catch {
            setError("Unable to approve this project. Please try again later.");
        } finally {
            setApprovingProjectId(null);
        }
    }

    const hasFilters = Boolean(searchInput || status || viewOnlyYourProjects);

    const pageAction = (
        <PrimaryButton onClick={() => navigate("/project-management/create")}>
            <Icon name="plus" size={18} color="currentColor" />
            <span>Create Project</span>
        </PrimaryButton>
    );

    return (
        <div className="list-project-page">
            <PagePanel
                title="Projects"
                description="Track project timelines, ownership and approvals."
                action={pageAction}
            >
                {error && <Alert variant="danger" className="list-project-alert">{error}</Alert>}
                {successMessage && (
                    <Alert variant="success" className="list-project-alert" dismissible onClose={() => setSuccessMessage("")}>
                        {successMessage}
                    </Alert>
                )}

                <section className="list-project-content" aria-label="Project directory" aria-busy={loading}>
                    <div className="list-project-toolbar">
                        <InputGroup className="list-project-search-box">
                            <InputGroup.Text className="list-project-search-icon">
                                <Icon name="search" size={19} color="currentColor" />
                            </InputGroup.Text>
                            <Form.Control
                                type="search"
                                aria-label="Search projects"
                                placeholder="Search by name, code or creator..."
                                className="list-project-search-input"
                                value={searchInput}
                                onChange={(event) => setSearchInput(event.target.value)}
                            />
                            {searchInput && (
                                <Button
                                    type="button"
                                    variant="light"
                                    aria-label="Clear search"
                                    className="list-project-clear-search"
                                    onClick={() => setSearchInput("")}
                                >
                                    <span aria-hidden="true">×</span>
                                </Button>
                            )}
                        </InputGroup>

                        <Form.Group className="list-project-select-box" controlId="project-status-filter">
                            <Form.Label className="visually-hidden">Status</Form.Label>
                            <span className="list-project-filter-icon">
                                <Icon name="filter" size={17} color="currentColor" />
                            </span>
                            <Form.Select
                                className="list-project-select"
                                value={status}
                                onChange={handleStatusChange}
                            >
                                <option value="">All statuses</option>
                                {PROJECT_STATUS_OPTIONS.map((projectStatus) => (
                                    <option key={projectStatus} value={projectStatus}>{projectStatus}</option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="list-project-view-filter" controlId="view-only-your-projects-filter">
                            <Form.Check
                                type="switch"
                                label="Only my projects"
                                checked={viewOnlyYourProjects}
                                onChange={handleViewOnlyYourProjectsChange}
                            />
                        </Form.Group>
                    </div>

                    <div className="list-project-results-bar">
                        <span aria-live="polite" role="status">
                            {loading ? "Loading projects..." : (
                                <>
                                    <strong>{totalElements}</strong> {totalElements === 1 ? "project" : "projects"}
                                    {hasFilters ? " matching your filters" : " in this list"}
                                </>
                            )}
                        </span>
                        {hasFilters ? (
                            <Button type="button" variant="link" className="list-project-reset" onClick={clearFilters}>
                                Clear filters <span aria-hidden="true">×</span>
                            </Button>
                        ) : (
                            <span className="list-project-sort-note">
                                <Icon name="sort" size={15} color="currentColor" /> Newest first
                            </span>
                        )}
                    </div>

                    <div className="list-project-table-wrap">
                        <Table hover responsive={false} className="list-project-table mb-0">
                            <caption className="visually-hidden">Projects with status, timeline, creator and available actions.</caption>
                            <thead>
                                <tr>
                                    {PROJECT_COLUMN_LABELS.map((label) => (
                                        <th key={label} scope="col" className="list-project-th">{label}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td colSpan={PROJECT_COLUMN_LABELS.length} className="list-project-state-cell">
                                            <Spinner animation="border" size="sm" aria-hidden="true" />
                                            <strong className="list-project-state-title">Loading your projects</strong>
                                            <span>Getting the latest project information.</span>
                                        </td>
                                    </tr>
                                ) : projects.length === 0 ? (
                                    <tr>
                                        <td colSpan={PROJECT_COLUMN_LABELS.length} className="list-project-state-cell">
                                            <span className="list-project-empty-icon">
                                                <Icon name={error ? "info" : "document"} size={27} color="currentColor" />
                                            </span>
                                            <strong className="list-project-state-title">
                                                {error ? "Projects couldn't be loaded" : hasFilters ? "No matching projects" : "Your projects start here"}
                                            </strong>
                                            <span>
                                                {error ? "Please try again to load the latest information."
                                                    : hasFilters ? "Try another keyword or clear your filters."
                                                        : "Create a project to start organising your team's work."}
                                            </span>
                                            <Button
                                                type="button"
                                                variant="light"
                                                className="list-project-empty-action"
                                                onClick={error ? () => setReloadVersion((version) => version + 1)
                                                    : hasFilters ? clearFilters
                                                        : () => navigate("/project-management/create")}
                                            >
                                                {error ? "Try again" : hasFilters ? "Clear filters" : "Create Project"}
                                            </Button>
                                        </td>
                                    </tr>
                                ) : projects.map((project) => (
                                    <tr key={project.id} className="list-project-row" onClick={() => openProjectDetail(project)}>
                                        <td className="list-project-project-cell">
                                            <div className="list-project-project-info">
                                                <span className="list-project-avatar">
                                                    <Icon name="document" size={21} color="currentColor" />
                                                </span>
                                                <div className="list-project-project-text">
                                                    <button
                                                        type="button"
                                                        className="list-project-name"
                                                        title={project.projectName || "Untitled project"}
                                                        onClick={(event) => {
                                                            event.stopPropagation();
                                                            openProjectDetail(project);
                                                        }}
                                                    >
                                                        {project.projectName || "Untitled project"}
                                                    </button>
                                                    <span className="list-project-code" title={project.projectCode || ""}>
                                                        {project.projectCode || "No project code"}
                                                    </span>
                                                    <span className="list-project-description" title={project.projectDescription || ""}>
                                                        {project.projectDescription || "No description"}
                                                    </span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="list-project-status-cell" data-label="Status">
                                            <StatusBadge status={project.projectStatus} />
                                        </td>
                                        <td className="list-project-timeline-cell" data-label="Timeline">
                                            <div className="list-project-date"><span>Start</span><span>{project.projectStartDate || "—"}</span></div>
                                            <div className="list-project-date"><span>End</span><span>{project.projectEndDate || "—"}</span></div>
                                        </td>
                                        <td className="list-project-creator-cell" data-label="Created by">
                                            <span className="list-project-creator-name" title={project.projectCreatedBy || ""}>
                                                {project.projectCreatedBy || "Unknown"}
                                            </span>
                                            <span className="list-project-created-date">Created {project.projectCreatedAt || "—"}</span>
                                        </td>
                                        <td className="list-project-action-cell">
                                            <div className="list-project-row-actions">
                                                {(project.canApprove || project.waitingForDepartmentApproval) && (
                                                    <Button
                                                        type="button"
                                                        variant="light"
                                                        className="list-project-approve-button"
                                                        disabled={!project.canApprove || approvingProjectId === project.id}
                                                        title={project.waitingForDepartmentApproval
                                                            ? "Waiting for HeadOfDepartment of Administrative to approve first"
                                                            : "Approve project"}
                                                        aria-label={(project.waitingForDepartmentApproval ? "Waiting Approve: " : "Approve project ") + project.projectName}
                                                        onClick={(event) => handleApproveProject(event, project)}
                                                    >
                                                        {project.waitingForDepartmentApproval
                                                            ? "Waiting Approve"
                                                            : approvingProjectId === project.id
                                                                ? <><Spinner animation="border" size="sm" aria-hidden="true" /> Approving...</>
                                                                : <><Icon name="shield" size={15} color="currentColor" /> Approve</>}
                                                    </Button>
                                                )}
                                                <Button
                                                    type="button"
                                                    variant="light"
                                                    className="list-project-open-button"
                                                    aria-label={"View project " + (project.projectName || "Untitled project")}
                                                    title="View project"
                                                    onClick={(event) => {
                                                        event.stopPropagation();
                                                        openProjectDetail(project);
                                                    }}
                                                >
                                                    <Icon name="arrowRight" size={17} color="currentColor" />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </div>

                    <div className="list-project-footer">
                        <span>{loading ? "Updating list..." : <>Showing <strong>{projects.length}</strong> of <strong>{totalElements}</strong> projects</>}</span>
                        <nav aria-label="Project pages">
                            <Pagination className="list-project-pagination mb-0">
                                <Pagination.Prev
                                    aria-label="Previous page"
                                    onClick={() => setPage((currentPage) => Math.max(0, currentPage - 1))}
                                    disabled={loading || page === 0}
                                >
                                    <Icon name="arrowLeft" size={16} color="currentColor" />
                                </Pagination.Prev>
                                {pageNumbers.map((pageNumber) =>
                                    typeof pageNumber === "number" ? (
                                        <Pagination.Item
                                            key={pageNumber}
                                            active={pageNumber === page}
                                            aria-label={"Page " + (pageNumber + 1)}
                                            onClick={() => setPage(pageNumber)}
                                            disabled={loading}
                                        >
                                            {pageNumber + 1}
                                        </Pagination.Item>
                                    ) : (
                                        <Pagination.Ellipsis key={pageNumber} disabled />
                                    )
                                )}
                                <Pagination.Next
                                    aria-label="Next page"
                                    onClick={() => setPage((currentPage) => Math.min(totalPages - 1, currentPage + 1))}
                                    disabled={loading || totalPages === 0 || page >= totalPages - 1}
                                >
                                    <Icon name="arrowRight" size={16} color="currentColor" />
                                </Pagination.Next>
                            </Pagination>
                        </nav>
                    </div>
                </section>
            </PagePanel>
        </div>
    );
}

export default ListProject;
