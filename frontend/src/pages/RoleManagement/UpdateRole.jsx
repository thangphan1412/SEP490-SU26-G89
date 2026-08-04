import { useEffect, useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconDeviceFloppy } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import roleApi from "../../services/roleService/roleApi.js";
import "../../assets/styles/css/roleStyles/Roles.css";

function UpdateRole() {
    const navigate = useNavigate();
    const { id } = useParams();
    const [form, setForm] = useState({
        roleCode: "",
        roleName: "",
        roleDescription: "",
    });
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [loadFailed, setLoadFailed] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let isMounted = true;

        async function loadRole() {
            try {
                setIsLoading(true);
                setError("");
                const response = await roleApi.getRoleById(id);
                const role = response.data?.data ?? response.data;

                if (isMounted) {
                    setForm({
                        roleCode: role?.roleCode || "",
                        roleName: role?.roleName || "",
                        roleDescription: role?.roleDescription || "",
                    });
                    setLoadFailed(!role);
                }
            } catch (requestError) {
                if (isMounted) {
                    setLoadFailed(true);
                    setError(
                        requestError.response?.data?.message
                        || requestError.response?.data?.detail
                        || "Unable to load role."
                    );
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        }

        loadRole();

        return () => {
            isMounted = false;
        };
    }, [id]);

    function handleChange(event) {
        const { name, value } = event.target;
        setForm((currentForm) => ({ ...currentForm, [name]: value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const request = {
            roleCode: form.roleCode.trim().toUpperCase(),
            roleName: form.roleName.trim(),
            roleDescription: form.roleDescription.trim(),
        };

        if (!request.roleCode || !request.roleName) {
            setError("Role code and role name are required.");
            return;
        }

        try {
            setIsSubmitting(true);
            setError("");
            await roleApi.updateRole(id, request);
            navigate(`/role-management/view/${id}`);
        } catch (requestError) {
            setError(
                requestError.response?.data?.message
                || requestError.response?.data?.detail
                || "Unable to update role."
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
                            <h1>Update Role</h1>
                            <p>Update the display name or description while keeping the role code stable.</p>
                        </div>
                        <div className="role-header-actions">
                            <Button className="role-secondary-button" onClick={() => navigate(`/role-management/view/${id}`)}>
                                <IconArrowLeft size={18} /> Back
                            </Button>
                        </div>
                    </header>

                    {isLoading ? (
                        <div className="role-page-state"><Spinner animation="border" /> Loading role...</div>
                    ) : loadFailed ? (
                        <div className="role-page-state role-page-state--error">
                            <Alert variant="danger">{error || "Role was not found."}</Alert>
                        </div>
                    ) : (
                        <Form className="role-form" onSubmit={handleSubmit} noValidate>
                            {error && <Alert variant="danger" className="role-form-alert">{error}</Alert>}

                            <div className="role-form-section">
                                <div className="role-form-section-heading">
                                    <h2>Role information</h2>
                                    <p>Review the current values and save only the changes you need.</p>
                                </div>

                                <div className="role-form-grid">
                                    <Form.Group className="role-field" controlId="update-role-code">
                                        <Form.Label>Role Code <span>*</span></Form.Label>
                                        <Form.Control
                                            name="roleCode"
                                            value={form.roleCode}
                                            maxLength={50}
                                            readOnly
                                            required
                                        />
                                        <Form.Text>The role code cannot be changed after creation.</Form.Text>
                                    </Form.Group>

                                    <Form.Group className="role-field" controlId="update-role-name">
                                        <Form.Label>Role Name <span>*</span></Form.Label>
                                        <Form.Control
                                            name="roleName"
                                            value={form.roleName}
                                            onChange={handleChange}
                                            maxLength={100}
                                            required
                                        />
                                        <Form.Text>The display name shown throughout the application.</Form.Text>
                                    </Form.Group>

                                    <Form.Group className="role-field role-field--wide" controlId="update-role-description">
                                        <Form.Label>Role Description</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            name="roleDescription"
                                            value={form.roleDescription}
                                            onChange={handleChange}
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
                                    onClick={() => navigate(`/role-management/view/${id}`)}
                                    disabled={isSubmitting}
                                >
                                    <IconArrowLeft size={18} /> Cancel
                                </Button>
                                <Button type="submit" className="role-primary-button" disabled={isSubmitting}>
                                    {isSubmitting ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={18} />}
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

export default UpdateRole;
