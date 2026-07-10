import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Container, Spinner, Stack } from "react-bootstrap";
import "../../assets/styles/css/permissionStyles/ViewPermissionPage.css";
import { deletePermission, viewPermission } from "../../config/axiosConfig.js";
import { ViewPermissionInfo } from "./PermissionComponents.jsx";

function ViewPermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("view");
  const [permission, setPermission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
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
        const response = await viewPermission(permissionId);
        const payload = response.data?.data ?? response.data;

        if (isActive) {
          setPermission(payload || null);
        }
      } catch (requestError) {
        console.error("Unable to load permission:", requestError);

        if (isActive) {
          setPermission(null);
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
    if (!permission?.id || !window.confirm("Delete this permission?")) {
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

  return (
    <Container fluid as="main" className="view-page">
      <Card className="view-card">
        <Card.Header className="view-header">
          <div className="view-title-row">
            <Button
              type="button"
              variant="light"
              className="view-back-button"
              onClick={() => navigate("/permission/list")}
              aria-label="Back to permissions"
            >
              {"<"}
            </Button>

            <div>
              <Card.Title as="h1">View Permission</Card.Title>
              <Card.Text>Review the permission information saved in the system.</Card.Text>
            </div>
          </div>

          {permission && (
            <Stack direction="horizontal" className="view-actions">
              <Button
                type="button"
                className="view-primary-button"
                onClick={() => navigate(`/permission/list?edit=${permission.id}`)}
              >
                Edit Permission
              </Button>
              <Button
                type="button"
                variant="light"
                className="view-delete-button"
                disabled={deleting}
                onClick={handleDelete}
              >
                {deleting ? "Deleting..." : "Delete"}
              </Button>
            </Stack>
          )}
        </Card.Header>

        {loading ? (
          <div className="view-state">
            <Spinner animation="border" role="status" />
            <span>Loading permission...</span>
          </div>
        ) : error ? (
          <Alert variant="danger" className="m-4">{error}</Alert>
        ) : !permission ? (
          <div className="view-state">Permission was not found.</div>
        ) : (
          <Card as="section" className="view-section">
            <Card.Title as="h2">Permission Information</Card.Title>

            <div className="view-overview-grid">
              <div className="view-shield">PERM</div>

              <div className="view-info-column">
                <ViewPermissionInfo label="Permission Name" value={permission.permissionName} />
                <ViewPermissionInfo label="Permission Code" value={permission.permissionCode} />
              </div>

              <div className="view-info-column">
                <ViewPermissionInfo label="Permission Module" value={permission.permissionModule} />
                <ViewPermissionInfo label="Project" value={permission.projectName} />
              </div>
            </div>
          </Card>
        )}
      </Card>
    </Container>
  );
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to load permission. Please try again later.";
}

export default ViewPermissionPage;
