import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table, Pagination, Stack, Spinner } from "react-bootstrap";
import {
    IconCalendar,
    IconCheck,
    IconCircleCheck,
    IconEye,
    IconFileInvoice,
    IconSearch,
    IconSignature,
} from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";

import contractApi from "../../services/contractService/contractApi.js";
import dashboardApi from "../../services/dashboardService/dashboardApi.js";
import {
    DEFAULT_CONTRACT_STATUSES,
    formatDashboardStatus,
    getDashboardStatusStyle,
    unwrapDashboardResponse,
} from "./dashboardUtils.js";

// --- CONSTANTS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const EMPTY_OVERVIEW = {
    totalAgreements: 0,
    activeAgreements: 0,
    pendingSignatures: 0,
    expiredAgreements: 0,
};

// --- MAIN COMPONENT ---
function TotalAgreements() {
    const navigate = useNavigate();
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [availableStatuses, setAvailableStatuses] = useState(DEFAULT_CONTRACT_STATUSES);
    const [agreements, setAgreements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [tableError, setTableError] = useState("");
    const [stats, setStats] = useState(EMPTY_OVERVIEW);
    const [statsError, setStatsError] = useState("");
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const loadOverview = useCallback(async () => {
        setStatsError("");
        try {
            const response = await dashboardApi.getOverview();
            setStats(unwrapDashboardResponse(response, EMPTY_OVERVIEW));
        } catch (error) {
            console.error("Unable to load total agreement metrics:", error);
            setStats(EMPTY_OVERVIEW);
            setStatsError("Unable to load agreement metrics. Please try again.");
        }
    }, []);

    const fetchContracts = useCallback(async (currPage = 0, currentKeyword = search, currentStatus = status) => {
        setLoading(true);
        setTableError("");
        try {
            const params = {
                search: currentKeyword,
                status: currentStatus,
                page: currPage,
                sortBy: "contractCreatedAt",
                sortDirection: "desc",
            };

            const response = await contractApi.getAllContracts(params);
            const pageData = response?.data?.data || {};

            setAgreements(pageData.items || []);
            setPagination({
                page: pageData.page ?? currPage,
                size: pageData.size || 10,
                totalElements: pageData.totalElements || 0,
                totalPages: pageData.totalPages || 0,
            });
            if (Array.isArray(pageData.availableStatuses) && pageData.availableStatuses.length > 0) {
                setAvailableStatuses(pageData.availableStatuses);
            }
        } catch (error) {
            console.error("Unable to load agreements:", error);
            setAgreements([]);
            setPagination((current) => ({ ...current, page: currPage, totalElements: 0, totalPages: 0 }));
            setTableError("Unable to load agreements. Please try again.");
        } finally {
            setLoading(false);
        }
    }, [search, status]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- starts the initial asynchronous API request.
        loadOverview();
    }, [loadOverview]);

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            fetchContracts(0, search, status);
        }, 400);

        return () => clearTimeout(delayDebounceFn);
    }, [fetchContracts, search, status]);

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Total Agreements</h1>
                <p className="mb-0" style={{ color: MUTED }}>View and manage all agreements in the system.</p>
            </div>

            {/* Filters */}
            <div className="d-flex flex-wrap align-items-center gap-3 mb-4">
                <SelectField
                    value={status}
                    onChange={setStatus}
                    options={[
                        { value: "", label: "All statuses" },
                        ...availableStatuses.map((statusOption) => ({
                            value: statusOption,
                            label: formatDashboardStatus(statusOption),
                        })),
                    ]}
                    style={{ minWidth: 266 }}
                />
                <SearchField value={search} onChange={setSearch} className="ms-lg-auto" style={{ minWidth: 300 }} />
            </div>

            {statsError && (
                <Alert variant="danger" className="d-flex justify-content-between align-items-center">
                    <span>{statsError}</span>
                    <Button variant="outline-danger" size="sm" onClick={loadOverview}>Retry</Button>
                </Alert>
            )}

            <Row className="g-3 mb-4">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value={stats.totalAgreements} icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value={stats.activeAgreements} icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value={stats.pendingSignatures} icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Ended Agreements" value={stats.expiredAgreements} icon={IconCalendar} tone="red" /></Col>
            </Row>

            <AgreementsTable
                rows={agreements}
                loading={loading}
                errorMessage={tableError}
                pagination={pagination}
                fetchContracts={fetchContracts}
                search={search}
                status={status}
                onView={(contractId) => navigate(`/contract-management/list?viewContractId=${contractId}`)}
            />
        </div>
    );
}

// --- SUB COMPONENTS ---
function SelectField({ value, onChange, options, icon: Icon, style }) {
    return (
        <div className="position-relative" style={style}>
            {Icon && <Icon className="position-absolute top-50 translate-middle-y" style={{ left: 16, color: "#3d4b68", zIndex: 1 }} size={18} />}
            <Form.Select value={value} onChange={(e) => onChange?.(e.target.value)} className={`fw-medium ${Icon ? "ps-5" : "ps-3"}`} style={{ height: 42, borderColor: "#dce3ee", color: "#2d3c5d", fontSize: 13 }}>
                {(options || [{ value, label: value }]).map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                ))}
            </Form.Select>
        </div>
    );
}

