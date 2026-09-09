import { useEffect, useState } from "react";
import { Alert, Badge, Button, Form, Spinner, Table } from "react-bootstrap";
import {
  IconBuildingSkyscraper,
  IconEdit,
  IconEye,
  IconPlus,
  IconRefresh,
  IconSearch,
  IconSelector,
} from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import {
  formatDepartmentDate,
  getDepartmentErrorMessage,
} from "./departmentUtils.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function ListDepartment() {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const requestController = new AbortController();
    const timer = setTimeout(async () => {
      try {
        setIsLoading(true);
        setError("");
        const response = await departmentApi.searchDepartments({
          search: search.trim(),
          status,
        }, requestController.signal);

        if (requestController.signal.aborted) {
          return;
        }

        setDepartments(Array.isArray(response.data?.data) ? response.data.data : []);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load departments:", requestError);
        setDepartments([]);
        setError(getDepartmentErrorMessage(
          requestError,
          "Unable to load departments. Please try again later."
        ));
      } finally {
        if (!requestController.signal.aborted) {
          setIsLoading(false);
        }
      }
    }, 300);

    return () => {
      clearTimeout(timer);
      requestController.abort();
    };
  }, [search, status, refreshKey]);

  function viewDepartment(id) {
    navigate(`/department-management/view/${id}`);
  }

  function editDepartment(event, id) {
    event.stopPropagation();
    navigate(`/department-management/update/${id}`);
  }

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel">
          <header className="department-panel-header">
            <div>
              <h1>Departments</h1>
              <p>Manage organizational departments.</p>
            </div>
            <div className="department-header-actions">
              <Button
                className="department-primary-button"
                onClick={() => navigate("/department-management/create")}
              >
                <IconPlus size={20} /> New Department
              </Button>
            </div>
          </header>

          <div className="department-list-body">
            <div className="department-toolbar">
              <div className="department-search">
                <IconSearch size={22} />
                <Form.Control
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search departments..."
                />
              </div>
              <label className="department-filter">
                <span>Status</span>
                <Form.Select
                  value={status}
                  onChange={(event) => setStatus(event.target.value)}
                >
                  <option value="">All</option>
                  <option value="Active">Active</option>
                  <option value="Inactive">Inactive</option>
                </Form.Select>
              </label>
              <Button
                variant="light"
                className="department-icon-button"
                aria-label="Reload departments"
                onClick={() => {
                  setSearch("");
                  setStatus("");
                  setRefreshKey((currentKey) => currentKey + 1);
                }}
              >
                <IconRefresh size={21} />
              </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <div className="department-table-wrap">
              <Table responsive hover className="department-table mb-0">
                <thead>
                  <tr>
                    {["Department Name", "Code", "Company ID", "Status", "Created At", "Updated At", "Actions"].map((label) => (
                      <th key={label}>{label} {label !== "Actions" && <IconSelector size={13} />}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {isLoading ? (
                    <tr>
                      <td colSpan={7} className="text-center py-5">
                        <Spinner animation="border" size="sm" /> Loading departments...
                      </td>
                    </tr>
                  ) : error ? (
                    <tr><td colSpan={7} className="text-center text-danger py-5">Unable to display departments.</td></tr>
                  ) : departments.length === 0 ? (
                    <tr><td colSpan={7} className="text-center py-5">No departments found.</td></tr>
                  ) : departments.map((item) => {
                    const departmentStatus = item.departmentStatus || "Unknown";

                    return (
                      <tr key={item.id} onClick={() => viewDepartment(item.id)}>
                        <td><span className="department-avatar department-avatar--building"><IconBuildingSkyscraper size={20} /></span><strong>{item.departmentName}</strong></td>
                        <td>{item.departmentCode}</td>
                        <td>{item.companyId ?? "-"}</td>
                        <td><Badge className={`department-status department-status--${departmentStatus.toLowerCase()}`}>{departmentStatus}</Badge></td>
                        <td>{formatDepartmentDate(item.departmentCreatedAt)}</td>
                        <td>{formatDepartmentDate(item.updatedAt)}</td>
                        <td>
                          <div className="department-table-actions">
                            <Button
                              className="department-row-action"
                              aria-label={`View ${item.departmentName}`}
                              onClick={(event) => {
                                event.stopPropagation();
                                viewDepartment(item.id);
                              }}
                            >
                              <IconEye size={17} /> View
                            </Button>
                            <Button
                              className="department-row-action"
                              aria-label={`Edit ${item.departmentName}`}
                              onClick={(event) => editDepartment(event, item.id)}
                            >
                              <IconEdit size={17} /> Edit
                            </Button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            </div>

            <footer className="department-list-footer">
              <span>Showing {departments.length} result{departments.length === 1 ? "" : "s"}</span>
            </footer>
          </div>
        </div>
      </section>
    </div>
  );
}

export default ListDepartment;
