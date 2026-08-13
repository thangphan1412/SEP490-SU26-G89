import React, { useState, useEffect } from "react";
import { Card, Col, Form, Row, Table, Pagination, Stack, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconArrowRight, IconCalendar, IconClock, IconDots, IconEye, IconSearch, IconSignature, IconTrendingDown, IconTrendingUp } from "@tabler/icons-react";
import contractApi from "../../services/contractService/contractApi.js";
import dashboardApi from "../../services/dashboardService/dashboardApi.js";

const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

function PendingSignatureAgreements() {
    const [search, setSearch] = useState("");
    const [loadingStats, setLoadingStats] = useState(true);
    const [stats, setStats] = useState(null);

    // Table State
    const [loadingTable, setLoadingTable] = useState(true);
    const [agreements, setAgreements] = useState([]);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    useEffect(() => {
        dashboardApi.getPendingSignatures().then(res => {
            setStats(res.data.data);
            setLoadingStats(false);
        }).catch(err => { console.error(err); setLoadingStats(false); });
    }, []);

    const fetchContracts = async (currPage = 0, currentKeyword = search, currentSize = pagination.size) => {
        setLoadingTable(true);
        try {
            // Chỉ lấy hợp đồng trạng thái PENDING_SIGNATURE
            const response = await contractApi.getAllContracts({ search: currentKeyword, status: "PENDING_SIGNATURE", page: currPage, size: currentSize, sortBy: "contractCreatedAt", sortDirection: "desc" });
            const pageData = response.data.data;
            setAgreements(pageData.items || []);
            setPagination({ page: currPage, size: currentSize, totalElements: pageData.totalElements || 0, totalPages: pageData.totalPages || 0 });
        } catch (error) { console.error(error); setAgreements([]); } finally { setLoadingTable(false); }
    };

    useEffect(() => {
        const delay = setTimeout(() => fetchContracts(0, search, pagination.size), 500);
        return () => clearTimeout(delay);
    }, [search]);

    if (loadingStats || !stats) {
        return <div className="vh-100 d-flex justify-content-center align-items-center"><Spinner animation="border" variant="primary" /></div>;
    }

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Pending Signature Agreements</h1>
                <p className="mb-0" style={{ color: MUTED }}>Track and manage agreements waiting for signatures.</p>
            </div>

            <div className="d-flex flex-wrap gap-3 mb-4">
                <SearchField value={search} onChange={setSearch} />
            </div>

            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value={stats.totalPending} icon={IconSignature} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Overdue (>14d)" value={stats.overdue} direction="down" icon={IconClock} tone="red" /></Col>
                <Col xl={3} md={6}><MetricCard label="Long Wait (>7d)" value={stats.dueIn7Days} icon={IconCalendar} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Avg. Days Pending" value={stats.avgDaysPending.toFixed(1)} direction="down" icon={IconClock} tone="slate" /></Col>
            </Row>

            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Pending by Project" description="Top projects with pending signature requests">
                        <PartyChart data={stats.pendingByProject} maxVal={stats.totalPending} />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Pending by Age" description="Number of agreements by pending duration">
                        <AgeChart data={stats.pendingByAge} total={stats.totalPending} />
                    </ChartCard>
                </Col>
            </Row>

            <PendingTable rows={agreements} loading={loadingTable} pagination={pagination} fetchContracts={fetchContracts} search={search} />
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

function MetricCard({ label, value, direction = "up", icon: Icon, tone }) {
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

function PartyChart({ data, maxVal }) {
    if (!data || data.length === 0) return <div className="text-center text-muted mt-5 fst-italic">No data</div>;
    return (
        <div className="pt-2 px-2">
            {data.slice(0, 5).map((row, index) => (
                <div className="d-flex align-items-center gap-3 mb-3" key={index}>
                    <span className="text-truncate" style={{ width: 150, color: "#3d4a68", fontSize: 13 }}>{row.name}</span>
                    <div className="flex-grow-1" style={{ height: 10, background: "#edf0f6", borderRadius: 4 }}>
                        <div className="h-100 rounded" style={{ width: `${maxVal===0?0:(row.value / maxVal) * 100}%`, background: BLUE }} />
                    </div>
                    <span className="fw-medium" style={{ width: 24, fontSize: 13 }}>{row.value}</span>
                </div>
            ))}
        </div>
    );
}

function AgeChart({ data, total }) {
    const gradient = React.useMemo(() => {
        if (!data || total === 0) return "conic-gradient(#e7ebf3 100%)";
        let start = 0;
        return `conic-gradient(${data.map(item => {
            const end = start + (item.value / total) * 100;
            const res = `${item.color} ${start.toFixed(2)}% ${(end - 0.5).toFixed(2)}%`;
            start = end; return res;
        }).join(", ")})`;
    }, [data, total]);

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

function PendingTable({ rows, loading, pagination, fetchContracts, search }) {
    const headers = ["Agreement No", "Title", "Project", "Created Date", "Creator", "Status", "Actions"];
    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-3 px-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">Pending Agreements <span style={{ color: MUTED }}>({pagination.totalElements})</span></h2>
                </div>
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
                                    <td className="px-4 py-3 fw-semibold">{row.contractTitle}</td>
                                    <td className="px-4 py-3">{row.projectName || 'General'}</td>
                                    <td className="px-4 py-3">{row.contractCreatedAt ? new Date(row.contractCreatedAt).toLocaleDateString() : 'N/A'}</td>
                                    <td className="px-4 py-3">{row.contractCreatedBy || 'N/A'}</td>
                                    <td className="px-4 py-3"><span className="rounded-2 fw-semibold px-2 py-1" style={{ background: "#fff4e8", color: "#ff8500", fontSize: 12 }}>Pending Signature</span></td>
                                    <td className="px-4 py-2">
                                        <div className="d-flex gap-2">
                                            <button className="btn btn-sm bg-white border"><IconEye size={17}/></button>
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
                            <Pagination.Prev disabled={pagination.page === 0 || loading} onClick={() => fetchContracts(pagination.page - 1, search, pagination.size)} />
                            <Pagination.Item active>{pagination.page + 1}</Pagination.Item>
                            <Pagination.Next disabled={(pagination.page + 1) >= pagination.totalPages || loading} onClick={() => fetchContracts(pagination.page + 1, search, pagination.size)} />
                        </Pagination>
                        <Form.Select size="sm" style={{ width: "90px" }} value={pagination.size} onChange={(e) => fetchContracts(0, search, parseInt(e.target.value, 10))}>
                            <option value={10}>10 / page</option>
                            <option value={20}>20 / page</option>
                            <option value={50}>50 / page</option>
                        </Form.Select>
                    </div>
                </Stack>
            </Card.Body>
        </Card>
    );
}

export default PendingSignatureAgreements;