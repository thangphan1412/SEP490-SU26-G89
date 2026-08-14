import { useEffect, useState } from "react";
import { Alert, Badge, Button, Spinner } from "react-bootstrap";
import {
  IconArrowLeft,
  IconBuilding,
  IconBuildingSkyscraper,
  IconCalendar,
  IconCheck,
  IconEdit,
  IconHash,
  IconId,
  IconRefresh,
} from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import {
  formatDepartmentDate,
  getDepartmentLoadErrorMessage,
  isValidDepartmentId,
} from "./departmentUtils.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function ViewDepartment() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [department, setDepartment] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const requestController = new AbortController();

    async function loadDepartment() {
      if (!isValidDepartmentId(id)) {
        setDepartment(null);
        setError("The department ID is invalid. Please choose a department from the list.");
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError("");
        setDepartment(null);
        const response = await departmentApi.getDepartmentById(
          id,
          requestController.signal
        );

        if (requestController.signal.aborted) {
          return;
        }

        const payload = response.data?.data ?? response.data;
        setDepartment(payload || null);

        if (!payload) {
          setError("Department was not found.");
        }
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load department:", requestError);
        setDepartment(null);
        setError(getDepartmentLoadErrorMessage(requestError));
      } finally {
        if (!requestController.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadDepartment();

    return () => {
      requestController.abort();
    };
  }, [id, reloadKey]);

  const departmentStatus = department?.departmentStatus || "Unknown";

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel">
          <header className="department-panel-header">
            <div>
              <h1>View Department</h1>
              <p>View the information stored for this department.</p>
            </div>
            <div className="department-header-actions">
              <Button
                className="department-secondary-button"
                onClick={() => navigate("/department-management/list")}
              >
                <IconArrowLeft size={18} /> Back
              </Button>
              {department && (
                <Button
                  className="department-primary-button"
                  onClick={() => navigate(`/department-management/update/${department.id}`)}
                >
                  <IconEdit size={19} /> Edit Department
                </Button>
              )}
            </div>
          </header>

          <div className="department-view-body">
            {isLoading ? (
              <div className="department-page-state">
                <Spinner animation="border" /> Loading department...
              </div>
            ) : !department ? (
              <div className="department-error-state">
                <Alert variant="danger">{error || "Department was not found."}</Alert>
                <div className="department-error-actions">
                  <Button
                    className="department-secondary-button"
                    onClick={() => navigate("/department-management/list")}
                  >
                    <IconArrowLeft size={18} /> Back to List
                  </Button>
                  {isValidDepartmentId(id) && (
                    <Button
                      className="department-primary-button"
                      onClick={() => setReloadKey((currentKey) => currentKey + 1)}
                    >
                      <IconRefresh size={18} /> Retry
                    </Button>
                  )}
                </div>
              </div>
            ) : (
              <>
                {error && <Alert variant="danger">{error}</Alert>}
                <section className="department-view-section">
                  <h2>Department Overview</h2>
                  <div className="department-overview">
                    <div className="department-overview-column">
                      <div className="department-info"><IconId size={21} /><span>ID</span><div>{department.id}</div></div>
                      <div className="department-info"><IconBuildingSkyscraper size={21} /><span>Department Name</span><div>{department.departmentName || "-"}</div></div>
                      <div className="department-info"><IconHash size={21} /><span>Department Code</span><div>{department.departmentCode || "-"}</div></div>
                      <div className="department-info"><IconBuilding size={21} /><span>Company ID</span><div>{department.companyId ?? "Not assigned"}</div></div>
                    </div>

                    <div className="department-overview-column">
                      <div className="department-info">
                        <IconCheck size={21} />
                        <span>Status</span>
                        <div>
                          <Badge className={`department-status department-status--${departmentStatus.toLowerCase()}`}>
                            {departmentStatus}
                          </Badge>
                        </div>
                      </div>
                      <div className="department-info"><IconCalendar size={21} /><span>Created At</span><div>{formatDepartmentDate(department.departmentCreatedAt, "long")}</div></div>
                      <div className="department-info"><IconCalendar size={21} /><span>Updated At</span><div>{formatDepartmentDate(department.updatedAt, "long")}</div></div>
                    </div>
                  </div>
                </section>
              </>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

export default ViewDepartment;
