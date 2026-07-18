import { useEffect, useState } from "react";
import { Button, Form, InputGroup, Pagination, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { listProjects } from "../../config/projectApi/projectApi.js";
import { Icon, PagePanel, PrimaryButton, StatusBadge } from "./ProjectComponents.jsx";
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

    visiblePages.forEach((pageNumber, index) => {
        if (index > 0 && pageNumber - visiblePages[index - 1] > 1) {
            pages.push(`ellipsis-${pageNumber}`);
        }

        pages.push(pageNumber);
    });

    return pages;
}

function ListProject() {
    const navigate = useNavigate();
    const [projects, setProjects] = useState([]);
    const [searchInput, setSearchInput] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [availableStatuses, setAvailableStatuses] = useState([]);
    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [sortBy, setSortBy] = useState("id");
    const [sortDirection, setSortDirection] = useState("desc");

    useEffect(() => {
        const debounceId = window.setTimeout(() => {
            setSearch(searchInput.trim());
            setPage(0);
        }, 500);

        return () => window.clearTimeout(debounceId);
    }, [searchInput]);

    useEffect(() => {
        const loadProjects = async () => {
            const requestParams = {
                search: search,
                status: status,
                page: page,
                sortBy: sortBy,
                sortDirection: sortDirection,
            };

            try {
                const response = await listProjects(requestParams);

                const payload = response.data?.data ?? response.data;

                const projectList = Array.isArray(payload?.items)
                    ? payload.items
                    : [];

                const statusList = Array.isArray(payload?.availableStatuses)
                    ? payload.availableStatuses
                    : [];

                const totalProjectCount = Number(payload?.totalElements) || 0;
                const totalPageCount = Number(payload?.totalPages) || 0;

                setProjects(projectList);
                setAvailableStatuses(statusList);
                setTotalElements(totalProjectCount);
                setTotalPages(totalPageCount);

                if (totalPageCount > 0 && page >= totalPageCount) {
                    setPage(totalPageCount - 1);
                }
            } catch (error) {
                console.error("Unable to load projects:", error);

                setProjects([]);
                setAvailableStatuses([]);
                setTotalElements(0);
                setTotalPages(0);
            }
        };

        loadProjects();
    }, [page, search, status, sortBy, sortDirection]);

    const pageNumbers = createPageNumbers(page, totalPages);

    const handleSort = (field) => {
        setPage(0);

        if (sortBy === field) {
            setSortDirection((currentDirection) =>
                currentDirection === "asc" ? "desc" : "asc"
            );
            return;
        }

        setSortBy(field);
        setSortDirection("asc");
    };

    const handleStatusChange = (event) => {
        setStatus(event.target.value);
        setPage(0);
    };

    const clearFilters = () => {
        setSearchInput("");
        setSearch("");
        setStatus("");
        setPage(0);
    };

    const openProjectDetail = (projectId) => {
        if (!projectId) {
            return;
        }

        navigate(`/project-management/view?id=${projectId}`);
    };

    const handleProjectRowKeyDown = (event, projectId) => {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            openProjectDetail(projectId);
        }
    };

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

                        {availableStatuses.map((availableStatus) => (
                            <option key={availableStatus} value={availableStatus}>
                                {availableStatus}
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
                        {projects.length === 0 ? (
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
                        disabled={page === 0}
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
                        disabled={totalPages === 0 || page >= totalPages - 1}
                    >
                        <Icon name="arrowRight" size={18} color="#243452" />
                    </Pagination.Next>
                </Pagination>
            </div>
        </PagePanel>
    );
}

export default ListProject;
