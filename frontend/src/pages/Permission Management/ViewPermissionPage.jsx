import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, ProgressBar, Spinner, Stack, Table } from "react-bootstrap";
import {
  IconArrowLeft,
  IconChevronRight,
  IconClock,
  IconEdit,
  IconFileDescription,
  IconFolder,
  IconKey,
  IconShieldCheck,
  IconTimelineEvent,
  IconTrash,
  IconUserShield,
} from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/ViewPermissionPage.css";
import { deletePermission, viewPermission } from "../../config/permissionApi/permissionApi.js";
import PermissionPage from "../../components/permissionComponents/PermissionPage.jsx";
import PermissionStatusBadge from "../../components/permissionComponents/PermissionStatusBadge.jsx";
import ViewPermissionInfo from "../../components/permissionComponents/ViewPermissionInfo.jsx";
import { listProjectPhases } from "../../config/phaseApi/phaseApi.js";
import ViewPhase from "../Phase Management/ViewPhase.jsx";
import { formatPermissionDate } from "./permissionUtils.js";

function ViewPermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("view");
  const phaseId = searchParams.get("phase");
  const [permission, setPermission] = useState(null);
  const [phases, setPhases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [phaseError, setPhaseError] = useState("");
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let isActive = true;

    const loadPermission = async () => {
      if (!permissionId) {
        if (isActive) {
          setError("Permission id is missing. Please choose a permission from the list.");
          setLoading(false);
        }
        return;
      }

      try {
        setLoading(true);
        setError("");
        setPhaseError("");
        const response = await viewPermission(permissionId);
        const payload = response.data?.data ?? response.data;
        let projectPhases = [];
        let projectPhaseError = "";

        if (payload?.projectId) {
          try {
            const phaseResponse = await listProjectPhases(payload.projectId);
            const phasePayload = phaseResponse.data?.data ?? phaseResponse.data;
            projectPhases = Array.isArray(phasePayload) ? phasePayload : [];
          } catch (requestError) {
            console.error("Unable to load project phases:", requestError);
            projectPhaseError = "Unable to load the phases of this project.";
          }
        }

        if (isActive) {
          setPermission(payload || null);
          setPhases(projectPhases);
          setPhaseError(projectPhaseError);
        }
      } catch (requestError) {
        console.error("Unable to load permission:", requestError);
        if (isActive) {
          setPermission(null);
          setPhases([]);
          setError(getErrorMessage(requestError));
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    };

    loadPermission();

    return () => {
      isActive = false;
    };
  }, [permissionId]);

  const handleDelete = async () => {
    if (!permission?.id || !window.confirm(`Delete permission "${permission.permissionName}"?`)) {
      return;
    }

    try {
      setDeleting(true);
      setError("");
      await deletePermission(permission.id);
      navigate("/permission/list");
    } catch (requestError) {
      console.error("Unable to delete permission:", requestError);
      setError(getErrorMessage(requestError));
    } finally {
      setDeleting(false);
    }
  };

  if (phaseId) {
    return (
      <ViewPhase
        phaseId={phaseId}
        onBack={() => navigate(permissionId ? `/permission/list?view=${permissionId}` : "/permission/list")}
      />
    );
  }

  const pageActions = (
    <Stack direction="horizontal" className="permission-view-actions">
      <Button className="permission-secondary-button" onClick={() => navigate("/permission/list")}>
        <IconArrowLeft size={18} /> Back
      </Button>
      {permission && (
        <>
          <Button className="permission-primary-button" onClick={() => navigate(`/permission/list?edit=${permission.id}`)}>
            <IconEdit size={18} /> Edit
          </Button>
          <Button className="permission-danger-button" disabled={deleting} onClick={handleDelete}>
            <IconTrash size={18} /> {deleting ? "Deleting..." : "Delete"}
          </Button>
        </>
      )}
    </Stack>
  );

  return (
    <PermissionPage
      title="Permission Details"
      description="Review the access rule, assignment, status, and audit information."
      action={pageActions}
    >
      {loading ? (
        <div className="permission-page-state"><Spinner animation="border" /> Loading permission...</div>
      ) : !permission ? (
        error
          ? <Alert variant="danger" className="permission-view-message">{error}</Alert>
          : <div className="permission-page-state">Permission was not found.</div>
      ) : (
        <div className="permission-view-content">
          {error && <Alert variant="danger">{error}</Alert>}

          <Card as="section" className="permission-view-hero">
            <span className="permission-view-shield"><IconShieldCheck size={38} stroke={1.7} /></span>
            <div className="permission-view-identity">
              <div className="permission-view-title-line">
                <h2>{permission.permissionName || "Unnamed permission"}</h2>
                <PermissionStatusBadge status={permission.status} />
              </div>
              <span className="permission-view-code"><IconKey size={15} /> {permission.permissionCode || "No code"}</span>
              <p>{permission.permissionDescription || "No description has been added for this permission."}</p>
            </div>
            <span className="permission-view-id">ID #{permission.id}</span>
          </Card>

          <div className="permission-view-grid">
            <Card as="section" className="permission-view-card permission-view-card--wide">
              <div className="permission-view-card-title">
                <span><IconFolder size={20} /></span>
                <div><h3>Access assignment</h3><p>Where this permission applies and which role receives it.</p></div>
              </div>
              <div className="permission-view-info-grid">
                <ViewPermissionInfo label="Project" value={formatProjectValue(permission)} />
                <ViewPermissionInfo label="Role" value={permission.roleName || "Unassigned"} />
                <ViewPermissionInfo label="Module" value={permission.permissionModule} />
                <div className="permission-info-item">
                  <p className="permission-info-label">Status</p>
                  <PermissionStatusBadge status={permission.status} />
                </div>
              </div>
            </Card>

            <Card as="section" className="permission-view-card">
              <div className="permission-view-card-title">
                <span><IconClock size={20} /></span>
                <div><h3>Audit information</h3><p>Creation time recorded automatically by the server.</p></div>
              </div>
              <div className="permission-view-audit-list">
                <ViewPermissionInfo label="Created at" value={formatPermissionDate(permission.createdAt)} />
              </div>
            </Card>

            <Card as="section" className="permission-view-card permission-project-phases-card">
              <div className="permission-view-card-title">
                <span><IconTimelineEvent size={20} /></span>
                <div><h3>Project Phases</h3><p>Select a phase to view its tasks, deliverables, and contracts.</p></div>
                <span className="permission-phase-count">{phases.length}</span>
              </div>

              {phaseError ? (
                <Alert variant="warning" className="permission-phase-alert">{phaseError}</Alert>
              ) : (
                <div className="permission-phase-table-wrap">
                  <Table hover responsive className="permission-phase-table mb-0">
                    <thead>
                      <tr><th>Phase</th><th>Date range</th><th>Progress</th><th>Status</th><th aria-label="Open phase" /></tr>
                    </thead>
                    <tbody>
                      {phases.length === 0 ? (
                        <tr><td colSpan={5} className="permission-phase-empty">This project does not have any phases.</td></tr>
                      ) : phases.map((phase) => {
                        const progress = normalizeProgress(phase.progress);
                        return (
                          <tr
                            key={phase.id}
                            className="permission-phase-row"
                            role="button"
                            tabIndex={0}
                            onClick={() => navigate(`/permission/list?view=${permission.id}&phase=${phase.id}`)}
                            onKeyDown={(event) => {
                              if (event.key === "Enter" || event.key === " ") {
                                event.preventDefault();
                                navigate(`/permission/list?view=${permission.id}&phase=${phase.id}`);
                              }
                            }}
                          >
                            <td>
                              <strong>{phase.title || `Phase #${phase.id}`}</strong>
                              <small>{phase.description || "No description"}</small>
                            </td>
                            <td>{formatDateRange(phase.startDate, phase.endDate)}</td>
                            <td className="permission-phase-progress">
                              <span>{progress}%</span>
                              <ProgressBar now={progress} />
                            </td>
                            <td><span className="permission-phase-status">{phase.status || "Not set"}</span></td>
                            <td className="permission-phase-open"><IconChevronRight size={18} /></td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </Table>
                </div>
              )}
            </Card>

            <Card as="section" className="permission-view-card permission-view-card--wide">
              <div className="permission-view-card-title">
                <span><IconFileDescription size={20} /></span>
                <div><h3>Description</h3><p>A plain-language explanation of the access rule.</p></div>
              </div>
              <p className="permission-view-description">
                {permission.permissionDescription || "No description has been added."}
              </p>
            </Card>

            <Card as="section" className="permission-view-card">
              <div className="permission-view-card-title">
                <span><IconUserShield size={20} /></span>
                <div><h3>Technical values</h3><p>Values used by the application.</p></div>
              </div>
              <div className="permission-view-audit-list">
                <ViewPermissionInfo label="Permission code" value={permission.permissionCode} />
                <ViewPermissionInfo label="Module" value={permission.permissionModule} />
              </div>
            </Card>
          </div>
        </div>
      )}
    </PermissionPage>
  );
}

function formatProjectValue(permission) {
  if (!permission.projectName && !permission.projectCode) {
    return "Unassigned";
  }

  return [permission.projectCode, permission.projectName].filter(Boolean).join(" - ");
}

function normalizeProgress(value) {
  const numberValue = Number(value);
  if (!Number.isFinite(numberValue)) {
    return 0;
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)));
}

function formatDate(value) {
  if (!value) {
    return "-";
  }
  const parts = String(value).split("-");
  return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : value;
}

function formatDateRange(startDate, endDate) {
  return `${formatDate(startDate)} - ${formatDate(endDate)}`;
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to load permission. Please try again later.";
}

export default ViewPermissionPage;
