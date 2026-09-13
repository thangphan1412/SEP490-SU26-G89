import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Row, Table, Spinner } from "react-bootstrap";
import { IconCalendar, IconFileInvoice, IconCircleCheck, IconCircleX } from "@tabler/icons-react";
import { BarChart as RechartsBarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import dashboardApi from "../../services/dashboardService/dashboardApi.js";
import {
    buildDonutGradient,
    formatDashboardStatus,
    unwrapDashboardResponse,
} from "./dashboardUtils.js";

const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const EMPTY_REPORTS = {
    totalAgreements: 0,
    activeAgreements: 0,
    expiredAgreements: 0,
    canceledAgreements: 0,
    typesDistribution: [],
    statusDistribution: [],
    agreementsOverTime: [],
    topExpiring: [],
    topTypes: [],
};

function ContractStatisticalReports() {
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [stats, setStats] = useState(EMPTY_REPORTS);

    const loadReports = useCallback(async () => {
        setLoading(true);
        setErrorMessage("");
        try {
            const response = await dashboardApi.getStatisticalReports();
            setStats(unwrapDashboardResponse(response, EMPTY_REPORTS));
        } catch (error) {
            console.error("Unable to load contract statistical reports:", error);
            setStats(EMPTY_REPORTS);
            setErrorMessage("Unable to load contract statistical reports. Please try again.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- starts the initial asynchronous API request.
        loadReports();
    }, [loadReports]);

    if (loading) {
        return <div className="vh-100 d-flex justify-content-center align-items-center"><Spinner animation="border" variant="primary" /></div>;
    }

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Contract Statistical Reports</h1>
                <p className="mb-0" style={{ color: MUTED }}>In-depth insights and analytics on your contract lifecycle.</p>
            </div>

            {errorMessage && (
                <Alert variant="danger" className="d-flex justify-content-between align-items-center">
                    <span>{errorMessage}</span>
                    <Button variant="outline-danger" size="sm" onClick={loadReports}>Retry</Button>
                </Alert>
            )}

            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value={stats.totalAgreements} icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value={stats.activeAgreements} icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Ended Agreements" value={stats.expiredAgreements} icon={IconCalendar} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Canceled" value={stats.canceledAgreements} icon={IconCircleX} tone="red" /></Col>
            </Row>

            <Row className="g-3 mb-3">
                <Col xl={4}>
                    <ChartCard title="Agreements by Type" description="Distribution by contract type">
                        <DonutChart data={stats.typesDistribution} total={stats.totalAgreements} />
                    </ChartCard>
                </Col>
                <Col xl={4}>
                    <ChartCard title="Agreements Over Time" description="Monthly trend of agreements">
                        <AutoBarChart data={stats.agreementsOverTime} />
                    </ChartCard>
                </Col>
                <Col xl={4}>
                    <ChartCard title="Contracts by Status" description="Distribution by current status">
                        <DonutChart data={stats.statusDistribution} total={stats.totalAgreements} />
                    </ChartCard>
                </Col>
            </Row>

            <Row className="g-3">
                <Col lg={7}><TopExpiringTable data={stats.topExpiring} /></Col>
                <Col lg={5}><TopTypes data={stats.topTypes} /></Col>
            </Row>
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

function DonutChart({ data, total }) {
    const gradient = useMemo(() => buildDonutGradient(data, total), [data, total]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-3 mt-4">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 177, height: 177, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 105, height: 105 }}>
                    <strong style={{ fontSize: 21 }}>{total}</strong><small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            {/* Nới rộng maxWidth lên 280 và cho phép chữ tự cắt ... */}
            <div className="flex-grow-1" style={{ maxWidth: 280 }}>
                {data?.length > 0 ? data.map(item => (
                    <div key={item.label} className="d-flex justify-content-between align-items-center mb-3 gap-2" style={{ color: "#3f4e6b", fontSize: 12 }}>
                        <span className="d-flex align-items-center gap-2 text-truncate" title={formatDashboardStatus(item.label)}>
                            <i className="rounded-circle flex-shrink-0" style={{ width: 9, height: 9, background: item.color }} />
                            <span className="text-truncate">{formatDashboardStatus(item.label)}</span>
                        </span>
                        <span className="fw-semibold text-nowrap">{item.value} ({item.percent})</span>
                    </div>
                )) : <div className="text-muted fst-italic">No data available</div>}
            </div>
        </div>
    );
}

function AutoBarChart({ data }) {
    if (!data || data.length === 0) return <div className="text-center text-muted py-5 fst-italic">No data</div>;
    return (
        <div style={{ width: '100%', height: 230 }}>
            <ResponsiveContainer width="100%" height="100%">
                <RechartsBarChart data={data} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#edf0f5" />
                    <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fill: '#71809b', fontSize: 11 }} dy={10} />
                    <YAxis allowDecimals={false} axisLine={false} tickLine={false} tick={{ fill: '#71809b', fontSize: 11 }} />
                    <Tooltip cursor={{ fill: '#f4f6fa' }} contentStyle={{ borderRadius: 8 }} formatter={(value) => [`${value} Agreements`, 'Total']} />
                    <Bar dataKey="count" fill={BLUE} radius={[4, 4, 0, 0]} barSize={25} />
                </RechartsBarChart>
            </ResponsiveContainer>
        </div>
    );
}

function TopExpiringTable({ data }) {
    return (
        <Card className="h-100 border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-3 px-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-1">Top Expiring Agreements</h2>
                    <p className="mb-0" style={{ color: MUTED, fontSize: 12 }}>Contracts expiring in the next 30 days</p>
                </div>
                <div className="table-responsive">
                    <Table className="mb-0 align-middle" style={{ minWidth: 650, fontSize: 12 }}>
                        <thead style={{ color: "#3d4a68", background: "#fbfcff" }}>
                        <tr>
                            {/* Đã thêm padding (px-4) để căn lề chuẩn xác 100% */}
                            <th className="px-4 py-3 fw-semibold">Agreement Name</th>
                            <th className="px-3 py-3 fw-semibold">Project</th>
                            <th className="px-3 py-3 fw-semibold">Expiry Date</th>
                            <th className="px-3 py-3 fw-semibold">Days Left</th>
                            <th className="px-3 py-3 fw-semibold">Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data?.length > 0 ? data.map((row, i) => (
                            <tr key={i}>
                                <td className="px-4 py-3 fw-semibold">{row.title || "Untitled agreement"}</td>
                                <td className="px-3 py-3">{row.company || "Standalone"}</td>
                                <td className="px-3 py-3">{row.date}</td>
                                <td className="px-3 py-3"><span className="text-danger fw-bold">{row.period}</span></td>
                                <td className="px-3 py-3"><span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#fff4e8", color: "#ff8500" }}>Expiring Soon</span></td>
                            </tr>
                        )) : (
                            <tr><td colSpan="5" className="text-center py-5 text-muted fst-italic">No active contracts are expiring soon.</td></tr>
                        )}
                        </tbody>
                    </Table>
                </div>
            </Card.Body>
        </Card>
    );
}

function TopTypes({ data }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <h2 className="h6 fw-bold mb-1">Agreements by Type</h2>
                <p className="mb-4" style={{ color: MUTED, fontSize: 12 }}>Total count by contract type</p>
                {data?.length > 0 ? data.map((row, index) => (
                    <div key={index} className="d-flex align-items-center gap-3 mb-3">
                        <span className="fw-semibold text-truncate" style={{ fontSize: 12, width: 112 }}>{row.name}</span>
                        <div className="flex-grow-1 rounded" style={{ height: 12, background: "#eff2f7" }}>
                            <div className="h-100 rounded" style={{ width: `${row.percent}%`, background: BLUE }} />
                        </div>
                        <span className="text-end fw-semibold" style={{ width: 55, fontSize: 12, color: "#50607e" }}>{row.count}</span>
                    </div>
                )) : <div className="text-center text-muted py-5 fst-italic">No contract type data available.</div>}
            </Card.Body>
        </Card>
    );
}

export default ContractStatisticalReports;