function SearchField({ value, onChange, className, style }) {
    return (
        <div className={`position-relative ${className}`} style={style}>
            <IconSearch className="position-absolute top-50 translate-middle-y" style={{ left: 15, color: "#4c5b78", zIndex: 1 }} size={19} />
            <Form.Control value={value} onChange={(e) => onChange(e.target.value)} placeholder="Search by Contract Code or Title..." className="ps-5" style={{ height: 42, borderColor: "#dce3ee", fontSize: 13 }} />
        </div>
    );
}

function MetricCard({ label, value, icon: Icon, tone }) {
    const tones = { blue: ["#eaf0ff", BLUE], green: ["#e5f8ef", "#08b875"], orange: ["#fff3e4", "#ff8500"], red: ["#ffebed", "#f3273b"] };
    const [background, color] = tones[tone];
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e" }}>{label}</span>
                    <span className="rounded-3 d-flex justify-content-center align-items-center" style={{ width: 54, height: 54, background, color }}><Icon size={27} /></span>
                </div>
                <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
            </Card.Body>
        </Card>
    );
}

function StatusBadge({ status }) {
    const { background, color } = getDashboardStatusStyle(status);
    const isActive = status?.toUpperCase() === "ACTIVE";
    return (
        <span className="d-inline-flex align-items-center gap-1 rounded-2 fw-semibold" style={{ padding: "5px 9px", background, color }}>
            {isActive && <IconCheck size={14} stroke={3} />}
            {formatDashboardStatus(status)}
        </span>
    );
}

function Actions({ onView }) {
    return (
        <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm bg-white border" aria-label="View agreement" style={{ borderColor: "#dce3ee" }} onClick={onView}><IconEye size={17} /></button>
        </div>
    );
}

function AgreementsTable({ rows, loading, errorMessage, pagination, fetchContracts, search, status, onView }) {
    const headers = ["Contract No", "Title", "Project", "Status", "Type", "Effective Date", "Expiry Date", "Actions"];

    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">Contract List ({pagination.totalElements})</h2>
                </div>
                {errorMessage && (
                    <Alert variant="danger" className="m-3 mb-0 d-flex justify-content-between align-items-center">
                        <span>{errorMessage}</span>
                        <Button variant="outline-danger" size="sm" onClick={() => fetchContracts(pagination.page, search, status)}>Retry</Button>
                    </Alert>
                )}
                <div className="table-responsive">
                    <Table className="align-middle mb-0" style={{ minWidth: 1050, fontSize: 13 }}>
                        <thead style={{ background: "#fafbfe", color: "#3d4a67" }}>
                        <tr>
                            {headers.map((header) => <th key={header} className="fw-semibold text-nowrap px-4 py-3">{header}</th>)}
                        </tr>
                        </thead>
                        <tbody>
                        {loading ? (
                            <tr><td colSpan="8" className="text-center py-5 text-muted"><Spinner animation="border" size="sm" className="me-2"/> Loading agreements...</td></tr>
                        ) : rows.length === 0 ? (
                            <tr><td colSpan="8" className="text-center py-5 text-muted fst-italic">No agreements found.</td></tr>
                        ) : (
                            rows.map((row) => (
                                <tr key={row.id}>
                                    <td className="px-4 py-3 text-nowrap fw-semibold text-primary">{row.contractNumber || 'N/A'}</td>
                                    <td className="px-4 py-3 text-nowrap fw-semibold">{row.contractTitle || 'Untitled'}</td>
                                    <td className="px-4 py-3 text-nowrap">{row.projectName || 'Standalone'}</td>
                                    <td className="px-4 py-3 text-nowrap"><StatusBadge status={row.contractStatus} /></td>
                                    <td className="px-4 py-3 text-nowrap">{row.contractTypeName || '—'}</td>
                                    <td className="px-4 py-3 text-nowrap">{row.effectiveDate || 'N/A'}</td>
                                    <td className="px-4 py-3 text-nowrap">{row.expirationDate || 'N/A'}</td>
                                    <td className="px-4 py-2"><Actions onView={() => onView(row.id)} /></td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </Table>
                </div>

                <Stack direction="horizontal" className="justify-content-between align-items-center px-4 py-3 border-top text-muted" style={{ fontSize: 13 }}>
                    <span>Showing {rows.length > 0 ? (pagination.page * pagination.size) + 1 : 0} to {(pagination.page * pagination.size) + rows.length} of {pagination.totalElements} results</span>
                    <div className="d-flex align-items-center gap-2">
                        <Pagination className="mb-0">
                            <Pagination.Prev disabled={pagination.page === 0 || loading} onClick={() => fetchContracts(pagination.page - 1, search, status)} />
                            <Pagination.Item active>{pagination.totalPages > 0 ? pagination.page + 1 : 0}</Pagination.Item>
                            <Pagination.Next disabled={(pagination.page + 1) >= pagination.totalPages || loading} onClick={() => fetchContracts(pagination.page + 1, search, status)} />
                        </Pagination>
                    </div>
                </Stack>
            </Card.Body>
        </Card>
    );
}

export default TotalAgreements;
