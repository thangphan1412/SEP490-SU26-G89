import { useEffect, useState } from "react";
import { Button, Form } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import departmentApi from "../../services/departmentService/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function UpdateDepartment() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form, setForm] = useState({
    departmentName: "",
    departmentCode: "",
    departmentCreateAt: "",
    parentDepartment: "",
    head: "",
    description: "",
    active: true,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDepartment = async () => {
      try {
        const response = await departmentApi.getDepartmentById(id);
        const department = response.data?.data;
        setForm((current) => ({
          ...current,
          departmentName: department?.departmentName || "",
          departmentCode: department?.departmentCode || "",
          departmentCreateAt: department?.departmentCreateAt || "",
          active: department?.departmentStatus?.toLowerCase() !== "inactive",
        }));
      } catch (requestError) {
        setError(requestError.response?.data?.message || "Unable to load department.");
      } finally {
        setIsLoading(false);
      }
    };

    loadDepartment();
  }, [id]);

  const handleChange = ({ target }) => {
    setForm((current) => ({
      ...current,
      [target.name]: target.type === "checkbox" ? target.checked : target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await departmentApi.updateDepartment(id, {
        departmentName: form.departmentName,
        departmentCode: form.departmentCode,
        departmentCreateAt: form.departmentCreateAt || null,
        departmentStatus: form.active ? "Active" : "Inactive",
      });
      navigate(`/department-management/view/${id}`);
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to update department.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="department-layout">
      <section className="department-content">
        <div className="department-panel department-panel--compact">
          <header className="department-panel-header">
            <div><h1>Update Department</h1><p>Modify department details and settings.</p></div>
          </header>

          <Form className="department-form department-form--update" onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger m-4 mb-0">{error}</div>}
            <div className="department-form-fields">
              <Form.Group className="department-field">
                <Form.Label>Department Name <span>*</span></Form.Label>
                <div><Form.Control name="departmentName" value={form.departmentName} onChange={handleChange} required /></div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Department Code <span>*</span></Form.Label>
                <div><Form.Control name="departmentCode" value={form.departmentCode} onChange={handleChange} required /><Form.Text>Unique code used to identify the department.</Form.Text></div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Parent Department</Form.Label>
                <div>
                  <Form.Select name="parentDepartment" value={form.parentDepartment} onChange={handleChange} disabled>
                    <option value="">Not available in Department entity</option>
                  </Form.Select>
                  <Form.Text>Parent department is not currently stored by the backend.</Form.Text>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Head of Department</Form.Label>
                <div>
                  <Form.Select name="head" value={form.head} onChange={handleChange} disabled>
                    <option value="">Not available in Department entity</option>
                  </Form.Select>
                  <Form.Text>Head of department is not currently stored by the backend.</Form.Text>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Description</Form.Label>
                <div className="department-description-control">
                  <Form.Control as="textarea" maxLength={255} name="description" value={form.description} onChange={handleChange} disabled />
                  <span>{form.description.length} / 255</span>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Status</Form.Label>
                <div>
                  <Form.Check type="switch" id="update-department-status" name="active" checked={form.active} onChange={handleChange} label={form.active ? "Active" : "Inactive"} />
                  <Form.Text>Inactive departments will be hidden from active selections.</Form.Text>
                </div>
              </Form.Group>
            </div>

            <div className="department-form-actions department-form-actions--outside">
              <Button type="button" variant="light" className="department-secondary-button" onClick={() => navigate(`/department-management/view/${id}`)}>Cancel</Button>
              <Button type="submit" className="department-primary-button" disabled={isLoading || isSubmitting}>{isSubmitting ? "Updating..." : "Update Department"}</Button>
            </div>
          </Form>
        </div>
      </section>
    </div>
  );
}

export default UpdateDepartment;
