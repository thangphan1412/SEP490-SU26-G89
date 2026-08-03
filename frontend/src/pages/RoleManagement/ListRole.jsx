import { useEffect, useState } from "react";
import { Alert, Button, Form, Spinner, Table } from "react-bootstrap";
import {
    IconEdit,
    IconEye,
    IconPlus,
    IconRefresh,
    IconSearch,
    IconShieldCheck,
    IconTrash,
} from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
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
            dateStyle: "short",
            timeStyle: "short",
        }).format(date);
};

function getRolesFromResponse(response) {
    const payload = response.data?.data ?? response.data;

    if (Array.isArray(payload)) {
        return payload;
    }

    if (Array.isArray(payload?.items)) {
        return payload.items;
    }

    return [];
}

function ListRole() {
    const navigate = useNavigate();
    const [roles, setRoles] = useState([]);
    const [search, setSearch] = useState("");
    const [refreshKey, setRefreshKey] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [deletingId, setDeletingId] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        let isMounted = true;
        const timer = window.setTimeout(async () => {
            setIsLoading(true);
            setError("");

            try {
                const response = await roleApi.searchRoles({ search: search.trim() });

                if (isMounted) {
                    setRoles(getRolesFromResponse(response));
                }
            } catch (requestError) {
                if (isMounted) {
                    setRoles([]);
                    setError(
                        requestError.response?.data?.message
                        || requestError.response?.data?.detail
                        || "Unable to load roles. The Role API may not be available yet."
                    );
                }
            } finally {
                if (isMounted) {
                    setIsLoading(false);
                }
            }
        }, 300);

        return () => {
            isMounted = false;
            window.clearTimeout(timer);
        };
    }, [refreshKey, search]);

    function reloadRoles() {
        setSearch("");
        setRefreshKey((currentKey) => currentKey + 1);
    }

    async function handleDelete(event, role) {
        event.stopPropagation();

        if (!window.confirm(`Delete role "${role.roleName}"?`)) {
            return;
        }

        try {
            setDeletingId(role.id);
            setError("");
            await roleApi.deleteRole(role.id);
            setRefreshKey((currentKey) => currentKey + 1);
        } catch (requestError) {
            setError(
                requestError.response?.data?.message
                || requestError.response?.data?.detail
                || "Unable to delete this role. It may still be assigned to users."
            );
        } finally {
            setDeletingId(null);
        }
    }

    function openAction(event, path) {
        event.stopPropagation();
        navigate(path);
    }

    return (
        <div className="role-layout">
            <section className="role-content">
                <div className="role-panel">
                    <header className="role-panel-header">
                        <div>
                            <h1>Roles</h1>
                            <p>Manage role codes, display names, descriptions, and audit information.</p>
                        </div>
                        <div className="role-header-actions">
                            <Button className="role-primary-button" onClick={() => navigate("/role-management/create")}>
                                <IconPlus size={19} /> New Role
                            </Button>
                        </div>
                    </header>

                    <div className="role-list-body">
                        <div className="role-toolbar">
                            <div className="role-search">
                                <IconSearch size={21} />
                                <Form.Control
                                    type="search"
                                    value={search}
                                    onChange={(event) => setSearch(event.target.value)}
                                    placeholder="Search name, code, description..."
                                    aria-label="Search roles"
                                />
                            </div>
                            <Button
                                variant="light"
                                className="role-icon-button"
                                aria-label="Reload roles"
                                onClick={reloadRoles}
                            >
                                <IconRefresh size={20} />
                            </Button>
                        </div>

                        {error && <Alert variant="danger" className="role-list-alert">{error}</Alert>}

                        <div className="role-table-wrap">
                            <Table hover responsive className="role-table mb-0">
                                <thead>
                                    <tr>
                                        <th>Role Name</th>
                                        <th>Role Code</th>
                                        <th>Description</th>
                                        <th>Created At</th>
                                        <th>Updated At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {isLoading ? (
                                        <tr>
                                            <td colSpan={6} className="role-table-state">
                                                <Spinner animation="border" size="sm" /> Loading roles...
                                            </td>
                                        </tr>
                                    ) : roles.length === 0 ? (
                                        <tr>
                                            <td colSpan={6} className="role-table-state">
                                                <span className="role-empty-icon"><IconShieldCheck size={26} /></span>
                                                <strong>No roles found</strong>
                                                <span>Try another search or create a new role.</span>
                                            </td>
                                        </tr>
                                    ) : roles.map((role) => (
                                        <tr
                                            key={role.id}
                                            className="role-table-row"
                                            onClick={() => navigate(`/role-management/view/${role.id}`)}
                                        >
                                            <td>
                                                <span className="role-avatar"><IconShieldCheck size={19} /></span>
                                                <strong>{role.roleName || "Unnamed role"}</strong>
                                            </td>
                                            <td><span className="role-code-badge">{role.roleCode || "-"}</span></td>
                                            <td className="role-description-cell">{role.roleDescription || "No description"}</td>
                                            <td>{formatDateTime(role.createdAt)}</td>
                                            <td>{formatDateTime(role.updatedAt)}</td>
                                            <td>
                                                <div className="role-row-actions">
                                                    <Button
                                                        variant="light"
                                                        className="role-table-action"
                                                        aria-label={`View ${role.roleName}`}
                                                        onClick={(event) => openAction(event, `/role-management/view/${role.id}`)}
                                                    >
                                                        <IconEye size={16} /> View
                                                    </Button>
                                                    <Button
                                                        variant="light"
                                                        className="role-table-action"
                                                        aria-label={`Edit ${role.roleName}`}
                                                        onClick={(event) => openAction(event, `/role-management/update/${role.id}`)}
                                                    >
                                                        <IconEdit size={16} /> Edit
                                                    </Button>
                                                    <Button
                                                        variant="light"
                                                        className="role-table-delete"
                                                        disabled={deletingId === role.id}
                                                        aria-label={`Delete ${role.roleName}`}
                                                        onClick={(event) => handleDelete(event, role)}
                                                    >
                                                        {deletingId === role.id ? <Spinner animation="border" size="sm" /> : <IconTrash size={16} />}
                                                        {deletingId === role.id ? "Deleting" : "Delete"}
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
}

export default ListRole;
