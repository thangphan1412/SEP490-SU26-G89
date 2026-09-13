import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table, Pagination, Stack, Spinner } from "react-bootstrap";
import { IconCalendar, IconClock, IconEye, IconSearch, IconSignature } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import dashboardApi from "../../services/dashboardService/dashboardApi.js";
import {
    buildDonutGradient,
    formatAverageDays,
    formatDashboardStatus,
    getDashboardStatusStyle,
    unwrapDashboardResponse,
} from "./dashboardUtils.js";

const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const EMPTY_PENDING_STATS = {
    totalPending: 0,
    overdue: 0,
    dueIn7Days: 0,
    avgDaysPending: 0,
    pendingByAge: [],
    pendingByProject: [],
};

function PendingSignatureAgreements() {
    const navigate = useNavigate();
    const [search, setSearch] = useState("");
    const [loadingStats, setLoadingStats] = useState(true);
    const [statsError, setStatsError] = useState("");
    const [stats, setStats] = useState(EMPTY_PENDING_STATS);

    const [loadingTable, setLoadingTable] = useState(true);
    const [tableError, setTableError] = useState("");
    const [agreements, setAgreements] = useState([]);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const loadStats = useCallback(async () => {
        setLoadingStats(true);
        setStatsError("");
        try {
            const response = await dashboardApi.getPendingSignatures();
            setStats(unwrapDashboardResponse(response, EMPTY_PENDING_STATS));
        } catch (error) {
            console.error("Unable to load pending signature statistics:", error);
            setStats(EMPTY_PENDING_STATS);
            setStatsError("Unable to load pending signature statistics. Please try again.");
        } finally {
            setLoadingStats(false);
        }
    }, []);

    const fetchContracts = useCallback(async (currPage = 0, currentKeyword = search) => {
        setLoadingTable(true);
        setTableError("");
        try {
            const response = await contractApi.getAllContracts({
                search: currentKeyword,
                status: "PENDING_SIGNATURE",
                page: currPage,
                sortBy: "contractCreatedAt",
                sortDirection: "desc",
            });
            const pageData = response?.data?.data || {};
            setAgreements(pageData.items || []);
            setPagination({
                page: pageData.page ?? currPage,
                size: pageData.size || 10,
                totalElements: pageData.totalElements || 0,
                totalPages: pageData.totalPages || 0,
            });
        } catch (error) {
            console.error("Unable to load pending signature agreements:", error);
            setAgreements([]);
            setPagination((current) => ({ ...current, page: currPage, totalElements: 0, totalPages: 0 }));
            setTableError("Unable to load pending signature agreements. Please try again.");
        } finally {
            setLoadingTable(false);
        }
    }, [search]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- starts the initial asynchronous API request.
        loadStats();
    }, [loadStats]);

    useEffect(() => {
        const delay = setTimeout(() => fetchContracts(0, search), 400);
        return () => clearTimeout(delay);
    }, [fetchContracts, search]);

    if (loadingStats) {
        return <div className="vh-100 d-flex justify-content-center align-items-center"><Spinner animation="border" variant="primary" /></div>;
    }

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Pending Signature Agreements</h1>
                <p className="mb-0" style={{ color: MUTED }}>Track and manage agreements waiting for signatures.</p>
            </div>

            {statsError && (
                <Alert variant="danger" className="d-flex justify-content-between align-items-center">
                    <span>{statsError}</span>
                    <Button variant="outline-danger" size="sm" onClick={loadStats}>Retry</Button>
                </Alert>
            )}

            <div className="d-flex flex-wrap gap-3 mb-4">
                <SearchField value={search} onChange={setSearch} />
            </div>

            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value={stats.totalPending} icon={IconSignature} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Overdue (>14d)" value={stats.overdue} direction="down" icon={IconClock} tone="red" /></Col>
                <Col xl={3} md={6}><MetricCard label="Waiting 8–14d" value={stats.dueIn7Days} icon={IconCalendar} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Avg. Days Pending" value={formatAverageDays(stats.avgDaysPending)} direction="down" icon={IconClock} tone="slate" /></Col>
            </Row>

            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Pending by Project" description="Top projects with pending signature requests">
                        <PartyChart data={stats.pendingByProject} />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Pending by Age" description="Number of agreements by pending duration">
                        <AgeChart data={stats.pendingByAge} total={stats.totalPending} />
                    </ChartCard>
                </Col>
            </Row>

            <PendingTable
                rows={agreements}
                loading={loadingTable}
                errorMessage={tableError}
                pagination={pagination}
                fetchContracts={fetchContracts}
                search={search}
                onView={(contractId) => navigate(`/contract-management/list?viewContractId=${contractId}`)}
            />
        </div>
    );
}

function SearchField({ value, onChange }) {
    return (
        <div className="position-relative flex-grow-1" style={{ minWidth: 260 }}>
            <IconSearch className="position-absolute top-50 translate-middle-y" style={{ left: 15, color: "#4c5b78", zIndex: 1 }} size={19} />
            <Form.Control value={value} onChange={(e) => onChange(e.target.value)} placeholder="Search pending agreements..." className="ps-5" style={{ height: 42, borderColor: "#dce3ee", fontSize: 13 }} />
        </div>
    );
}

function MetricCard({ label, value, icon: Icon, tone }) {
    const tones = { blue: ["#eaf0ff", BLUE], green: ["#e5f8ef", "#08b875"], red: ["#ffebed", "#f3273b"], slate: ["#edf1fb", "#42527b"] };
    const [background, color] = tones[tone];
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e" }}>{label}</span>
                    <span className="rounded-3 d-flex justify-content-center align-items-center" style={{ width: 54, height: 54, background, color }}><Icon size={27} /></span>
                </div>
                <div className="d-flex align-items-center gap-3">
                    <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
                </div>
            </Card.Body>
        </Card>
    );
}

