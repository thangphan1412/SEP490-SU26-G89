import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Row, Spinner } from "react-bootstrap";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import {
    IconCalendar,
    IconCircleCheck,
    IconFileInvoice,
    IconFileText,
    IconSignature,
} from "@tabler/icons-react";

import dashboardApi from "../../services/dashboardService/dashboardApi.js";
import {
    buildDonutGradient,
    formatDashboardStatus,
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
    statusDistribution: [],
    upcomingExpirations: [],
    agreementsOverTime: [],
};

function AgreementStatistics() {
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [stats, setStats] = useState(EMPTY_OVERVIEW);

    const loadOverview = useCallback(async () => {
        setLoading(true);
        setErrorMessage("");
        try {
            const response = await dashboardApi.getOverview();
            setStats(unwrapDashboardResponse(response, EMPTY_OVERVIEW));
        } catch (error) {
            console.error("Unable to load agreement statistics:", error);
            setStats(EMPTY_OVERVIEW);
            setErrorMessage("Unable to load agreement statistics. Please try again.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- starts the initial asynchronous API request.
        loadOverview();
    }, [loadOverview]);

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center vh-100">
                <Spinner animation="border" variant="primary" />
            </div>
        );
    }

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Agreement Statistics</h1>
                <p className="mb-0" style={{ color: MUTED }}>Overview of your contract portfolio and key metrics.</p>
            </div>

            {errorMessage && (
                <Alert variant="danger" className="d-flex justify-content-between align-items-center">
                    <span>{errorMessage}</span>
                    <Button variant="outline-danger" size="sm" onClick={loadOverview}>Retry</Button>
                </Alert>
            )}

            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value={stats.totalAgreements} icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value={stats.activeAgreements} icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value={stats.pendingSignatures} icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Ended Agreements" value={stats.expiredAgreements} icon={IconCalendar} tone="red" /></Col>
            </Row>

            {/* Charts Row */}
            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Agreements Over Time" description="Monthly trend of created agreements">
                        <AgreementsLineChart data={stats.agreementsOverTime} />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Agreements by Status" description="Distribution of agreements by current status">
                        <DonutChart data={stats.statusDistribution} total={stats.totalAgreements} />
                    </ChartCard>
                </Col>
            </Row>

            <Row className="g-3">
                <Col lg={12}><UpcomingExpirations expirations={stats.upcomingExpirations} /></Col>
            </Row>
        </div>
    );
}

// --- SUB COMPONENTS ---
function MetricCard({ label, value, icon: Icon, tone }) {
    const tones = {
        blue: ["#eaf0ff", BLUE],
        green: ["#e5f8ef", "#08b875"],
        orange: ["#fff3e4", "#ff8500"],
        red: ["#ffebed", "#f3273b"]
    };
    const [background, color] = tones[tone];
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e", fontWeight: 500 }}>{label}</span>
                    <span className="rounded-3 d-flex align-items-center justify-content-center" style={{ width: 54, height: 54, color, background }}>
                        <Icon size={27} />
                    </span>
                </div>
                <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
            </Card.Body>
        </Card>
    );
}

function DonutChart({ data, total }) {
    const gradient = useMemo(() => buildDonutGradient(data, total), [data, total]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-4 mt-4">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 206, height: 206, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 122, height: 122 }}>
                    <strong style={{ fontSize: 23 }}>{total}</strong>
                    <small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            <div className="w-100" style={{ maxWidth: 260 }}>
                {data?.length > 0 ? data.map((item) => (
                    <div key={item.label} className="d-flex align-items-center justify-content-between mb-3 gap-3" style={{ color: "#3f4e6b", fontSize: 13 }}>
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

function AgreementsLineChart({ data }) {
    if (!data?.length) {
        return <div className="text-center text-muted py-5 mt-4 fst-italic">No trend data available</div>;
    }

    return (
        <div style={{ width: '100%', height: 230 }}>
            <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#edf0f5" />
                    <XAxis
                        dataKey="month"
                        axisLine={false}
                        tickLine={false}
                        tick={{ fill: '#71809b', fontSize: 12 }}
                        dy={10}
                    />

                    <YAxis
                        allowDecimals={false}
                        axisLine={false}
                        tickLine={false}
                        tick={{ fill: '#71809b', fontSize: 12 }}
                    />

                    <Tooltip
                        contentStyle={{ borderRadius: 8, border: '1px solid #e7ebf3', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}
                        cursor={{ stroke: '#e7ebf3', strokeWidth: 2 }}
                        formatter={(value) => [`${value} Agreements`, 'Total']}
                    />

                    <Line
                        type="monotone"
                        dataKey="count"
                        stroke={BLUE}
                        strokeWidth={3}
                        dot={{ r: 4, fill: BLUE, stroke: '#fff', strokeWidth: 2 }}
                        activeDot={{ r: 6 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}

function UpcomingExpirations({ expirations }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4 pb-3">
                <div className="d-flex justify-content-between">
                    <div>
                        <h2 className="h6 fw-bold mb-1">Upcoming Expirations</h2>
                        <p className="mb-2" style={{ color: MUTED, fontSize: 13 }}>Active contracts expiring in the next 30 days</p>
                    </div>
                </div>

                {expirations?.length > 0 ? expirations.map((exp, index) => (
                    <div key={index} className={`d-flex align-items-center gap-3 py-3 ${index ? "border-top" : ""}`} style={{ borderColor: BORDER }}>
                        <div className="rounded-circle d-flex justify-content-center align-items-center" style={{ width: 31, height: 31, background: "#f1f4fa", color: "#425472" }}>
                            <IconFileText size={17} />
                        </div>
                        <div className="flex-grow-1">
                            <div className="fw-semibold" style={{ fontSize: 13 }}>{exp.title || "Untitled agreement"}</div>
                            <small style={{ color: MUTED }}>{exp.company || "Standalone"}</small>
                        </div>
                        <small className="text-nowrap" style={{ color: MUTED }}>{exp.date}</small>
                        <span className="fw-semibold text-nowrap text-danger" style={{ fontSize: 13 }}>{exp.period}</span>
                    </div>
                )) : (
                    <div className="text-center py-4 text-muted fst-italic">No active contracts are expiring soon.</div>
                )}
            </Card.Body>
        </Card>
    );
}

export default AgreementStatistics;
