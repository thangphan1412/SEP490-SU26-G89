import { useEffect, useState } from "react";
import { Badge, Button } from "react-bootstrap";
import { IconBuilding, IconBuildingSkyscraper, IconCalendar, IconCheck, IconEdit, IconHash, IconId } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "long", timeStyle: "short" }).format(new Date(value))
  : "-";

function ViewDepartment() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [department, setDepartment] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDepartment = async () => {
      try {
        const response = await departmentApi.getDepartmentById(id);
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
      <section className="department-content">
        <div className="department-panel">
          <header className="department-panel-header">
            <div><h1>View Department</h1><p>View the information stored for this department.</p></div>
            <div className="department-header-actions">
              <Button className="department-primary-button" onClick={() => navigate(`/department-management/update/${id}`)} disabled={!department}><IconEdit size={19} /> Edit Department</Button>
            </div>
          </header>

          <div className="department-view-body">
            {isLoading && <div className="alert alert-info">Loading department...</div>}
            {error && <div className="alert alert-danger">{error}</div>}
            <section className="department-view-section">
              <h2>Department Overview</h2>
              <div className="department-overview">
                <div className="department-overview-column">
                  <div className="department-info"><IconId size={21} /><span>ID</span><div>{department?.id ?? "-"}</div></div>
                  <div className="department-info"><IconBuildingSkyscraper size={21} /><span>Department Name</span><div>{departmentName}</div></div>
                  <div className="department-info"><IconHash size={21} /><span>Department Code</span><div>{department?.departmentCode || "-"}</div></div>
                  <div className="department-info"><IconBuilding size={21} /><span>Company ID</span><div>{department?.companyId ?? "Not assigned"}</div></div>
                </div>

                <div className="department-overview-column">
                  <div className="department-info"><IconCheck size={21} /><span>Status</span><div><Badge className={`department-status department-status--${departmentStatus.toLowerCase()}`}>{departmentStatus}</Badge></div></div>
                  <div className="department-info"><IconCalendar size={21} /><span>Created At</span><div>{formatDateTime(department?.departmentCreatedAt)}</div></div>
                  <div className="department-info"><IconCalendar size={21} /><span>Updated At</span><div>{formatDateTime(department?.updatedAt)}</div></div>
                </div>
              </div>
            </section>
          </div>
        </div>
      </section>
    </div>
  );
}

export default ViewDepartment;