function ChartCard({ title, description, children }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ minHeight: 300, borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <h2 className="h6 fw-bold mb-1">{title}</h2>
                <p className="mb-3" style={{ color: MUTED, fontSize: 13 }}>{description}</p>
                {children}
            </Card.Body>
        </Card>
    );
}

function PartyChart({ data }) {
    if (!data || data.length === 0) return <div className="text-center text-muted mt-5 fst-italic">No data</div>;
    const largestProjectCount = Math.max(...data.map((row) => Number(row.value) || 0));
    return (
        <div className="pt-2 px-2">
            {data.slice(0, 5).map((row, index) => (
                <div className="d-flex align-items-center gap-3 mb-3" key={index}>
                    <span className="text-truncate" style={{ width: 150, color: "#3d4a68", fontSize: 13 }}>{row.name}</span>
                    <div className="flex-grow-1" style={{ height: 10, background: "#edf0f6", borderRadius: 4 }}>
                        <div className="h-100 rounded" style={{ width: `${largestProjectCount === 0 ? 0 : (row.value / largestProjectCount) * 100}%`, background: BLUE }} />
                    </div>
                    <span className="fw-medium" style={{ width: 24, fontSize: 13 }}>{row.value}</span>
                </div>
            ))}
        </div>
    );
}

function AgeChart({ data, total }) {
    const gradient = useMemo(() => buildDonutGradient(data, total), [data, total]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-4 mt-3">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 178, height: 178, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 106, height: 106 }}>
                    <strong style={{ fontSize: 23 }}>{total}</strong><small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            <div className="w-100" style={{ maxWidth: 260 }}>
                {data?.map(item => (
                    <div key={item.label} className="d-flex justify-content-between mb-3 gap-3" style={{ color: "#3f4e6b", fontSize: 13 }}>
                        <span className="d-flex align-items-center gap-2"><i className="rounded-circle" style={{ width: 9, height: 9, background: item.color }} />{item.label}</span>
                        <span className="fw-semibold">{item.value} ({item.percent})</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

function PendingTable({ rows, loading, errorMessage, pagination, fetchContracts, search, onView }) {
    const headers = ["Agreement No", "Title", "Project", "Created Date", "Creator", "Status", "Actions"];
    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-3 px-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">Pending Agreements <span style={{ color: MUTED }}>({pagination.totalElements})</span></h2>
                </div>
                {errorMessage && (
                    <Alert variant="danger" className="m-3 mb-0 d-flex justify-content-between align-items-center">
                        <span>{errorMessage}</span>
                        <Button variant="outline-danger" size="sm" onClick={() => fetchContracts(pagination.page, search)}>Retry</Button>
                    </Alert>
                )}
                <div className="table-responsive">
                    <Table className="align-middle mb-0" style={{ minWidth: 1050, fontSize: 13 }}>
                        <thead style={{ background: "#fafbfe", color: "#3d4a67" }}>
                        <tr>{headers.map(h => <th key={h} className="fw-semibold text-nowrap px-4 py-3">{h}</th>)}</tr>
                        </thead>
                        <tbody>
                        {loading ? (
                            <tr><td colSpan="7" className="text-center py-5 text-muted"><Spinner animation="border" size="sm"/> Loading...</td></tr>
                        ) : rows.length === 0 ? (
                            <tr><td colSpan="7" className="text-center py-5 text-muted fst-italic">No pending agreements.</td></tr>
                        ) : (
                            rows.map(row => (
                                <tr key={row.id}>
                                    <td className="px-4 py-3 fw-semibold text-primary">{row.contractNumber || 'N/A'}</td>
                                    <td className="px-4 py-3 fw-semibold">{row.contractTitle || 'Untitled agreement'}</td>
                                    <td className="px-4 py-3">{row.projectName || 'Standalone'}</td>
                                    <td className="px-4 py-3">{row.contractCreatedAt ? new Date(row.contractCreatedAt).toLocaleDateString() : 'N/A'}</td>
                                    <td className="px-4 py-3">{row.contractCreatedBy || 'N/A'}</td>
                                    <td className="px-4 py-3"><StatusBadge status={row.contractStatus} /></td>
                                    <td className="px-4 py-2">
                                        <div className="d-flex gap-2">
                                            <button type="button" className="btn btn-sm bg-white border" aria-label="View agreement" onClick={() => onView(row.id)}><IconEye size={17}/></button>
                                        </div>
                                    </td>
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
                            <Pagination.Prev disabled={pagination.page === 0 || loading} onClick={() => fetchContracts(pagination.page - 1, search)} />
                            <Pagination.Item active>{pagination.totalPages > 0 ? pagination.page + 1 : 0}</Pagination.Item>
                            <Pagination.Next disabled={(pagination.page + 1) >= pagination.totalPages || loading} onClick={() => fetchContracts(pagination.page + 1, search)} />
                        </Pagination>
                    </div>
                </Stack>
            </Card.Body>
        </Card>
    );
}

function StatusBadge({ status }) {
    const { background, color } = getDashboardStatusStyle(status);
    return (
        <span className="rounded-2 fw-semibold px-2 py-1" style={{ background, color, fontSize: 12 }}>
            {formatDashboardStatus(status)}
        </span>
    );
}

export default PendingSignatureAgreements;
