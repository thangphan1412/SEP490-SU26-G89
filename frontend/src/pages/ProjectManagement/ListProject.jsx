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
import { useNavigate } from "react-router-dom";
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

const PROJECT_COLUMN_LABELS = [
    "Project",
    "Code",
    "Status",
    "Start Date",
    "End Date",
    "Created By",
    "Created At",
    "Actions",
];

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
    const [successMessage, setSuccessMessage] = useState("");
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
            window.alert("Bạn không được quyền xem project này!");
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

    // Tạo nút điều hướng tới màn hình tạo dự án.
    const pageAction = (
        <PrimaryButton onClick={() => navigate("/project-management/create")}>
            <Icon name="plus" size={19} color="#fff" />
            <span>Create Project</span>
        </PrimaryButton>
    );

    return (
        <PagePanel
            title="Projects"
            description="View and find projects, timelines, ownership, and current status."
            action={pageAction}
        >
            {error && <Alert variant="danger" className="mb-3">{error}</Alert>}
            {successMessage && (
                <Alert variant="success" className="mb-3">
                    {successMessage}
                </Alert>
            )}

            <div className="list-project-toolbar">
                <InputGroup className="list-project-search-box">
                    <InputGroup.Text className="list-project-search-icon">
                        <Icon name="search" size={22} color="#3f4d6f" />
                    </InputGroup.Text>

                    <Form.Control
                        aria-label="Search projects"
                        placeholder="Search by code, name, description, or creator..."
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
                            x
                        </Button>
                    )}
                </InputGroup>

                <Form.Group className="list-project-select-box" controlId="project-status-filter">
                    <Form.Label className="list-project-select-label">Status</Form.Label>

                    <Form.Select
                        className="list-project-select"
                        value={status}
                        onChange={handleStatusChange}
                    >
                        <option value="">All statuses</option>

                        {PROJECT_STATUS_OPTIONS.map((projectStatus) => (
                            <option key={projectStatus} value={projectStatus}>
                                {projectStatus}
                            </option>
                        ))}
                    </Form.Select>

                    <span className="list-project-select-icon">
                        <Icon name="chevron" size={18} color="#243452" />
                    </span>
                </Form.Group>

                <Form.Group
                    className="list-project-view-filter"
                    controlId="view-only-your-projects-filter"
                >
                    <Form.Check
                        type="switch"
                        label="View Only Your Projects"
                        checked={viewOnlyYourProjects}
                        onChange={handleViewOnlyYourProjectsChange}
                    />
                </Form.Group>

                {(searchInput || status || viewOnlyYourProjects) && (
                    <Button
                        type="button"
                        variant="light"
                        className="list-project-filter-button"
                        onClick={clearFilters}
                    >
                        Clear filters
                    </Button>
                )}
            </div>

            <div className="list-project-table-wrap">
                <Table hover responsive={false} className="list-project-table mb-0">
                    <thead>
                        <tr>
                            {PROJECT_COLUMN_LABELS.map((label) => (
                                <th key={label} className="list-project-th">
                                    {label}
                                </th>
                            ))}
                        </tr>
                    </thead>

                    <tbody>
                        {loading ? (
                            <tr>
                                <td
                                    colSpan={PROJECT_COLUMN_LABELS.length}
                                    className="list-project-state-cell"
                                >
                                    <Spinner animation="border" size="sm" />
                                    <strong className="list-project-state-title">
                                        Loading projects...
                                    </strong>
                                </td>
                            </tr>
                        ) : projects.length === 0 ? (
                            <tr>
                                <td
                                    colSpan={PROJECT_COLUMN_LABELS.length}
                                    className="list-project-state-cell"
                                >
                                    <span className="list-project-empty-icon">
                                        <Icon name="document" size={28} color="#5b6b8a" />
                                    </span>

                                    <strong className="list-project-state-title">
                                        No projects found
                                    </strong>

                                    <span>
                                        Try changing the search term or status filter.
                                    </span>
                                </td>
                            </tr>
                        ) : (
                            projects.map((project) => (
                                <tr
                                    key={project.id}
                                    className="list-project-row"
                                    onClick={() => openProjectDetail(project)}
                                >
                                    <td className="list-project-project-cell">
                                        <span className="project-management-icon-circle list-project-avatar">
                                            <Icon name="document" size={20} />
                                        </span>

                                        <span className="list-project-project-text">
                                            <strong className="list-project-name">
                                                {project.projectName || "Untitled project"}
                                            </strong>

                                            <span
                                                title={project.projectDescription || ""}
                                                className="list-project-description"
                                            >
                                                {project.projectDescription || "No description"}
                                            </span>
                                        </span>
                                    </td>

                                    <td className="list-project-td">
                                        <span className="list-project-code-badge">
                                            {project.projectCode || "-"}
                                        </span>
                                    </td>

                                    <td className="list-project-td">
                                        <StatusBadge status={project.projectStatus} />
                                    </td>

                                    <td className="list-project-td">
                                        {project.projectStartDate}
                                    </td>

                                    <td className="list-project-td">
                                        {project.projectEndDate}
                                    </td>

                                    <td className="list-project-td">
                                        {project.projectCreatedBy || "-"}
                                    </td>

                                    <td className="list-project-td">
                                        {project.projectCreatedAt}
                                    </td>

                                    <td className="list-project-td list-project-action-cell">
                                        {project.canApprove ? (
                                            <Button
                                                type="button"
                                                variant="success"
                                                className="list-project-approve-button"
                                                disabled={approvingProjectId === project.id}
                                                onClick={(event) =>
                                                    handleApproveProject(event, project)
                                                }
                                            >
                                                {approvingProjectId === project.id
                                                    ? "Approving..."
                                                    : "Approve Project"}
                                            </Button>
                                        ) : (
                                            <span className="list-project-no-action">-</span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </Table>
            </div>

            <div className="list-project-footer">
                <span>Total: {totalElements} results</span>

                <Pagination className="list-project-pagination mb-0">
                    <Pagination.Prev
                        aria-label="Previous page"
                        className="list-project-page-item"
                        onClick={() =>
                            setPage((currentPage) => Math.max(0, currentPage - 1))
                        }
                        disabled={loading || page === 0}
                    >
                        <Icon name="arrowLeft" size={18} color="#243452" />
                    </Pagination.Prev>

                    {pageNumbers.map((pageNumber) =>
                        typeof pageNumber === "number" ? (
                            <Pagination.Item
                                key={pageNumber}
                                className="list-project-page-item"
                                active={pageNumber === page}
                                onClick={() => setPage(pageNumber)}
                                disabled={loading}
                            >
                                {pageNumber + 1}
                            </Pagination.Item>
                        ) : (
                            <Pagination.Ellipsis
                                key={pageNumber}
                                disabled
                                className="list-project-page-item list-project-ellipsis"
                            />
                        )
                    )}

                    <Pagination.Next
                        aria-label="Next page"
                        className="list-project-page-item"
                        onClick={() =>
                            setPage((currentPage) =>
                                Math.min(totalPages - 1, currentPage + 1)
                            )
                        }
                        disabled={loading || totalPages === 0 || page >= totalPages - 1}
                    >
                        <Icon name="arrowRight" size={18} color="#243452" />
                    </Pagination.Next>
                </Pagination>
            </div>
        </PagePanel>
    );
}

export default ListProject;
