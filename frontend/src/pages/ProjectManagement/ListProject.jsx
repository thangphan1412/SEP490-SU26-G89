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
import { listProjects } from "../../services/projectService/projectApi.js";
import Icon from "../../components/projectComponents/Icon.jsx";
import PagePanel from "../../components/projectComponents/PagePanel.jsx";
import PrimaryButton from "../../components/projectComponents/PrimaryButton.jsx";
import StatusBadge from "../../components/projectComponents/StatusBadge.jsx";
import {
    getApiErrorMessage,
    PROJECT_STATUS_OPTIONS,
} from "../../components/projectComponents/projectFormUtils.js";
import "../../assets/styles/css/projectStyles/ListProject.css";

const sortableColumns = [
    ["Project", "projectName"],
    ["Code", "projectCode"],
    ["Status", "projectStatus"],
    ["Start Date", "projectStartDate"],
    ["End Date", "projectEndDate"],
    ["Created By", "projectCreatedBy"],
    ["Created At", "projectCreatedAt"],
];

function createPageNumbers(currentPage, totalPages) {
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

function ListProject() {
    const navigate = useNavigate();
    const [projects, setProjects] = useState([]);
    const [searchInput, setSearchInput] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [sortBy, setSortBy] = useState("id");
    const [sortDirection, setSortDirection] = useState("desc");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [projectStatusOptions, setProjectStatusOptions] = useState(
        PROJECT_STATUS_OPTIONS
    );

    useEffect(function () {
        const debounceId = window.setTimeout(function () {
            setSearch(searchInput.trim());
            setPage(0);
        }, 500);

        return function () {
            window.clearTimeout(debounceId);
        };
    }, [searchInput]);

    useEffect(function () {
        let isActive = true;
        const requestController = new AbortController();

        async function loadProjects() {
            const requestParams = {
                search: search,
                status: status,
                page: page,
                sortBy: sortBy,
                sortDirection: sortDirection,
            };

            try {
                setLoading(true);
                setError("");
                const payload = await listProjects(
                    requestParams,
                    requestController.signal
                );

                const projectList = Array.isArray(payload?.items)
                    ? payload.items
                    : [];

                const totalProjectCount = Number(payload?.totalElements) || 0;
                const totalPageCount = Number(payload?.totalPages) || 0;
                const responseStatuses = Array.isArray(payload?.availableStatuses)
                    ? payload.availableStatuses
                    : [];
                const statusOptions = [...PROJECT_STATUS_OPTIONS];

                for (const responseStatus of responseStatuses) {
                    if (responseStatus && !statusOptions.includes(responseStatus)) {
                        statusOptions.push(responseStatus);
                    }
                }

                if (!isActive) {
                    return;
                }

                setProjects(projectList);
                setTotalElements(totalProjectCount);
                setTotalPages(totalPageCount);
                setProjectStatusOptions(statusOptions);

                if (totalPageCount > 0 && page >= totalPageCount) {
                    setPage(totalPageCount - 1);
                }
            } catch (error) {
                if (!isActive) {
                    return;
                }

                console.error("Unable to load projects:", error);

                setProjects([]);
                setTotalElements(0);
                setTotalPages(0);
                setProjectStatusOptions(PROJECT_STATUS_OPTIONS);
                setError(getApiErrorMessage(
                    error,
                    "Unable to load projects. Please try again later."
                ));
            } finally {
                if (isActive) {
                    setLoading(false);
                }
            }
        }

        loadProjects();

        return function () {
            isActive = false;
            requestController.abort();
        };
    }, [page, search, status, sortBy, sortDirection]);

    const pageNumbers = createPageNumbers(page, totalPages);

    function handleSort(field) {
        setPage(0);

        if (sortBy === field) {
            setSortDirection(function (currentDirection) {
                if (currentDirection === "asc") {
                    return "desc";
                }

                return "asc";
            });
            return;
        }

        setSortBy(field);
        setSortDirection("asc");
    }

    function handleStatusChange(event) {
        setStatus(event.target.value);
        setPage(0);
    }

    function clearFilters() {
        setSearchInput("");
        setSearch("");
        setStatus("");
        setPage(0);
    }

    function openProjectDetail(projectId) {
        if (!projectId) {
            return;
        }

        navigate(`/project-management/view?id=${projectId}`);
    }

    function handleProjectRowKeyDown(event, projectId) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            openProjectDetail(projectId);
        }
    }

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

                        {projectStatusOptions.map((projectStatus) => (
                            <option key={projectStatus} value={projectStatus}>
                                {projectStatus}
                            </option>
                        ))}
                    </Form.Select>

                    <span className="list-project-select-icon">
                        <Icon name="chevron" size={18} color="#243452" />
                    </span>
                </Form.Group>

                {(searchInput || status) && (
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
                            {sortableColumns.map(([label, field]) => (
                                <th key={field} className="list-project-th">
                                    <Button
                                        type="button"
                                        variant="link"
                                        className={`list-project-sort-button ${sortBy === field
                                            ? "list-project-sort-button--active"
                                            : ""
                                        }`}
                                        onClick={() => handleSort(field)}
                                        aria-label={`Sort by ${label}`}
                                    >
                                        {label}

                                        <Icon
                                            name="sort"
                                            size={13}
                                            color={sortBy === field ? "#1f4fff" : "#62708c"}
                                        />
                                    </Button>
                                </th>
                            ))}
                        </tr>
                    </thead>

                    <tbody>
                        {loading ? (
                            <tr>
                                <td
                                    colSpan={sortableColumns.length}
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
                                    colSpan={sortableColumns.length}
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
                                    tabIndex={0}
                                    role="button"
                                    onClick={() => openProjectDetail(project.id)}
                                    onKeyDown={(event) =>
                                        handleProjectRowKeyDown(event, project.id)
                                    }
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
