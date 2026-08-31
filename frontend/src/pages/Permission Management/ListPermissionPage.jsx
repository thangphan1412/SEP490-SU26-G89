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

// Khai báo các cột có thể sắp xếp trong bảng quyền.
const sortableColumns = [
  ["Permission", "permissionName"],
  ["Permission Code", "permissionCode"],
  ["Project", "projectName"],
  ["Status", "status"],
];

// Tạo danh sách số trang rút gọn và chèn dấu ba chấm khi cần.
function createPageNumbers(currentPage, totalPages) {
  // Hiển thị toàn bộ số trang khi tổng số trang không quá năm.
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


// Chọn màn hình danh sách, chi tiết hoặc cập nhật theo query string.
function ListPermissionPage() {
  const [searchParams] = useSearchParams();
  const viewingPermissionId = searchParams.get("view");
  const editingPermissionId = searchParams.get("edit");

  // Mở màn hình chi tiết khi URL chứa tham số view.
  if (viewingPermissionId) {
    return <ViewPermissionPage />;
  }

  // Mở màn hình cập nhật khi URL chứa tham số edit.
  if (editingPermissionId) {
    return <UpdatePermissionPage />;
  }

  return <PermissionListContent />;
}

// Hiển thị nội dung danh sách quyền cùng bộ lọc, sắp xếp và phân trang.
function PermissionListContent() {
  const navigate = useNavigate();
  const [permissions, setPermissions] = useState([]);
  const [projects, setProjects] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [projectId, setProjectId] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);
  const [deleteVersion, setDeleteVersion] = useState(0);

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

  // Tải danh sách dự án để hiển thị trong bộ lọc.
  useEffect(function () {
    const requestController = new AbortController();

    // Gọi API và chuẩn hóa danh sách dự án dùng cho bộ lọc.
    async function loadFilterOptions() {
      try {
        const projectPayload = await listPermissionProjects(
          requestController.signal
        );

        if (requestController.signal.aborted) {
          return;
        }

        setProjects(Array.isArray(projectPayload) ? projectPayload : []);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load permission filters:", requestError);
      }
    }

    loadFilterOptions();

    return function () {
      requestController.abort();
    };
  }, []);

  // Tải danh sách quyền theo bộ lọc, sắp xếp và phân trang hiện tại.
  useEffect(function () {
    const requestController = new AbortController();

    // Gọi API và đồng bộ dữ liệu quyền cùng thông tin phân trang.
    async function loadPermissions() {
      try {
        setLoading(true);
        setError("");
        const payload = await listPermissions(
          {
            search,
            projectId: projectId || undefined,
            status: status === "" ? undefined : status === "true",
            page,
            sortBy,
            sortDirection,
          },
          requestController.signal
        );
        const items = Array.isArray(payload?.items) ? payload.items : [];
        const responseTotalPages = Number(payload?.totalPages) || 0;

        if (requestController.signal.aborted) {
          return;
        }

        setPermissions(items);
        setTotalPages(responseTotalPages);

        // Đưa trang hiện tại về giới hạn hợp lệ sau khi dữ liệu thay đổi.
        if (responseTotalPages > 0 && page >= responseTotalPages) {
          setPage(responseTotalPages - 1);
        }
      } catch (requestError) {
        if (requestController.signal.aborted) {
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
        if (!requestController.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadPermissions();

    return function () {
      requestController.abort();
    };
  }, [deleteVersion, page, projectId, search, sortBy, sortDirection, status]);

  // Đổi trường sắp xếp hoặc đảo chiều khi chọn lại cùng một cột.
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

  // Xóa toàn bộ bộ lọc và đưa danh sách về trang đầu.
  function clearFilters() {
    setSearchInput("");
    setSearch("");
    setProjectId("");
    setStatus("");
    setPage(0);
  }

  // Điều hướng tới màn hình chi tiết của quyền được chọn.
  function openPermissionDetail(permissionId) {
    navigate(`/permission/list?view=${permissionId}`);
  }

  // Hỗ trợ mở chi tiết quyền bằng bàn phím trên hàng bảng.
  function handleRowKeyDown(event, permissionId) {
    // Chỉ xử lý phím Enter hoặc Space như thao tác nhấp chuột.
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openPermissionDetail(permissionId);
    }
  }

  // Xác nhận rồi gửi yêu cầu xóa quyền được chọn.
  async function handleDelete(event, permission) {
    event.stopPropagation();

    // Hủy thao tác khi người dùng không xác nhận xóa.
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

  // Cập nhật bộ lọc dự án và quay về trang đầu.
  function handleProjectFilterChange(event) {
    setProjectId(event.target.value);
    setPage(0);
  }

  // Cập nhật bộ lọc trạng thái và quay về trang đầu.
  function handleStatusFilterChange(event) {
    setStatus(event.target.value);
    setPage(0);
  }

  // Điều hướng tới màn hình chỉnh sửa mà không kích hoạt sự kiện của hàng.
  function openPermissionEdit(event, permissionId) {
    event.stopPropagation();
    navigate(`/permission/list?edit=${permissionId}`);
  }

  const pageNumbers = createPageNumbers(page, totalPages);
  const filtersAreActive = Boolean(searchInput || projectId || status);
  const canCreatePermission = projects.some(
    (project) => project.canManage === true
  );
  const createAction = canCreatePermission ? (
    <Button className="permission-primary-button" onClick={() => navigate("/permission/create")}>
      <IconPlus size={19} />
      Create Permission
    </Button>
  ) : null;

  return (
    <PermissionPage
      title="Permissions"
      description="Manage access rules by project, module, and status."
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
                <td colSpan={5} className="permission-list-state">
                  <Spinner animation="border" size="sm" /> Loading permissions...
                </td>
              </tr>
            ) : permissions.length === 0 ? (
              <tr>
                <td colSpan={5} className="permission-list-state">
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
                  <td className="permission-status-cell"><PermissionStatusBadge status={permission.status} /></td>
                  <td className="permission-actions-cell">
                    {permission.canManage ? (
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
                    ) : (
                      <span>View only</span>
                    )}
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

// Hiển thị biểu tượng phù hợp với trạng thái sắp xếp của cột.
function SortIcon({ field, sortBy, sortDirection }) {
  if (field !== sortBy) {
    return <IconArrowsSort size={14} />;
  }

  return sortDirection === "asc" ? <IconArrowUp size={14} /> : <IconArrowDown size={14} />;
}

export default ListPermissionPage;
