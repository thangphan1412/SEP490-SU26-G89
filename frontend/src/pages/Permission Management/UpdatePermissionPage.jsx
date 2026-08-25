import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconClock, IconDeviceFloppy, IconInfoCircle } from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/UpdatePermissionPage.css";
import {
  listPermissionActions,
  listPermissionProjects,
  updatePermission,
  viewPermission,
} from "../../services/permissionService/permissionApi.js";
import PermissionFormField from "../../components/permissionComponents/PermissionFormField.jsx";
import PermissionPage from "../../components/permissionComponents/PermissionPage.jsx";
import {
  permissionWorkScopeOptions,
} from "../../components/permissionComponents/permissionModuleOptions.js";
import {
  formatPermissionDate,
  formatPermissionProjectName,
  getPermissionErrorMessage,
} from "./permissionUtils.js";

const initialPermission = {
  permissionName: "",
  permissionCode: "",
  permissionDescription: "",
  projectId: "",
  status: true,
  allowedActions: [],
  workScope: "FULL",
  createdAt: null,
};

// Hiển thị biểu mẫu tải và cập nhật một quyền hiện có.
function UpdatePermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const permissionId = searchParams.get("edit");
  const returnProjectId = searchParams.get("returnProjectId");
  const [permission, setPermission] = useState(initialPermission);
  const [projects, setProjects] = useState([]);
  const [actionOptions, setActionOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  // Tải quyền, dự án và action mỗi khi permission id thay đổi.
  useEffect(function () {
    const requestController = new AbortController();

    // Tải song song dữ liệu quyền và các tùy chọn cấu hình.
    async function loadPage() {
      // Dừng tải và báo lỗi khi route không có permission id.
      if (!permissionId) {
        setError("Permission id is missing. Please choose a permission from the list.");
        setLoadFailed(true);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");
        setLoadFailed(false);
        const [permissionPayload, projectPayload, actionPayload] = await Promise.all([
          viewPermission(permissionId, requestController.signal),
          listPermissionProjects(requestController.signal),
          listPermissionActions(requestController.signal),
        ]);

        // Bỏ qua response khi component đã hủy request.
        if (requestController.signal.aborted) {
          return;
        }

        setPermission({
          permissionName: permissionPayload?.permissionName || "",
          permissionCode: permissionPayload?.permissionCode || "",
          permissionDescription: permissionPayload?.permissionDescription || "",
          projectId: permissionPayload?.projectId ? String(permissionPayload.projectId) : "",
          status: permissionPayload?.status ?? true,
          allowedActions: Array.isArray(permissionPayload?.allowedActions)
            ? permissionPayload.allowedActions
            : [],
          workScope: permissionPayload?.workScope === "OWN" ? "OWN" : "FULL",
          createdAt: permissionPayload?.createdAt || null,
        });
        const availableProjects = Array.isArray(projectPayload)
          ? projectPayload.filter((project) => project.canManage === true)
          : [];

        setProjects(availableProjects);
        setActionOptions(Array.isArray(actionPayload) ? actionPayload : []);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load permission for update:", requestError);
        setError(getPermissionErrorMessage(
          requestError,
          "Unable to load permission. Please try again later."
        ));
        setLoadFailed(true);
      } finally {
        if (!requestController.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadPage();

    return function () {
      requestController.abort();
    };
  }, [permissionId]);

  // Đồng bộ trường thông tin quyền vừa thay đổi vào state.
  function handleChange(event) {
    const { name, value } = event.target;
    setPermission((currentPermission) => ({
      ...currentPermission,
      [name]: value,
    }));
  }

  // Thêm hoặc loại bỏ một action trong cấu hình quyền hiện tại.
  function toggleAllowedAction(action) {
    setPermission(function (currentPermission) {
      const currentActions = currentPermission.allowedActions;
      let allowedActions;

      if (currentActions.includes(action)) {
        allowedActions = currentActions.filter(function (currentAction) {
          return currentAction !== action;
        });
      } else {
        allowedActions = [...currentActions, action];
      }

      return {
        ...currentPermission,
        allowedActions,
      };
    });
  }

  // Cập nhật work scope được chọn cho quyền.
  function handleWorkScopeChange(event) {
    const workScope = event.target.value;

    setPermission(function (currentPermission) {
      return {
        ...currentPermission,
        workScope,
      };
    });
  }

  // Chuẩn hóa biểu mẫu rồi gửi yêu cầu cập nhật quyền.
  async function handleSubmit(event) {
    event.preventDefault();

    // Không gửi request khi route thiếu permission id.
    if (!permissionId) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      const updatedPermission = await updatePermission(permissionId, {
        permissionName: permission.permissionName.trim(),
        permissionCode: permission.permissionCode.trim(),
        permissionDescription: permission.permissionDescription.trim(),
        projectId: permission.projectId,
        status: permission.status,
        allowedActions: permission.allowedActions,
        workScope: permission.workScope,
      });
      navigate(getReturnPath(updatedPermission?.id || permissionId));
    } catch (requestError) {
      console.error("Unable to update permission:", requestError);
      setError(getPermissionErrorMessage(
        requestError,
        "Unable to update permission. Please try again later."
      ));
    } finally {
      setSaving(false);
    }
  }

  // Xác định đường dẫn quay lại dự án nguồn hoặc chi tiết quyền.
  function getReturnPath(currentPermissionId = permissionId) {
    if (returnProjectId) {
      return `/project-management/view?id=${encodeURIComponent(returnProjectId)}`;
    }

    if (currentPermissionId) {
      return `/permission/list?view=${encodeURIComponent(currentPermissionId)}`;
    }

    return "/permission/list";
  }

  // Điều hướng người dùng trở lại màn hình nguồn.
  function goBack() {
    navigate(getReturnPath());
  }
  const backAction = (
    <Button className="permission-secondary-button" onClick={goBack}>
      <IconArrowLeft size={18} /> Back
    </Button>
  );

  if (loading) {
    return (
      <PermissionPage title="Update Permission" description="Loading the permission information..." action={backAction}>
        <div className="permission-page-state"><Spinner animation="border" /> Loading permission...</div>
      </PermissionPage>
    );
  }

  if (loadFailed) {
    return (
      <PermissionPage title="Update Permission" description="The permission could not be opened." action={backAction}>
        <Alert variant="danger" className="permission-update-load-error">{error}</Alert>
      </PermissionPage>
    );
  }

  const noProjects = projects.length === 0;
  const noActions = actionOptions.length === 0;
  const noOptions = noProjects || noActions;

  return (
    <PermissionPage
      title="Update Permission"
      description="Review the current values and save only the changes you need."
      action={backAction}
    >
      <Form className="permission-update-form" onSubmit={handleSubmit}>
        {error && <Alert variant="danger">{error}</Alert>}
        {noProjects && (
          <Alert variant="warning">At least one project is required to save this permission.</Alert>
        )}
        {noActions && (
          <Alert variant="warning">No active permission actions are available in the database.</Alert>
        )}

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">1</span>
            <div>
              <h2>Permission information</h2>
              <p>Keep the name, code, and description clear for other users.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="update-permission-name" label="Permission Name" required>
                <Form.Control className="permission-input" name="permissionName" value={permission.permissionName} onChange={handleChange} maxLength={50} required />
              </PermissionFormField>
            </Col>
            <Col md={6}>
              <PermissionFormField controlId="update-permission-code" label="Permission Code" required hint="The code must remain unique.">
                <Form.Control className="permission-input" name="permissionCode" value={permission.permissionCode} onChange={handleChange} maxLength={50} required />
              </PermissionFormField>
            </Col>
            <Col xs={12}>
              <PermissionFormField controlId="update-permission-description" label="Description">
                <div className="permission-description-box">
                  <Form.Control
                    as="textarea"
                    className="permission-textarea"
                    name="permissionDescription"
                    value={permission.permissionDescription}
                    onChange={handleChange}
                    placeholder="Explain what this permission allows a user to do..."
                    maxLength={255}
                  />
                  <span>{permission.permissionDescription.length} / 255</span>
                </div>
              </PermissionFormField>
            </Col>
          </Row>
        </Card>

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">2</span>
            <div>
              <h2>Assignment and status</h2>
              <p>Change the project or availability when business rules change.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col xs={12}>
              <PermissionFormField controlId="update-permission-project" label="Project" required>
                <Form.Select className="permission-input" name="projectId" value={permission.projectId} onChange={handleChange} disabled={projects.length === 0} required>
                  <option value="" disabled>Select project</option>
                  {projects.map((project) => (
                    <option key={project.id} value={project.id}>
                      {formatPermissionProjectName(project)}
                    </option>
                  ))}
                </Form.Select>
              </PermissionFormField>
            </Col>
            <Col xs={12}>
              <div className="permission-status-control">
                <div>
                  <strong>Permission status</strong>
                  <span>Turn this off when the permission should no longer be available.</span>
                </div>
                <Form.Check
                  type="switch"
                  id="update-permission-status"
                  label={permission.status ? "Active" : "Inactive"}
                  checked={permission.status}
                  onChange={(event) => setPermission((current) => ({ ...current, status: event.target.checked }))}
                />
              </div>
            </Col>
          </Row>
        </Card>

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">3</span>
            <div>
              <h2>Access configuration</h2>
              <p>Choose what this permission can do and which project work it can see.</p>
            </div>
          </div>

          <div className="permission-update-configuration-section">
            <div className="permission-update-configuration-heading">
              <h3>Allowed actions</h3>
              <p>Select all actions that users with this permission can perform.</p>
            </div>

            <div className="permission-update-action-grid">
              {actionOptions.map((option) => (
                <Form.Check
                  key={option.id || option.actionCode}
                  id={`update-permission-action-${option.actionCode.toLowerCase()}`}
                  type="checkbox"
                  label={option.actionName || option.actionCode}
                  title={option.description || ""}
                  checked={permission.allowedActions.includes(option.actionCode)}
                  onChange={() => toggleAllowedAction(option.actionCode)}
                />
              ))}
            </div>
          </div>

          <div className="permission-update-configuration-section permission-update-work-scope">
            <div className="permission-update-configuration-heading">
              <h3>Work visibility</h3>
              <p>Choose one visibility level for tasks and deliverables.</p>
            </div>

            <div className="permission-update-scope-options">
              {permissionWorkScopeOptions.map((option) => (
                <Form.Check
                  key={option.value}
                  id={`update-permission-scope-${option.value.toLowerCase()}`}
                  type="radio"
                  name="workScope"
                  value={option.value}
                  label={option.label}
                  checked={permission.workScope === option.value}
                  onChange={handleWorkScopeChange}
                />
              ))}
            </div>
          </div>
        </Card>

        <div className="permission-update-audit">
          <IconInfoCircle size={19} />
          <div>
            <strong>Audit information</strong>
            <span><IconClock size={16} /> Created: {formatPermissionDate(permission.createdAt)}</span>
          </div>
        </div>

        <div className="permission-form-actions">
          <Button className="permission-secondary-button" onClick={goBack} disabled={saving}>Cancel</Button>
          <Button type="submit" className="permission-primary-button" disabled={saving || noOptions}>
            {saving ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
            {saving ? "Saving..." : "Save Changes"}
          </Button>
        </div>
      </Form>
    </PermissionPage>
  );
}

export default UpdatePermissionPage;
