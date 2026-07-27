import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  Alert,
  Button,
  Form,
  InputGroup,
  Pagination,
  Spinner,
  Stack,
  Table,
} from "react-bootstrap";
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconArrowUp,
  IconArrowsSort,
  IconEdit,
  IconPlus,
  IconSearch,
  IconShieldCheck,
  IconTrash,
  IconX,
} from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/ListPermissionPage.css";
import {
  deletePermission,
  listPermissionProjects,
  listPermissionRoles,
  listPermissions,
} from "../../services/permissionService/permissionApi.js";
import PermissionPage from "../../components/permissionComponents/PermissionPage.jsx";
import PermissionStatusBadge from "../../components/permissionComponents/PermissionStatusBadge.jsx";
import UpdatePermissionPage from "./UpdatePermissionPage.jsx";
import ViewPermissionPage from "./ViewPermissionPage.jsx";
import {
  formatPermissionProjectName,
  formatPermissionProjectValue,
  getPermissionErrorMessage,
} from "./permissionUtils.js";

// Các cột có thể sắp xếp trong bảng
const sortableColumns = [
  ["Permission", "permissionName"],
  ["Permission Code", "permissionCode"],
  ["Project", "projectName"],
  ["Role", "roleName"],
  ["Status", "status"],
];

function createPageNumbers(currentPage, totalPages) {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => index);
  }

  const candidates = new Set([0, totalPages - 1, currentPage - 1, currentPage, currentPage + 1]);
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


