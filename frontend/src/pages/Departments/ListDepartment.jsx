import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Form, Nav, Table } from "react-bootstrap";
import { IconBuildingSkyscraper, IconChevronLeft, IconChevronRight, IconDots, IconFilter, IconHierarchy, IconListDetails, IconPlus, IconRefresh, IconSearch, IconSelector, IconSettings, IconUsers } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import { getAllDepartments } from "../../config/departmentApi/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function ListDepartment() {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDepartments = async () => {
    setIsLoading(true);
    setError("");

    try {
      const response = await getAllDepartments();
      setDepartments(Array.isArray(response.data?.data) ? response.data.data : []);
    } catch (requestError) {
      setDepartments([]);
      setError(requestError.response?.data?.message || "Unable to load departments.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    let isMounted = true;

    getAllDepartments()
      .then((response) => {
        if (isMounted) {
          setDepartments(Array.isArray(response.data?.data) ? response.data.data : []);
        }
      })
      .catch((requestError) => {
        if (isMounted) {
          setDepartments([]);
          setError(requestError.response?.data?.message || "Unable to load departments.");
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredDepartments = useMemo(() => departments.filter((item) =>
    (!search || `${item.departmentName} ${item.departmentCode}`.toLowerCase().includes(search.toLowerCase())) &&
    (!status || item.departmentStatus?.toLowerCase() === status.toLowerCase())
  ), [departments, search, status]);

  return (
    <div className="department-layout">
      <aside className="department-sidebar">
        <p>Department Management</p>
        <Nav className="department-sidebar-nav">
          <Nav.Link active onClick={() => navigate("/department-management/list")}><IconListDetails size={21} /><span>List Department</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/create")}><IconPlus size={21} /><span>Create Department</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/list")}><IconHierarchy size={21} /><span>Organization Chart</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/view/2")}><IconUsers size={21} /><span>Team Members</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/update/2")}><IconSettings size={21} /><span>Department Settings</span></Nav.Link>
        </Nav>
      </aside>

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
              <label className="department-filter"><span>Parent Department</span><Form.Select disabled><option>Not available</option></Form.Select></label>
              <Button variant="light" className="department-tool-button"><IconFilter size={20} /> Filters</Button>
              <Button variant="light" className="department-icon-button" aria-label="Reload departments" onClick={() => { setSearch(""); setStatus(""); loadDepartments(); }}><IconRefresh size={21} /></Button>
            </div>

            <div className="department-table-wrap">
              <Table responsive hover className="department-table mb-0">
                <thead><tr>{["Department Name", "Code", "Parent Department", "Head", "Status"].map((label) => <th key={label}>{label} <IconSelector size={13} /></th>)}<th>Actions</th></tr></thead>
                <tbody>{isLoading ? (
                  <tr><td colSpan={6} className="text-center py-5">Loading departments...</td></tr>
                ) : error ? (
                  <tr><td colSpan={6} className="text-center text-danger py-5">{error}</td></tr>
                ) : filteredDepartments.length === 0 ? (
                  <tr><td colSpan={6} className="text-center py-5">No departments found.</td></tr>
                ) : filteredDepartments.map((item) => (
                  <tr key={item.id} onClick={() => navigate(`/department-management/view/${item.id}`)}>
                    <td><span className="department-avatar department-avatar--building"><IconBuildingSkyscraper size={20} /></span><strong>{item.departmentName}</strong></td>
                    <td>{item.departmentCode}</td><td>-</td>
                    <td><span className="department-head"><strong>-</strong></span></td>
                    <td><Badge className={`department-status department-status--${item.departmentStatus?.toLowerCase() || "inactive"}`}>{item.departmentStatus || "Inactive"}</Badge></td>
                    <td><Button variant="light" className="department-row-action" aria-label={`Actions for ${item.departmentName}`} onClick={(event) => event.stopPropagation()}><IconDots /></Button></td>
                  </tr>
                ))}</tbody>
              </Table>
            </div>

            <footer className="department-list-footer">
              <span>Showing 1 to {filteredDepartments.length} of {filteredDepartments.length} results</span>
              <div><Button variant="light"><IconChevronLeft size={18} /></Button><Button className="active">1</Button><Button variant="light"><IconChevronRight size={18} /></Button><Form.Select><option>10 / page</option></Form.Select></div>
            </footer>
          </div>
        </div>
      </section>
    </div>
  );
}

export default ListDepartment;
