import { useEffect, useState } from "react";
import { Badge, Button, Nav, Table } from "react-bootstrap";
import { IconBuildingSkyscraper, IconCalendar, IconCheck, IconDots, IconEdit, IconFileText, IconHash, IconHierarchy, IconListDetails, IconPlus, IconSettings, IconUser, IconUserPlus, IconUsers } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import { getDepartmentById } from "../../config/departmentApi/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function ViewDepartment() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [department, setDepartment] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDepartment = async () => {
      try {
        const response = await getDepartmentById(id);
        setDepartment(response.data?.data || null);
      } catch (requestError) {
        setError(requestError.response?.data?.message || "Unable to load department.");
      } finally {
        setIsLoading(false);
      }
    };

    loadDepartment();
  }, [id]);

  const departmentName = department?.departmentName || "-";
  const departmentStatus = department?.departmentStatus || "Inactive";

  return (
    <div className="department-layout">
      <aside className="department-sidebar">
        <p>Department Management</p>
        <Nav className="department-sidebar-nav">
          <Nav.Link active onClick={() => navigate("/department-management/list")}><IconListDetails size={21} /><span>List Department</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/create")}><IconPlus size={21} /><span>Create Department</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/list")}><IconHierarchy size={21} /><span>Organization Chart</span></Nav.Link>
          <Nav.Link><IconUsers size={21} /><span>Team Members</span></Nav.Link>
          <Nav.Link onClick={() => navigate(`/department-management/update/${id}`)}><IconSettings size={21} /><span>Department Settings</span></Nav.Link>
        </Nav>
      </aside>

      <section className="department-content">
        <div className="department-panel">
          <header className="department-panel-header">
            <div><h1>View Department</h1><p>View department details, hierarchy, and team members.</p></div>
            <div className="department-header-actions">
              <Button className="department-primary-button" onClick={() => navigate(`/department-management/update/${id}`)} disabled={!department}><IconEdit size={19} /> Edit Department</Button>
              <Button variant="light" className="department-icon-button"><IconDots /></Button>
            </div>
          </header>

          <div className="department-view-body">
            {isLoading && <div className="alert alert-info">Loading department...</div>}
            {error && <div className="alert alert-danger">{error}</div>}
            <section className="department-view-section">
              <h2>Department Overview</h2>
              <div className="department-overview">
                <div className="department-overview-column">
                  <div className="department-info"><IconBuildingSkyscraper size={21} /><span>Department Name</span><div>{departmentName}</div></div>
                  <div className="department-info"><IconHash size={21} /><span>Department Code</span><div>{department?.departmentCode || "-"}</div></div>
                  <div className="department-info"><IconHierarchy size={21} /><span>Parent Department</span><div>-</div></div>
                  <div className="department-info"><IconUser size={21} /><span>Head of Department</span><div>-</div></div>
                  <div className="department-info"><IconFileText size={21} /><span>Description</span><div>-</div></div>
                </div>

                <div className="department-overview-column">
                  <div className="department-info"><IconUserPlus size={21} /><span>Created By</span><div>-</div></div>
                  <div className="department-info"><IconCalendar size={21} /><span>Created On</span><div>{department?.departmentCreateAt || "-"}</div></div>
                  <div className="department-info"><IconUserPlus size={21} /><span>Updated By</span><div>-</div></div>
                  <div className="department-info"><IconCalendar size={21} /><span>Updated On</span><div>-</div></div>
                  <div className="department-info"><IconCheck size={21} /><span>Status</span><div><Badge className={`department-status department-status--${departmentStatus.toLowerCase()}`}>{departmentStatus}</Badge></div></div>
                </div>
              </div>
            </section>

            <section className="department-view-section">
              <h2>Department Hierarchy</h2>
              <div className="department-hierarchy"><span>-</span><b>&gt;</b><span>{departmentName}</span></div>
            </section>

            <section className="department-view-section department-view-section--flush">
              <h2>Members (0)</h2>
              <Table responsive className="department-members-table mb-0">
                <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined On</th></tr></thead>
                <tbody><tr><td colSpan={4} className="text-center py-4">Member data is not available in the Department API.</td></tr></tbody>
              </Table>
            </section>
          </div>
        </div>
      </section>
    </div>
  );
}

export default ViewDepartment;
