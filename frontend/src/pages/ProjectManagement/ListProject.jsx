import { useEffect, useState } from "react";
import { listProjects } from "../../config/axiosConfig.js";
import { Icon, PagePanel, StatusBadge } from "./ProjectComponents.jsx";
import "./ListProject.css";


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
        }, 1000);

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

    return (
        <PagePanel
            title="Projects"
            description="View and find projects, timelines, ownership, and current status."
        >
            <div className="list-project-toolbar">
                {/* Search box and status filter */}
                <label className="list-project-search-box">
                    <Icon name="search" size={22} color="#3f4d6f" />

                    <input
                        aria-label="Search projects"
                        placeholder="Search by code, name, description, or creator..."
                        className="list-project-search-input"
                        value={searchInput}
                        onChange={(event) => setSearchInput(event.target.value)}
                    />

                    {searchInput && (
                        <button
                            type="button"
                            aria-label="Clear search"
                            className="list-project-clear-search"
                            onClick={() => setSearchInput("")}
                        >
                            ×
                        </button>
                    )}
                </label>

                {/* Status filter dropdown */}
                <label className="list-project-select-box">
                    <span className="list-project-select-label">Status</span>

                    <select
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
                    </select>

                    <span className="list-project-select-icon">
                        <Icon name="chevron" size={18} color="#243452" />
                    </span>
                </label>

                {(searchInput || status) && (
                    <button
                        type="button"
                        className="list-project-filter-button"
                        onClick={clearFilters}
                    >
                        Clear filters
                    </button>
                )}
            </div>

            {/* //Table of projects */}
            <div className="list-project-table-wrap">
                <table className="list-project-table">
                    <thead>
                        <tr>
                            {sortableColumns.map(([label, field]) => (
                                <th key={field} className="list-project-th">
                                    <button
                                        type="button"
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
                                    </button>
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
                                <tr key={project.id} className="list-project-row">
                                    <td className="list-project-project-cell">
                                        <span className="project-icon-circle list-project-avatar">
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
                                            {project.projectCode || "—"}
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
                                        {project.projectCreatedBy || "—"}
                                    </td>

                                    <td className="list-project-td">
                                        {project.projectCreatedAt}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div className="list-project-footer">
                <span>Total: {totalElements} results</span>

                {/*Pagination controls */}
                <div className="list-project-pagination">
                    <button
                        type="button"
                        aria-label="Previous page"
                        className="list-project-page-button"
                        onClick={() =>
                            setPage((currentPage) => Math.max(0, currentPage - 1))
                        }
                        disabled={page === 0}
                    >
                        <Icon name="arrowLeft" size={18} color="#243452" />
                    </button>

                    {pageNumbers.map((pageNumber) =>
                        typeof pageNumber === "number" ? (
                            <button
                                key={pageNumber}
                                type="button"
                                className={`list-project-page-button ${pageNumber === page
                                        ? "list-project-page-button--current"
                                        : ""
                                    }`}
                                onClick={() => setPage(pageNumber)}
                            >
                                {pageNumber + 1}
                            </button>
                        ) : (
                            <span key={pageNumber} className="list-project-ellipsis">
                                …
                            </span>
                        )
                    )}

                    <button
                        type="button"
                        aria-label="Next page"
                        className="list-project-page-button"
                        onClick={() =>
                            setPage((currentPage) =>
                                Math.min(totalPages - 1, currentPage + 1)
                            )
                        }
                        disabled={totalPages === 0 || page >= totalPages - 1}
                    >
                        <Icon name="arrowRight" size={18} color="#243452" />
                    </button>
                </div>
            </div>
        </PagePanel>
    );
}

export default ListProject;