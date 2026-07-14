import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Container, Form, Pagination, Spinner, Stack, Table } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/ListPermissionPage.css";
import {
  deletePermission,
  listPermissionProjects,
  listPermissions,
} from "../../config/axiosConfig.js";
import UpdatePermissionPage from "./UpdatePermissionPage.jsx";
import ViewPermissionPage from "./ViewPermissionPage.jsx";

const sortableColumns = [
  ["Permission Name", "permissionName"],
  ["Permission Code", "permissionCode"],
  ["Project", "projectName"],
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

function ListPermissionPage() {
  const [searchParams] = useSearchParams();
  const viewingPermissionId = searchParams.get("view");
  const editingPermissionId = searchParams.get("edit");

  if (viewingPermissionId) {
    return <ViewPermissionPage />;
  }

  if (editingPermissionId) {
    return <UpdatePermissionPage />;
  }

  return <PermissionListContent />;
}

function PermissionListContent() {
  const navigate = useNavigate();
  const [permissions, setPermissions] = useState([]);
  const [projects, setProjects] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [projectId, setProjectId] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState("id");
  const [sortDirection, setSortDirection] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);
  const [deleteVersion, setDeleteVersion] = useState(0);

  useEffect(() => {
    const debounceId = window.setTimeout(() => {
      setSearch(searchInput.trim());
      setPage(0);
    }, 500);

    return () => window.clearTimeout(debounceId);
  }, [searchInput]);

  useEffect(() => {
    let isActive = true;

    const loadProjects = async () => {
      try {
        const response = await listPermissionProjects();
        const payload = response.data?.data ?? response.data;

        if (isActive) {
          setProjects(Array.isArray(payload) ? payload : []);
        }
      } catch (requestError) {
        console.error("Unable to load projects for permission filter:", requestError);
      }
    };

    loadProjects();

    return () => {
      isActive = false;
    };
  }, []);

  useEffect(() => {
    let isActive = true;

    const loadPermissions = async () => {
      try {
        setLoading(true);
        setError("");
        const response = await listPermissions({
          search,
          projectId: projectId ? Number(projectId) : undefined,
          page,
          sortBy,
          sortDirection,
        });
        const payload = response.data?.data ?? response.data;
        const items = Array.isArray(payload?.items) ? payload.items : [];
        const responseTotalPages = Number(payload?.totalPages) || 0;

        if (!isActive) {
          return;
        }

        setPermissions(items);
        setTotalPages(responseTotalPages);

        if (responseTotalPages > 0 && page >= responseTotalPages) {
          setPage(responseTotalPages - 1);
        }
      } catch (requestError) {
        console.error("Unable to load permissions:", requestError);

        if (isActive) {
          setPermissions([]);
          setTotalPages(0);
          setError(getErrorMessage(requestError));
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    };

    loadPermissions();

    return () => {
      isActive = false;
    };
  }, [page, projectId, deleteVersion, search, sortBy, sortDirection]);

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

  const clearFilters = () => {
    setSearchInput("");
    setSearch("");
    setProjectId("");
    setPage(0);
  };

  const openPermissionDetail = (permissionId) => {
    navigate(`/permission/list?view=${permissionId}`);
  };

  const handleRowKeyDown = (event, permissionId) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openPermissionDetail(permissionId);
    }
  };

  const handleDelete = async (event, permission) => {
    event.stopPropagation();

    if (!window.confirm(`Delete permission "${permission.permissionName}"?`)) {
      return;
    }

    try {
      setDeletingId(permission.id);
      setError("");
      await deletePermission(permission.id);
      setDeleteVersion((currentVersion) => currentVersion + 1);
    } catch (requestError) {
      console.error("Unable to delete permission:", requestError);
      setError(getErrorMessage(requestError));
    } finally {
      setDeletingId(null);
    }
  };

  const pageNumbers = createPageNumbers(page, totalPages);

  return (
    <Container fluid as="main" className="list-page">
      <Card className="list-card">
        <Card.Header className="list-header">
          <div>
            <Card.Title as="h1">Permissions</Card.Title>
            <Card.Text>View, filter, create, update, and delete permissions.</Card.Text>
          </div>

          <Button
            type="button"
            className="list-primary-button"
            onClick={() => navigate("/permission/create")}
          >
            + Create Permission
          </Button>
        </Card.Header>

        <div className="list-toolbar">
          <Form.Control
            className="list-search"
            placeholder="Search by name, code, module, or project..."
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />

          <Form.Select
            className="list-select"
            value={projectId}
            onChange={(event) => {
              setProjectId(event.target.value);
              setPage(0);
            }}
          >
            <option value="">All projects</option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {formatProjectName(project)}
              </option>
            ))}
          </Form.Select>

          {(searchInput || projectId) && (
            <Button
              type="button"
              variant="light"
              className="list-outline-button"
              onClick={clearFilters}
            >
              Clear filters
            </Button>
          )}

        </div>

        {error && <Alert variant="danger" className="mx-4 mb-0">{error}</Alert>}

        <div className="list-table-wrapper">
          <Table hover responsive={false} className="list-table mb-0">
            <thead>
              <tr>
                {sortableColumns.map(([label, field]) => (
                  <th key={field}>
                    <Button
                      type="button"
                      variant="link"
                      className="list-sort-button"
                      onClick={() => handleSort(field)}
                    >
                      {label}{sortBy === field ? (sortDirection === "asc" ? " ↑" : " ↓") : ""}
                    </Button>
                  </th>
                ))}
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={4} className="list-state-cell">
                    <Spinner animation="border" size="sm" /> Loading permissions...
                  </td>
                </tr>
              ) : permissions.length === 0 ? (
                <tr>
                  <td colSpan={4} className="list-state-cell">
                    No permissions found. Try changing the filters or create a permission.
                  </td>
                </tr>
              ) : (
                permissions.map((permission) => (
                  <tr
                    key={permission.id}
                    className="list-row"
                    role="button"
                    tabIndex={0}
                    onClick={() => openPermissionDetail(permission.id)}
                    onKeyDown={(event) => handleRowKeyDown(event, permission.id)}
                  >
                    <td>
                      <span className="list-name-value">
                        <span className="list-icon">PERM</span>
                        <strong>{permission.permissionName || "Unnamed permission"}</strong>
                      </span>
                    </td>
                    <td>{permission.permissionCode || "-"}</td>
                    <td>{permission.projectName || "-"}</td>
                    <td>
                      <Stack direction="horizontal" className="list-row-actions">
                        <Button
                          type="button"
                          variant="light"
                          className="list-action-button"
                          onClick={(event) => {
                            event.stopPropagation();
                            navigate(`/permission/list?edit=${permission.id}`);
                          }}
                        >
                          Edit
                        </Button>
                        <Button
                          type="button"
                          variant="light"
                          className="list-delete-button"
                          disabled={deletingId === permission.id}
                          onClick={(event) => handleDelete(event, permission)}
                        >
                          {deletingId === permission.id ? "Deleting..." : "Delete"}
                        </Button>
                      </Stack>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </div>

        <div className="list-footer">
          <Pagination className="list-pages mb-0">
            <Pagination.Prev
              aria-label="Previous page"
              onClick={() => setPage((currentPage) => Math.max(0, currentPage - 1))}
              disabled={page === 0 || loading}
            />

            {pageNumbers.map((pageNumber) => (
              typeof pageNumber === "number" ? (
                <Pagination.Item
                  key={pageNumber}
                  active={pageNumber === page}
                  onClick={() => setPage(pageNumber)}
                  disabled={loading}
                >
                  {pageNumber + 1}
                </Pagination.Item>
              ) : (
                <Pagination.Ellipsis key={pageNumber} disabled />
              )
            ))}

            <Pagination.Next
              aria-label="Next page"
              onClick={() => setPage((currentPage) => Math.min(totalPages - 1, currentPage + 1))}
              disabled={loading || totalPages === 0 || page >= totalPages - 1}
            />
          </Pagination>
        </div>
      </Card>
    </Container>
  );
}

function formatProjectName(project) {
  const projectCode = project.projectCode ? `${project.projectCode} - ` : "";
  return `${projectCode}${project.projectName || `Project #${project.id}`}`;
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to process the permission request. Please try again later.";
}

export default ListPermissionPage;
