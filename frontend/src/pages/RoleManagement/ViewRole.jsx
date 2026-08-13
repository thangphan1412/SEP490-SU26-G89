import { useEffect, useState } from "react";
import { Alert, Button, Spinner } from "react-bootstrap";
import {
    IconArrowLeft,
    IconCalendar,
    IconEdit,
    IconFileDescription,
    IconHash,
    IconId,
    IconLock,
    IconShieldCheck,
    IconTrash,
    IconUsers,
} from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import roleApi from "../../services/RoleService/roleApi.js";
import "../../assets/styles/css/roleStyles/Roles.css";

const formatDateTime = (value) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? value
        : new Intl.DateTimeFormat("vi-VN", {
            dateStyle: "long",
            timeStyle: "short",
        }).format(date);
};

function ViewRole() {
    const navigate = useNavigate();
    const { id } = useParams();
    const systemRoleCode = id?.startsWith("system-")
        ? decodeURIComponent(id.substring("system-".length))
        : null;
    const [role, setRole] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let isMounted = true;

        async function loadRole() {
            try {
                setIsLoading(true);
                setError("");
                const response = systemRoleCode
                    ? await roleApi.getSystemRoleByCode(systemRoleCode)
                    : await roleApi.getRoleById(id);

                if (isMounted) {
                    setRole(response.data?.data ?? response.data);
                }
            } catch (requestError) {
                if (isMounted) {
                    setRole(null);
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
    }, [id, systemRoleCode]);

    async function handleDelete() {
        if (!role || !window.confirm(`Delete role "${role.roleName}"?`)) {
            return;
        }

        try {
            setIsDeleting(true);
            setError("");
            await roleApi.deleteRole(role.id);
            navigate("/role-management/list");
        } catch (requestError) {
            setError(
                requestError.response?.data?.message
                || requestError.response?.data?.detail
                || "Unable to delete this role. It may still be assigned to users."
            );
        } finally {
            setIsDeleting(false);
        }
    }

    return (
        <div className="role-layout">
            <section className="role-content">
                <div className="role-panel">
                    <header className="role-panel-header">
                        <div>
                            <h1>Role Details</h1>
                            <p>Review system-role usage or custom/project-role information.</p>
                        </div>
                        <div className="role-header-actions">
                            <Button className="role-secondary-button" onClick={() => navigate("/role-management/list")}>
                                <IconArrowLeft size={18} /> Back
                            </Button>
                            {role && !role.systemRole && (
                                <>
                                    <Button className="role-primary-button" onClick={() => navigate(`/role-management/update/${role.id}`)}>
                                        <IconEdit size={18} /> Edit
                                    </Button>
                                    <Button className="role-danger-button" onClick={handleDelete} disabled={isDeleting}>
                                        {isDeleting ? <Spinner animation="border" size="sm" /> : <IconTrash size={18} />}
                                        {isDeleting ? "Deleting..." : "Delete"}
                                    </Button>
                                </>
                            )}
                        </div>
                    </header>

                    <div className="role-view-body">
                        {isLoading ? (
                            <div className="role-page-state"><Spinner animation="border" /> Loading role...</div>
                        ) : !role ? (
                            <Alert variant="danger">{error || "Role was not found."}</Alert>
                        ) : (
                            <>
                                {error && <Alert variant="danger">{error}</Alert>}

                                <section className="role-view-hero">
                                    <span className="role-view-icon"><IconShieldCheck size={34} /></span>
                                    <div>
                                        <h2>{role.roleName || "Unnamed role"}</h2>
                                        <span className="role-code-badge">{role.roleCode || "No code"}</span>
                                        <span className={`role-type-badge role-type-badge--${role.systemRole ? "system" : "custom"}`}>
                                            {role.systemRole && <IconLock size={13} />}
                                            {role.systemRole ? "System · Read only" : "Custom / Project"}
                                        </span>
                                    </div>
                                </section>

                                <section className="role-view-section">
                                    <h2>Role Overview</h2>
                                    <div className="role-overview">
                                        <div className="role-info">
                                            <IconId size={20} />
                                            <span>Role ID</span>
                                            <strong>{role.id || "-"}</strong>
                                        </div>
                                        <div className="role-info">
                                            <IconHash size={20} />
                                            <span>Role Code</span>
                                            <strong>{role.roleCode || "-"}</strong>
                                        </div>
                                        <div className="role-info">
                                            <IconShieldCheck size={20} />
                                            <span>Role Name</span>
                                            <strong>{role.roleName || "-"}</strong>
                                        </div>
                                        <div className="role-info">
                                            <IconUsers size={20} />
                                            <span>Assigned Users</span>
                                            <strong>{role.assignedUserCount ?? 0}</strong>
                                        </div>
                                        <div className="role-info">
                                            <IconCalendar size={20} />
                                            <span>Created At</span>
                                            <strong>{formatDateTime(role.createdAt)}</strong>
                                        </div>
                                        <div className="role-info">
                                            <IconCalendar size={20} />
                                            <span>Updated At</span>
                                            <strong>{formatDateTime(role.updatedAt)}</strong>
                                        </div>
                                        <div className="role-info role-info--wide">
                                            <IconFileDescription size={20} />
                                            <span>Description</span>
                                            <strong>{role.roleDescription || "No description"}</strong>
                                        </div>
                                    </div>
                                </section>
                            </>
                        )}
                    </div>
                </div>
            </section>
        </div>
    );
}

export default ViewRole;
