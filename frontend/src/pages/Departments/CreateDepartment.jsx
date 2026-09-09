import { useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconDeviceFloppy } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import {
  getDepartmentErrorMessage,
  normalizeDepartmentRequest,
  validateDepartmentRequest,
} from "./departmentUtils.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

const initialForm = {
  departmentName: "",
  departmentCode: "",
  active: true,
};

function CreateDepartment() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

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
      const response = await departmentApi.createDepartment(request);
      const createdDepartment = response.data?.data ?? response.data;

      navigate(createdDepartment?.id
        ? `/department-management/view/${createdDepartment.id}`
        : "/department-management/list");
    } catch (requestError) {
      setError(getDepartmentErrorMessage(
        requestError,
        "Unable to create department."
      ));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel department-panel--compact">
          <header className="department-panel-header">
            <div>
              <h1>Create Department</h1>
              <p>Add a new department to the organization.</p>
            </div>
            <div className="department-header-actions">
              <Button
                className="department-secondary-button"
                onClick={() => navigate("/department-management/list")}
                disabled={isSubmitting}
              >
                <IconArrowLeft size={18} /> Back
              </Button>
            </div>
          </header>

          <Form className="department-form" onSubmit={handleSubmit} noValidate>
            {error && (
              <Alert variant="danger" className="department-form-alert">
                {error}
              </Alert>
            )}

            <div className="department-form-fields">
              <Form.Group className="department-field" controlId="create-department-name">
                <Form.Label>Department Name <span>*</span></Form.Label>
                <div>
                  <Form.Control
                    name="departmentName"
                    value={form.departmentName}
                    onChange={handleChange}
                    placeholder="Enter department name"
                    maxLength={100}
                    required
                  />
                  <Form.Text>Use a clear display name of up to 100 characters.</Form.Text>
                </div>
              </Form.Group>

              <Form.Group className="department-field" controlId="create-department-code">
                <Form.Label>Department Code <span>*</span></Form.Label>
                <div>
                  <Form.Control
                    name="departmentCode"
                    value={form.departmentCode}
                    onChange={handleChange}
                    placeholder="CONTRACT_REVIEW"
                    maxLength={50}
                    required
                  />
                  <Form.Text>Starts with a letter; use uppercase letters, numbers, and underscores.</Form.Text>
                </div>
              </Form.Group>

              <Form.Group className="department-field" controlId="create-department-status">
                <Form.Label>Status</Form.Label>
                <div>
                  <Form.Check
                    type="switch"
                    name="active"
                    checked={form.active}
                    onChange={handleChange}
                    label={form.active ? "Active" : "Inactive"}
                  />
                </div>
              </Form.Group>
            </div>

            <div className="department-form-actions">
              <Button
                type="button"
                className="department-secondary-button"
                onClick={() => navigate("/department-management/list")}
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
                {isSubmitting ? "Creating..." : "Create Department"}
              </Button>
            </div>
          </Form>
        </div>
      </section>
    </div>
  );
}

export default CreateDepartment;
