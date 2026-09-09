import { useEffect, useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconDeviceFloppy, IconRefresh } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import {
  getDepartmentErrorMessage,
  getDepartmentLoadErrorMessage,
  isValidDepartmentId,
  normalizeDepartmentRequest,
  validateDepartmentRequest,
} from "./departmentUtils.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

const initialForm = {
  departmentName: "",
  departmentCode: "",
  active: true,
};

function UpdateDepartment() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form, setForm] = useState(initialForm);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadFailed, setLoadFailed] = useState(false);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const requestController = new AbortController();

    async function loadDepartment() {
      if (!isValidDepartmentId(id)) {
        setError("The department ID is invalid. Please choose a department from the list.");
        setLoadFailed(true);
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setLoadFailed(false);
        setError("");
        const response = await departmentApi.getDepartmentById(
          id,
          requestController.signal
        );

        if (requestController.signal.aborted) {
          return;
        }

        const department = response.data?.data ?? response.data;
        if (!department) {
          setLoadFailed(true);
          setError("Department was not found.");
          return;
        }

        setForm({
          departmentName: department.departmentName || "",
          departmentCode: department.departmentCode || "",
          active: department.departmentStatus?.toLowerCase() === "active",
        });
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load department for update:", requestError);
        setLoadFailed(true);
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

  function getBackPath() {
    return isValidDepartmentId(id)
      ? `/department-management/view/${id}`
      : "/department-management/list";
  }

  function handleChange(event) {
    const { name, type, checked, value } = event.target;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: type === "checkbox"
        ? checked
        : name === "departmentCode"
          ? value.toUpperCase().replace(/[^A-Z0-9_]/g, "")
          : value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const request = normalizeDepartmentRequest(form);
    const validationError = validateDepartmentRequest(request);

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setIsSubmitting(true);
      setError("");
      await departmentApi.updateDepartment(id, request);
      navigate(`/department-management/view/${id}`);
    } catch (requestError) {
      setError(getDepartmentErrorMessage(
        requestError,
        "Unable to update department."
      ));
    } finally {
      setIsSubmitting(false);
    }
  }

  const backAction = (
    <Button
      className="department-secondary-button"
      onClick={() => navigate(getBackPath())}
      disabled={isSubmitting}
    >
      <IconArrowLeft size={18} /> Back
    </Button>
  );

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel department-panel--compact">
          <header className="department-panel-header">
            <div>
              <h1>Update Department</h1>
              <p>Modify department details and settings.</p>
            </div>
            <div className="department-header-actions">{backAction}</div>
          </header>

          {isLoading ? (
            <div className="department-page-state">
              <Spinner animation="border" /> Loading department...
            </div>
          ) : loadFailed ? (
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
            <Form
              className="department-form department-form--update"
              onSubmit={handleSubmit}
              noValidate
            >
              {error && (
                <Alert variant="danger" className="department-form-alert">
                  {error}
                </Alert>
              )}

              <div className="department-form-fields">
                <Form.Group className="department-field" controlId="update-department-name">
                  <Form.Label>Department Name <span>*</span></Form.Label>
                  <div>
                    <Form.Control
                      name="departmentName"
                      value={form.departmentName}
                      onChange={handleChange}
                      maxLength={100}
                      required
                    />
                    <Form.Text>Use a clear display name of up to 100 characters.</Form.Text>
                  </div>
                </Form.Group>

                <Form.Group className="department-field" controlId="update-department-code">
                  <Form.Label>Department Code <span>*</span></Form.Label>
                  <div>
                    <Form.Control
                      name="departmentCode"
                      value={form.departmentCode}
                      onChange={handleChange}
                      maxLength={50}
                      required
                    />
                    <Form.Text>Starts with a letter; use uppercase letters, numbers, and underscores.</Form.Text>
                  </div>
                </Form.Group>

                <Form.Group className="department-field" controlId="update-department-status">
                  <Form.Label>Status</Form.Label>
                  <div>
                    <Form.Check
                      type="switch"
                      name="active"
                      checked={form.active}
                      onChange={handleChange}
                      label={form.active ? "Active" : "Inactive"}
                    />
                    <Form.Text>Inactive departments remain stored but are unavailable for active selections.</Form.Text>
                  </div>
                </Form.Group>
              </div>

              <div className="department-form-actions department-form-actions--outside">
                <Button
                  type="button"
                  className="department-secondary-button"
                  onClick={() => navigate(getBackPath())}
                  disabled={isSubmitting}
                >
                  <IconArrowLeft size={18} /> Cancel
                </Button>
                <Button
                  type="submit"
                  className="department-primary-button"
                  disabled={isSubmitting}
                >
                  {isSubmitting
                    ? <Spinner animation="border" size="sm" />
                    : <IconDeviceFloppy size={18} />}
                  {isSubmitting ? "Saving..." : "Save Changes"}
                </Button>
              </div>
            </Form>
          )}
        </div>
      </section>
    </div>
  );
}

export default UpdateDepartment;
