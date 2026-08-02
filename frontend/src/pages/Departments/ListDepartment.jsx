import { useEffect, useState } from "react";
import { Badge, Button, Form, Table } from "react-bootstrap";
import { IconBuildingSkyscraper, IconPlus, IconRefresh, IconSearch, IconSelector } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(new Date(value))
  : "-";

function ListDepartment() {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;
    const timer = setTimeout(async () => {
      setIsLoading(true);
      setError("");

      try {
        const response = await departmentApi.searchDepartments({
          search: search.trim(),
          status,
        });

        if (isMounted) {
          setDepartments(Array.isArray(response.data?.data) ? response.data.data : []);
        }
      } catch (requestError) {
        if (isMounted) {
          setDepartments([]);
          setError(requestError.response?.data?.message || "Unable to load departments.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }, 300);

    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
  }, [search, status, refreshKey]);

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel">
          <header className="department-panel-header">
            <div><h1>Departments</h1><p>Manage organizational departments.</p></div>
            <div className="department-header-actions">
              <Button className="department-primary-button" onClick={() => navigate("/department-management/create")}><IconPlus size={20} /> New Department</Button>
            </div>
          </header>

          <div className="department-list-body">
            <div className="department-toolbar">
              <div className="department-search"><IconSearch size={22} /><Form.Control value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search departments..." /></div>
              <label className="department-filter"><span>Status</span><Form.Select value={status} onChange={(event) => setStatus(event.target.value)}><option value="">All</option><option>Active</option><option>Inactive</option></Form.Select></label>
              <Button variant="light" className="department-icon-button" aria-label="Reload departments" onClick={() => { setSearch(""); setStatus(""); setRefreshKey((current) => current + 1); }}><IconRefresh size={21} /></Button>
            </div>

            <div className="department-table-wrap">
              <Table responsive hover className="department-table mb-0">
                <thead><tr>{["Department Name", "Code", "Company ID", "Status", "Created At", "Updated At"].map((label) => <th key={label}>{label} <IconSelector size={13} /></th>)}</tr></thead>
                <tbody>{isLoading ? (
                  <tr><td colSpan={6} className="text-center py-5">Loading departments...</td></tr>
                ) : error ? (
                  <tr><td colSpan={6} className="text-center text-danger py-5">{error}</td></tr>
                ) : departments.length === 0 ? (
                  <tr><td colSpan={6} className="text-center py-5">No departments found.</td></tr>
                ) : departments.map((item) => (
                  <tr key={item.id} onClick={() => navigate(`/department-management/view/${item.id}`)}>
                    <td><span className="department-avatar department-avatar--building"><IconBuildingSkyscraper size={20} /></span><strong>{item.departmentName}</strong></td>
                    <td>{item.departmentCode}</td>
                    <td>{item.companyId ?? "-"}</td>
                    <td><Badge className={`department-status department-status--${item.departmentStatus?.toLowerCase() || "inactive"}`}>{item.departmentStatus || "Inactive"}</Badge></td>
                    <td>{formatDateTime(item.departmentCreatedAt)}</td>
                    <td>{formatDateTime(item.updatedAt)}</td>
                  </tr>
                ))}</tbody>
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
