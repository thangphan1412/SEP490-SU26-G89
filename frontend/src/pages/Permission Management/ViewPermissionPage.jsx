import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Spinner, Stack } from "react-bootstrap";
import {
  IconArrowLeft,
  IconClock,
  IconEdit,
  IconFileDescription,
  IconFolder,
  IconKey,
  IconShieldCheck,
  IconTrash,
  IconUserShield,
} from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/ViewPermissionPage.css";
import { deletePermission, viewPermission } from "../../services/permissionService/permissionApi.js";
import PermissionPage from "../../components/permissionComponents/PermissionPage.jsx";
import PermissionStatusBadge from "../../components/permissionComponents/PermissionStatusBadge.jsx";
import ViewPermissionInfo from "../../components/permissionComponents/ViewPermissionInfo.jsx";
import { formatPermissionModule } from "../../components/permissionComponents/permissionModuleOptions.js";
import {
  formatPermissionDate,
  formatPermissionProjectValue,
  getPermissionErrorMessage,
} from "./permissionUtils.js";

function ViewPermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("view");
  const returnProjectId = searchParams.get("returnProjectId");
  const [permission, setPermission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deleting, setDeleting] = useState(false);

  useEffect(function () {
    let isActive = true;
    const requestController = new AbortController();

    async function loadPermission() {
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
        const payload = await viewPermission(permissionId, requestController.signal);

        if (isActive) {
          setPermission(payload || null);
        }
      } catch (requestError) {
        if (!isActive) {
          return;
        }

        console.error("Unable to load permission:", requestError);
        setPermission(null);
        setError(getPermissionErrorMessage(
          requestError,
          "Unable to load permission. Please try again later."
        ));
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadPermission();

    return function () {
      isActive = false;
      requestController.abort();
    };
  }, [permissionId]);

  function getBackPath() {
    if (returnProjectId) {
      return `/project-management/view?id=${encodeURIComponent(returnProjectId)}`
        + "&openPermissionConfigure=true";
    }

    return "/permission/list";
  }

  function goBack() {
    navigate(getBackPath());
  }

  async function handleDelete() {
    if (!permission?.id || !window.confirm(`Delete permission "${permission.permissionName}"?`)) {
      return;
    }

    try {
      setDeleting(true);
      setError("");
      await deletePermission(permission.id);
      navigate(getBackPath());
    } catch (requestError) {
      console.error("Unable to delete permission:", requestError);
      setError(getPermissionErrorMessage(
        requestError,
        "Unable to delete permission. Please try again later."
      ));
    } finally {
      setDeleting(false);
    }
  }

  const pageActions = (
    <Stack direction="horizontal" className="permission-view-actions">
      <Button className="permission-secondary-button" onClick={goBack}>
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
                <div><h3>Access assignment</h3><p>Where this permission applies.</p></div>
              </div>
              <div className="permission-view-info-grid">
                <ViewPermissionInfo
                  label="Project"
                  value={formatPermissionProjectValue(permission)}
                />
                <ViewPermissionInfo label="Module" value={formatPermissionModule(permission.permissionModule)} />
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
                <ViewPermissionInfo label="Module" value={formatPermissionModule(permission.permissionModule)} />
              </div>
            </Card>
          </div>
        </div>
      )}
    </PermissionPage>
  );
}

export default ViewPermissionPage;