// chuyển hướng sang trang xem hoặc chỉnh sửa nếu có tham số truy vấn
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
  const [roles, setRoles] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [projectId, setProjectId] = useState("");
  const [roleId, setRoleId] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState("id");
  const [sortDirection, setSortDirection] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);
  const [deleteVersion, setDeleteVersion] = useState(0);

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

    async function loadFilterOptions() {
      try {
        const [projectPayload, rolePayload] = await Promise.all([
          listPermissionProjects(requestController.signal),
          listPermissionRoles(requestController.signal),
        ]);

        if (isActive) {
          setProjects(Array.isArray(projectPayload) ? projectPayload : []);
          setRoles(Array.isArray(rolePayload) ? rolePayload : []);
        }
      } catch (requestError) {
        if (isActive) {
          console.error("Unable to load permission filters:", requestError);
        }
      }
    }

    loadFilterOptions();

    return function () {
      isActive = false;
      requestController.abort();
    };
  }, []);

  useEffect(function () {
    let isActive = true;
    const requestController = new AbortController();

    async function loadPermissions() {
      try {
        setLoading(true);
        setError("");
        const payload = await listPermissions(
          {
            search,
            projectId: projectId || undefined,
            roleId: roleId || undefined,
            status: status === "" ? undefined : status === "true",
            page,
            sortBy,
            sortDirection,
          },
          requestController.signal
        );
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
        if (!isActive) {
          return;
        }

        console.error("Unable to load permissions:", requestError);
        setPermissions([]);
        setTotalPages(0);
        setError(getPermissionErrorMessage(
          requestError,
          "Unable to load permissions. Please try again later."
        ));
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadPermissions();

    return function () {
      isActive = false;
      requestController.abort();
    };
  }, [deleteVersion, page, projectId, roleId, search, sortBy, sortDirection, status]);

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

  function clearFilters() {
    setSearchInput("");
    setSearch("");
    setProjectId("");
    setRoleId("");
    setStatus("");
    setPage(0);
  }

  function openPermissionDetail(permissionId) {
    navigate(`/permission/list?view=${permissionId}`);
  }

  function handleRowKeyDown(event, permissionId) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openPermissionDetail(permissionId);
    }
  }

  async function handleDelete(event, permission) {
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
      setError(getPermissionErrorMessage(
        requestError,
        "Unable to delete permission. Please try again later."
      ));
    } finally {
      setDeletingId(null);
    }
  }

  function handleProjectFilterChange(event) {
    setProjectId(event.target.value);
    setPage(0);
  }

  function handleRoleFilterChange(event) {
    setRoleId(event.target.value);
    setPage(0);
  }

  function handleStatusFilterChange(event) {
    setStatus(event.target.value);
    setPage(0);
  }

  function openPermissionEdit(event, permissionId) {
    event.stopPropagation();
    navigate(`/permission/list?edit=${permissionId}`);
  }

  const pageNumbers = createPageNumbers(page, totalPages);
  const filtersAreActive = Boolean(searchInput || projectId || roleId || status);
  const createAction = (
    <Button className="permission-primary-button" onClick={() => navigate("/permission/create")}>
      <IconPlus size={19} />
      Create Permission
    </Button>
  );

  return (
    <PermissionPage
      title="Permissions"
      description="Manage access rules by project, role, module, and status."
      action={createAction}
    >
      <div className="permission-list-toolbar">
        <InputGroup className="permission-list-search">
          <InputGroup.Text><IconSearch size={20} /></InputGroup.Text>
          <Form.Control
            aria-label="Search permissions"
            placeholder="Search name, code, module, description..."
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />
          {searchInput && (
            <Button variant="light" aria-label="Clear search" onClick={() => setSearchInput("")}>
              <IconX size={18} />
            </Button>
          )}
        </InputGroup>

        <Form.Group className="permission-list-filter">
          <Form.Label>Project</Form.Label>
          <Form.Select value={projectId} onChange={handleProjectFilterChange}>
            <option value="">All projects</option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {formatPermissionProjectName(project)}
              </option>
            ))}
          </Form.Select>
        </Form.Group>

        <Form.Group className="permission-list-filter">
          <Form.Label>Role</Form.Label>
          <Form.Select value={roleId} onChange={handleRoleFilterChange}>
            <option value="">All roles</option>
            {roles.map((role) => (
              <option key={role.id} value={role.id}>{role.roleName || `Role #${role.id}`}</option>
            ))}
          </Form.Select>
        </Form.Group>

        <Form.Group className="permission-list-filter">
          <Form.Label>Status</Form.Label>
          <Form.Select value={status} onChange={handleStatusFilterChange}>
            <option value="">All statuses</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </Form.Select>
        </Form.Group>

        {filtersAreActive && (
          <Button variant="light" className="permission-list-clear" onClick={clearFilters}>
            Clear filters
          </Button>
        )}
      </div>

      {error && <Alert variant="danger" className="permission-list-alert">{error}</Alert>}

      <div className="permission-list-table-wrap">
        <Table hover responsive={false} className="permission-list-table mb-0">
          <thead>
            <tr>
              {sortableColumns.map(([label, field]) => (
                <th key={field}>
                  <Button
                    variant="link"
                    className={sortBy === field ? "permission-sort-button active" : "permission-sort-button"}
                    onClick={() => handleSort(field)}
                  >
                    {label}
                    <SortIcon field={field} sortBy={sortBy} sortDirection={sortDirection} />
                  </Button>
                </th>
              ))}
              <th className="permission-actions-heading">Actions</th>
            </tr>
          </thead>

          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="permission-list-state">
                  <Spinner animation="border" size="sm" /> Loading permissions...
                </td>
              </tr>
            ) : permissions.length === 0 ? (
              <tr>
                <td colSpan={6} className="permission-list-state">
                  <span className="permission-empty-icon"><IconShieldCheck size={28} /></span>
                  <strong>No permissions found</strong>
                  <span>Try changing the filters or create a new permission.</span>
                </td>
              </tr>
            ) : (
              permissions.map((permission) => (
                <tr
                  key={permission.id}
                  className="permission-list-row"
                  role="button"
                  tabIndex={0}
                  onClick={() => openPermissionDetail(permission.id)}
                  onKeyDown={(event) => handleRowKeyDown(event, permission.id)}
                >
                  <td className="permission-name-cell">
                    <div className="permission-name-content">
                      <span className="permission-row-icon"><IconShieldCheck size={20} /></span>
                      <span className="permission-name-text">
                        <strong>{permission.permissionName || "Unnamed permission"}</strong>
                      </span>
                    </div>
                  </td>
                  <td className="permission-code-cell">
                    <span className="permission-code-badge">{permission.permissionCode || "-"}</span>
                  </td>
                  <td className="permission-project-cell">
                    {formatPermissionProjectValue(permission)}
                  </td>
                  <td className="permission-role-cell">{permission.roleName || "Unassigned"}</td>
                  <td className="permission-status-cell"><PermissionStatusBadge status={permission.status} /></td>
                  <td className="permission-actions-cell">
                    <Stack direction="horizontal" className="permission-row-actions">
                      <Button
                        variant="light"
                        className="permission-table-action"
                        onClick={(event) => openPermissionEdit(event, permission.id)}
                      >
                        <IconEdit size={17} /> Edit
                      </Button>
                      <Button
                        variant="light"
                        className="permission-table-delete"
                        disabled={deletingId === permission.id}
                        onClick={(event) => handleDelete(event, permission)}
                      >
                        <IconTrash size={17} />
                        {deletingId === permission.id ? "Deleting" : "Delete"}
                      </Button>
                    </Stack>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </div>

      <div className="permission-list-footer">
        <Pagination className="permission-list-pagination mb-0">
          <Pagination.Prev onClick={() => setPage((currentPage) => Math.max(0, currentPage - 1))} disabled={page === 0 || loading}>
            <IconArrowLeft size={17} />
          </Pagination.Prev>
          {pageNumbers.map((pageNumber) => typeof pageNumber === "number" ? (
            <Pagination.Item key={pageNumber} active={pageNumber === page} onClick={() => setPage(pageNumber)} disabled={loading}>
              {pageNumber + 1}
            </Pagination.Item>
          ) : <Pagination.Ellipsis key={pageNumber} disabled />)}
          <Pagination.Next onClick={() => setPage((currentPage) => Math.min(totalPages - 1, currentPage + 1))} disabled={loading || totalPages === 0 || page >= totalPages - 1}>
            <IconArrowRight size={17} />
          </Pagination.Next>
        </Pagination>
      </div>
    </PermissionPage>
  );
}

function SortIcon({ field, sortBy, sortDirection }) {
  if (field !== sortBy) {
    return <IconArrowsSort size={14} />;
  }

  return sortDirection === "asc" ? <IconArrowUp size={14} /> : <IconArrowDown size={14} />;
}

export default ListPermissionPage;
