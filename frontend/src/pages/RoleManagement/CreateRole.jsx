import { useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconDeviceFloppy } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import roleApi from "../../services/RoleService/roleApi.js";
import "../../assets/styles/css/roleStyles/Roles.css";

function CreateRole() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        roleCode: "",
        roleName: "",
        roleDescription: "",
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState("");

    function handleChange(event) {
        const { name, value } = event.target;
        setForm((currentForm) => ({
            ...currentForm,
            [name]: name === "roleCode"
                ? value.toUpperCase().replace(/[^A-Z0-9_]/g, "")
                : value,
        }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const request = {
            roleCode: form.roleCode.trim().toUpperCase(),
            roleName: form.roleName.trim(),
            roleDescription: form.roleDescription.trim(),
        };

        if (!request.roleCode) {
            setError("Role code is required.");
            return;
        }

        if (!/^[A-Z][A-Z0-9_]{1,49}$/.test(request.roleCode)) {
            setError("Role code must start with a letter and contain 2-50 uppercase letters, numbers, or underscores.");
            return;
        }

        if (!request.roleName) {
            setError("Role name is required.");
            return;
        }

        try {
            setIsSubmitting(true);
            setError("");
            const response = await roleApi.createRole(request);
            const createdRole = response.data?.data ?? response.data;

            navigate(createdRole?.id
                ? `/role-management/view/${createdRole.id}`
                : "/role-management/list");
        } catch (requestError) {
            setError(
                requestError.response?.data?.message
                || requestError.response?.data?.detail
                || "Unable to create role."
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="role-layout">
            <section className="role-content">
                <div className="role-panel role-panel--compact">
                    <header className="role-panel-header">
                        <div>
                            <h1>Create Role</h1>
                            <p>Create a custom or project role. System roles are sourced from users and cannot be created here.</p>
                        </div>
                        <div className="role-header-actions">
                            <Button className="role-secondary-button" onClick={() => navigate("/role-management/list")}>
                                <IconArrowLeft size={18} /> Back
                            </Button>
                        </div>
                    </header>

                    <Form className="role-form" onSubmit={handleSubmit} noValidate>
                        {error && <Alert variant="danger" className="role-form-alert">{error}</Alert>}

                        <div className="role-form-section">
                            <div className="role-form-section-heading">
                                <h2>Role information</h2>
                                    <p>Use a stable custom code and a clear name for project assignments.</p>
                            </div>

                            <div className="role-form-grid">
                                <Form.Group className="role-field" controlId="create-role-code">
                                    <Form.Label>Role Code <span>*</span></Form.Label>
                                    <Form.Control
                                        name="roleCode"
                                        value={form.roleCode}
                                        onChange={handleChange}
                                        placeholder="CONTRACT_REVIEWER"
                                        maxLength={50}
                                        required
                                    />
                                    <Form.Text>Uppercase letters, numbers, and underscores.</Form.Text>
                                </Form.Group>

                                <Form.Group className="role-field" controlId="create-role-name">
                                    <Form.Label>Role Name <span>*</span></Form.Label>
                                    <Form.Control
                                        name="roleName"
                                        value={form.roleName}
                                        onChange={handleChange}
                                        placeholder="Contract Reviewer"
                                        maxLength={100}
                                        required
                                    />
                                    <Form.Text>The display name shown throughout the application.</Form.Text>
                                </Form.Group>

                                <Form.Group className="role-field role-field--wide" controlId="create-role-description">
                                    <Form.Label>Role Description</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        name="roleDescription"
                                        value={form.roleDescription}
                                        onChange={handleChange}
                                        placeholder="Describe the responsibility and intended access for this role..."
                                        maxLength={255}
                                    />
                                    <Form.Text>{form.roleDescription.length} / 255 characters</Form.Text>
                                </Form.Group>
                            </div>
                        </div>

                        <div className="role-form-actions">
                            <Button
                                type="button"
                                className="role-secondary-button"
                                onClick={() => navigate("/role-management/list")}
                                disabled={isSubmitting}
                            >
                                <IconArrowLeft size={18} /> Cancel
                            </Button>
                            <Button type="submit" className="role-primary-button" disabled={isSubmitting}>
                                {isSubmitting ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
                                {isSubmitting ? "Saving..." : "Create Role"}
                            </Button>
                        </div>
                    </Form>
                </div>
            </section>
        </div>
    );
}

export default CreateRole;
