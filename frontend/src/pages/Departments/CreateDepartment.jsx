import { useState } from "react";
import { Button, Form, Nav } from "react-bootstrap";
import { IconHierarchy, IconListDetails, IconPlus, IconSettings, IconUsers } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import { createDepartment } from "../../config/departmentApi/departmentApi.js";
import "../../assets/styles/css/departmentStyles/Departments.css";

function CreateDepartment() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    departmentName: "",
    departmentCode: "",
    parentDepartment: "",
    head: "",
    description: "",
    active: true,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

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
      await createDepartment({
        departmentName: form.departmentName,
        departmentCode: form.departmentCode,
        departmentStatus: form.active ? "Active" : "Inactive",
      });
      navigate("/department-management/list");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to create department.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="department-layout">
      <aside className="department-sidebar">
        <p>Department Management</p>
        <Nav className="department-sidebar-nav">
          <Nav.Link onClick={() => navigate("/department-management/list")}><IconListDetails size={21} /><span>List Department</span></Nav.Link>
          <Nav.Link active onClick={() => navigate("/department-management/create")}><IconPlus size={21} /><span>Create Department</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/list")}><IconHierarchy size={21} /><span>Organization Chart</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/view/2")}><IconUsers size={21} /><span>Team Members</span></Nav.Link>
          <Nav.Link onClick={() => navigate("/department-management/update/2")}><IconSettings size={21} /><span>Department Settings</span></Nav.Link>
        </Nav>
      </aside>

      <section className="department-content">
        <div className="department-panel department-panel--compact">
          <header className="department-panel-header">
            <div><h1>Create Department</h1><p>Add a new department to the organization.</p></div>
          </header>

          <Form className="department-form" onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger m-4 mb-0">{error}</div>}
            <div className="department-form-fields">
              <Form.Group className="department-field">
                <Form.Label>Department Name <span>*</span></Form.Label>
                <div><Form.Control name="departmentName" value={form.departmentName} onChange={handleChange} placeholder="Enter department name" required /></div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Department Code <span>*</span></Form.Label>
                <div><Form.Control name="departmentCode" value={form.departmentCode} onChange={handleChange} placeholder="Enter department code" required /></div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Parent Department</Form.Label>
                <div>
                  <Form.Select name="parentDepartment" value={form.parentDepartment} onChange={handleChange} disabled>
                    <option value="">Not available in Department entity</option>
                  </Form.Select>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Head of Department</Form.Label>
                <div>
                  <Form.Select name="head" value={form.head} onChange={handleChange} disabled>
                    <option value="">Not available in Department entity</option>
                  </Form.Select>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Description</Form.Label>
                <div className="department-description-control">
                  <Form.Control as="textarea" maxLength={255} name="description" value={form.description} onChange={handleChange} placeholder="Not available in Department entity" disabled />
                  <span>{form.description.length} / 255</span>
                </div>
              </Form.Group>

              <Form.Group className="department-field">
                <Form.Label>Status</Form.Label>
                <div><Form.Check type="switch" id="create-department-status" name="active" checked={form.active} onChange={handleChange} label={form.active ? "Active" : "Inactive"} /></div>
              </Form.Group>
            </div>

            <div className="department-form-actions">
              <Button type="button" variant="light" className="department-secondary-button" onClick={() => navigate("/department-management/list")}>Cancel</Button>
              <Button type="submit" className="department-primary-button" disabled={isSubmitting}>{isSubmitting ? "Creating..." : "Create Department"}</Button>
            </div>
          </Form>
        </div>
      </section>
    </div>
  );
}

export default CreateDepartment;
