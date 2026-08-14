import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Col, Form, Row, Spinner } from "react-bootstrap";
import {
  IconArrowLeft,
  IconClock,
  IconDeviceFloppy,
  IconInfoCircle,
} from "@tabler/icons-react";
import "../../assets/styles/css/permissionStyles/CreatePermissionPage.css";
import {
  createPermission,
  listPermissionActions,
  listPermissionProjects,
} from "../../services/permissionService/permissionApi.js";
import PermissionFormField from "../../components/permissionComponents/PermissionFormField.jsx";
import PermissionPage from "../../components/permissionComponents/PermissionPage.jsx";
import {
  permissionWorkScopeOptions,
} from "../../components/permissionComponents/permissionModuleOptions.js";
import {
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
};

// Hiển thị biểu mẫu tạo quyền mới và tải các tùy chọn cấu hình cần thiết.
function CreatePermissionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const requestedProjectId = searchParams.get("projectId") || "";
  const returnProjectId = searchParams.get("returnProjectId") || "";
  const [permission, setPermission] = useState({
    ...initialPermission,
    projectId: requestedProjectId,
  });
  const [projects, setProjects] = useState([]);
  const [actionOptions, setActionOptions] = useState([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  // Lấy danh sách dự án và action khả dụng khi component được mount.
  useEffect(function () {
    const requestController = new AbortController();

    // Tải song song dự án có thể quản lý và permission action catalog.
    async function loadOptions() {
      try {
        const [projectPayload, actionPayload] = await Promise.all([
          listPermissionProjects(requestController.signal),
          listPermissionActions(requestController.signal),
        ]);

        // Bỏ qua response khi component đã hủy request.
        if (requestController.signal.aborted) {
          return;
        }

        const availableProjects = Array.isArray(projectPayload)
          ? projectPayload.filter((project) => project.canManage === true)
          : [];

        setProjects(availableProjects);
        setActionOptions(Array.isArray(actionPayload) ? actionPayload : []);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load permission options:", requestError);
        setError("Unable to load permission options. Please try again later.");
      } finally {
        if (!requestController.signal.aborted) {
          setLoadingOptions(false);
        }
      }
    }

    loadOptions();

    return function () {
      requestController.abort();
    };
  }, []);

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

  // Xác định đường dẫn quay lại danh sách quyền hoặc dự án nguồn.
  function getBackPath() {
    if (returnProjectId) {
      return `/project-management/view?id=${encodeURIComponent(returnProjectId)}`
        + "&openPermissionConfigure=true";
    }

    return "/permission/list";
  }

  // Tạo đường dẫn mở quyền vừa tạo và giữ ngữ cảnh dự án nguồn.
  function getCreatedPermissionPath(permissionId) {
    // Quay về màn hình trước khi backend không trả về id quyền mới.
    if (!permissionId) {
      return getBackPath();
    }

    let path = `/permission/list?view=${encodeURIComponent(permissionId)}`;

    if (returnProjectId) {
      path += `&returnProjectId=${encodeURIComponent(returnProjectId)}`;
    }

    return path;
  }

  // Điều hướng người dùng trở lại màn hình nguồn.
  function goBack() {
    navigate(getBackPath());
  }

  // Chuẩn hóa biểu mẫu rồi gửi yêu cầu tạo quyền.
  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      setSaving(true);
      const createdPermission = await createPermission({
        permissionName: permission.permissionName.trim(),
        permissionCode: permission.permissionCode.trim(),
        permissionDescription: permission.permissionDescription.trim(),
        projectId: permission.projectId,
        status: permission.status,
        allowedActions: permission.allowedActions,
        workScope: permission.workScope,
      });

      navigate(getCreatedPermissionPath(createdPermission?.id));
    } catch (requestError) {
      console.error("Unable to create permission:", requestError);
      setError(getPermissionErrorMessage(
        requestError,
        "Unable to save permission. Please check the information and try again."
      ));
    } finally {
      setSaving(false);
    }
  }

  const backAction = (
    <Button
      type="button"
      className="permission-secondary-button"
      onClick={goBack}
    >
      <IconArrowLeft size={18} />
      {returnProjectId ? "Back to project" : "Back to list"}
    </Button>
  );
  const noProjects = !loadingOptions && projects.length === 0;
  const noActions = !loadingOptions && actionOptions.length === 0;
  const noOptions = noProjects || noActions;

  return (
    <PermissionPage
      title="Create Permission"
      description="Define a clear access rule and assign it to a project."
      action={backAction}
    >
      <Form className="permission-create-form" onSubmit={handleSubmit}>
        {error && <Alert variant="danger">{error}</Alert>}
        {noProjects && (
          <Alert variant="warning">
            At least one project is required before a permission can be created.
          </Alert>
        )}
        {noActions && (
          <Alert variant="warning">
            No active permission actions are available in the database.
          </Alert>
        )}

        <Card as="section" className="permission-form-card">
          <div className="permission-section-heading">
            <span className="permission-section-number">1</span>
            <div>
              <h2>Permission information</h2>
              <p>Use a short name and a unique code that are easy to understand.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col md={6}>
              <PermissionFormField controlId="permission-name" label="Permission Name" required>
                <Form.Control
                  className="permission-input"
                  name="permissionName"
                  value={permission.permissionName}
                  onChange={handleChange}
                  placeholder="Example: View contracts"
                  maxLength={50}
                  required
                />
              </PermissionFormField>
            </Col>

            <Col md={6}>
              <PermissionFormField controlId="permission-code" label="Permission Code" required hint="Use a unique code, for example CONTRACT_VIEW.">
                <Form.Control
                  className="permission-input"
                  name="permissionCode"
                  value={permission.permissionCode}
                  onChange={handleChange}
                  placeholder="CONTRACT_VIEW"
                  maxLength={50}
                  required
                />
              </PermissionFormField>
            </Col>

            <Col xs={12}>
              <PermissionFormField controlId="permission-description" label="Description">
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
              <p>Choose the project where this permission is used.</p>
            </div>
          </div>

          <Row className="g-4">
            <Col xs={12}>
              <PermissionFormField controlId="permission-project" label="Project" required>
                <Form.Select
                  className="permission-input"
                  name="projectId"
                  value={permission.projectId}
                  onChange={handleChange}
                  disabled={loadingOptions || projects.length === 0}
                  required
                >
                  <option value="" disabled>{loadingOptions ? "Loading projects..." : "Select project"}</option>
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
                  <span>Inactive permissions remain stored but are marked as unavailable.</span>
                </div>
                <Form.Check
                  type="switch"
                  id="permission-status"
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

          <div className="permission-create-configuration-section">
            <div className="permission-create-configuration-heading">
              <h3>Allowed actions</h3>
              <p>Select all actions that users with this permission can perform.</p>
            </div>

            <div className="permission-create-action-grid">
              {actionOptions.map((option) => (
                <Form.Check
                  key={option.id || option.actionCode}
                  id={`create-permission-action-${option.actionCode.toLowerCase()}`}
                  type="checkbox"
                  label={option.actionName || option.actionCode}
                  title={option.description || ""}
                  checked={permission.allowedActions.includes(option.actionCode)}
                  onChange={() => toggleAllowedAction(option.actionCode)}
                />
              ))}
            </div>
          </div>

          <div className="permission-create-configuration-section permission-create-work-scope">
            <div className="permission-create-configuration-heading">
              <h3>Work visibility</h3>
              <p>Choose one visibility level for tasks and deliverables.</p>
            </div>

            <div className="permission-create-scope-options">
              {permissionWorkScopeOptions.map((option) => (
                <Form.Check
                  key={option.value}
                  id={`create-permission-scope-${option.value.toLowerCase()}`}
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

        <div className="permission-audit-note">
          <IconInfoCircle size={19} />
          <span><IconClock size={16} /> Creation time is recorded automatically by the server.</span>
        </div>

        <div className="permission-form-actions">
          <Button
            type="button"
            className="permission-secondary-button"
            onClick={goBack}
            disabled={saving}
          >
            Cancel
          </Button>
          <Button type="submit" className="permission-primary-button" disabled={saving || loadingOptions || noOptions}>
            {saving ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
            {saving ? "Creating..." : "Create Permission"}
          </Button>
        </div>
      </Form>
    </PermissionPage>
  );
}

export default CreatePermissionPage;
