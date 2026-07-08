import { useEffect, useState } from "react";
import { Button, Card, Form, Stack, Table } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { viewProject } from "../../config/axiosConfig.js";
import { Icon, PagePanel, PrimaryButton, StatusBadge } from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/ViewProject.css";

function showValue(value) {
    return value || "-";
}

function normalizeText(value) {
    return String(value || "").trim().toLowerCase();
}

function DetailRow({ label, value, isStatus = false }) {
    return (
        <div className="view-project-detail-row">
            <span className="view-project-detail-label">{label}</span>
            {isStatus ? (
                <StatusBadge status={value} />
            ) : (
                <span className="view-project-detail-value">{showValue(value)}</span>
            )}
        </div>
    );
}

function EmptyRow({ colSpan, message }) {
    return (
        <tr>
            <td colSpan={colSpan} className="view-project-empty-cell">
                {message}
            </td>
        </tr>
    );
}

function ViewProject() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const projectId = searchParams.get("id");
    const [project, setProject] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [userSearch, setUserSearch] = useState("");
    const [contractSearch, setContractSearch] = useState("");
    const [contractStatus, setContractStatus] = useState("");

    useEffect(() => {
        const loadProject = async () => {
            if (!projectId) {
                setProject(null);
                setError("Project id is missing. Please choose a project from the list.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError("");

                const response = await viewProject(projectId);
                const payload = response.data?.data ?? response.data;
                setProject(payload);
            } catch (apiError) {
                console.error("Unable to load project detail:", apiError);
                setProject(null);
                setError("Unable to load this project. Please try again later.");
            } finally {
                setLoading(false);
            }
        };

        loadProject();
    }, [projectId]);

    const projectUsers = Array.isArray(project?.users) ? project.users : [];
    const projectContracts = Array.isArray(project?.contracts) ? project.contracts : [];
    const userSearchText = normalizeText(userSearch);
    const contractSearchText = normalizeText(contractSearch);
    const contractStatusText = normalizeText(contractStatus);

    const filteredUsers = projectUsers.filter((user) =>
        normalizeText(user.userName).includes(userSearchText)
    );

    const contractStatusOptions = [...new Set(
        projectContracts
            .map((contract) => contract.contractStatus)
            .filter((status) => normalizeText(status))
    )].sort();

    const filteredContracts = projectContracts.filter((contract) => {
        const matchesName = normalizeText(contract.contractTitle).includes(contractSearchText);
        const matchesStatus = !contractStatusText
            || normalizeText(contract.contractStatus) === contractStatusText;

        return matchesName && matchesStatus;
    });

    const clearUserSearch = () => {
        setUserSearch("");
    };

    const clearContractFilters = () => {
        setContractSearch("");
        setContractStatus("");
    };

    const pageAction = (
        <Stack direction="horizontal" gap={2} className="view-project-actions">
            <Button
                type="button"
                variant="light"
                className="view-project-back-button"
                onClick={() => navigate("/project-management/list")}
            >
                Back
            </Button>

            {projectId && (
                <PrimaryButton onClick={() => navigate(`/project-management/update?id=${projectId}`)}>
                    <Icon name="edit" size={20} color="#ffffff" />
                    Edit Project
                </PrimaryButton>
            )}
        </Stack>
    );

    return (
        <PagePanel
            title="Project Details"
            description="View project information, users, and contracts from the database."
            action={pageAction}
        >
            {loading ? (
                <Card as="section" className="project-card">
                    <p className="view-project-state-text">Loading project...</p>
                </Card>
            ) : error ? (
                <Card as="section" className="project-card">
                    <p className="view-project-state-text view-project-state-text--error">
                        {error}
                    </p>
                </Card>
            ) : (
                <>
                    <Card as="section" className="project-card">
                        <Card.Title as="h2" className="project-card-title">
                            Basic Information
                        </Card.Title>

                        <div className="view-project-info-grid">
                            <DetailRow label="Name" value={project.projectName} />
                            <DetailRow label="Project Code" value={project.projectCode} />
                            <DetailRow label="Status" value={project.projectStatus} isStatus />
                            <DetailRow label="Start Date" value={project.projectStartDate} />
                            <DetailRow label="End Date" value={project.projectEndDate} />
                            <DetailRow label="Created By" value={project.projectCreatedBy} />
                            <DetailRow label="Created At" value={project.projectCreatedAt} />
                            <div className="view-project-description-row">
                                <span className="view-project-detail-label">Description</span>
                                <p className="view-project-description-text">
                                    {showValue(project.projectDescription)}
                                </p>
                            </div>
                        </div>
                    </Card>

                    <Card as="section" className="project-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-card-title">
                                Project Users
                            </Card.Title>

                            <span className="view-project-result-count">
                                {filteredUsers.length} / {projectUsers.length} users
                            </span>
                        </div>

                        <div className="view-project-filter-bar">
                            <Form.Group
                                className="view-project-search-box"
                                controlId="project-user-search"
                            >
                                <Form.Label className="view-project-filter-label">
                                    Search user
                                </Form.Label>
                                <Form.Control
                                    className="view-project-filter-input"
                                    value={userSearch}
                                    placeholder="Search by user name..."
                                    onChange={(event) => setUserSearch(event.target.value)}
                                />
                            </Form.Group>

                            {userSearch && (
                                <Button
                                    type="button"
                                    variant="light"
                                    className="view-project-clear-button"
                                    onClick={clearUserSearch}
                                >
                                    Clear
                                </Button>
                            )}
                        </div>

                        <div className="view-project-table-wrap">
                            <Table hover responsive={false} className="view-project-table mb-0">
                                <thead>
                                    <tr>
                                        <th className="view-project-th">User Name</th>
                                        <th className="view-project-th">Permission</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectUsers.length === 0 ? (
                                        <EmptyRow
                                            colSpan={2}
                                            message="No users are assigned to this project."
                                        />
                                    ) : filteredUsers.length === 0 ? (
                                        <EmptyRow
                                            colSpan={2}
                                            message="No users match your search."
                                        />
                                    ) : (
                                        filteredUsers.map((user, index) => (
                                            <tr
                                                key={`${user.userName}-${user.permission}-${index}`}
                                                className="view-project-row"
                                            >
                                                <td className="view-project-td">
                                                    {showValue(user.userName)}
                                                </td>
                                                <td className="view-project-td">
                                                    {showValue(user.permission)}
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>

                    <Card as="section" className="project-card">
                        <div className="view-project-section-header">
                            <Card.Title as="h2" className="project-card-title">
                                Project Contracts
                            </Card.Title>

                            <span className="view-project-result-count">
                                {filteredContracts.length} / {projectContracts.length} contracts
                            </span>
                        </div>

                        <div className="view-project-filter-bar">
                            <Form.Group
                                className="view-project-search-box"
                                controlId="project-contract-search"
                            >
                                <Form.Label className="view-project-filter-label">
                                    Search contract
                                </Form.Label>
                                <Form.Control
                                    className="view-project-filter-input"
                                    value={contractSearch}
                                    placeholder="Search by contract title..."
                                    onChange={(event) => setContractSearch(event.target.value)}
                                />
                            </Form.Group>

                            <Form.Group
                                className="view-project-status-filter"
                                controlId="project-contract-status"
                            >
                                <Form.Label className="view-project-filter-label">
                                    Status
                                </Form.Label>
                                <Form.Select
                                    className="view-project-filter-input"
                                    value={contractStatus}
                                    onChange={(event) => setContractStatus(event.target.value)}
                                >
                                    <option value="">All statuses</option>
                                    {contractStatusOptions.map((status) => (
                                        <option key={status} value={status}>
                                            {status}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>

                            {(contractSearch || contractStatus) && (
                                <Button
                                    type="button"
                                    variant="light"
                                    className="view-project-clear-button"
                                    onClick={clearContractFilters}
                                >
                                    Clear
                                </Button>
                            )}
                        </div>

                        <div className="view-project-table-wrap">
                            <Table hover responsive={false} className="view-project-table mb-0">
                                <thead>
                                    <tr>
                                        <th className="view-project-th">Contract Title</th>
                                        <th className="view-project-th">Contract Number</th>
                                        <th className="view-project-th">Contract Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {projectContracts.length === 0 ? (
                                        <EmptyRow
                                            colSpan={3}
                                            message="No contracts belong to this project."
                                        />
                                    ) : filteredContracts.length === 0 ? (
                                        <EmptyRow
                                            colSpan={3}
                                            message="No contracts match your filters."
                                        />
                                    ) : (
                                        filteredContracts.map((contract, index) => (
                                            <tr
                                                key={`${contract.contractNumber}-${index}`}
                                                className="view-project-row"
                                            >
                                                <td className="view-project-td">
                                                    {showValue(contract.contractTitle)}
                                                </td>
                                                <td className="view-project-td">
                                                    {showValue(contract.contractNumber)}
                                                </td>
                                                <td className="view-project-td">
                                                    <StatusBadge status={contract.contractStatus} />
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                </>
            )}
        </PagePanel>
    );
}

export default ViewProject;
