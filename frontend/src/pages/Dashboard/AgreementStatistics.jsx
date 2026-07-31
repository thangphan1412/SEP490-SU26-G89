import React, { useState, useEffect, useMemo } from "react";
import { Card, Col, Row } from "react-bootstrap";
import {
    IconCalendar,
    IconCircleCheck,
    IconFileInvoice,
    IconFileText,
    IconSignature,
    IconTrendingDown,
    IconTrendingUp,
    IconUpload,
} from "@tabler/icons-react";

// --- CONSTANTS & COLORS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const MOCK_STATUS_SERIES = [
    { label: "Active", value: 856, percent: "68.8%", color: "#2361ed" },
    { label: "Pending Signature", value: 128, percent: "10.3%", color: "#ff8909" },
    { label: "Expired", value: 24, percent: "1.9%", color: "#fa4455" },
    { label: "Draft", value: 160, percent: "12.8%", color: "#9eabc0" },
    { label: "Canceled", value: 80, percent: "6.4%", color: "#4d5c74" },
];

const MOCK_RECENT_ACTIVITY = [
    { title: "NDA Agreement with Acme Corp has been signed.", detail: "By Jane Smith • May 30, 2025 10:24 AM", status: "Completed", tone: "green", icon: IconSignature },
    { title: "Service Agreement with Blue Ltd is pending signature.", detail: "By Mike Johnson • May 29, 2025 02:15 PM", status: "Pending", tone: "orange", icon: IconCalendar },
    { title: "Master Services Agreement has been uploaded.", detail: "By Admin • May 28, 2025 08:45 AM", status: "Uploaded", tone: "blue", icon: IconUpload }
];

const MOCK_UPCOMING_EXPIRATIONS = [
    { title: "Software License Agreement", company: "Acme Corporation", date: "Jun 05, 2025", period: "In 12 days" },
    { title: "Maintenance Agreement", company: "Tech Solutions Inc.", date: "Jun 12, 2025", period: "In 19 days" },
    { title: "Vendor Agreement", company: "Global Services LLC", date: "Jun 20, 2025", period: "In 27 days" }
];

// --- MAIN COMPONENT ---
function AgreementStatistics() {
    // TODO: Thay thế bằng dữ liệu gọi từ API (useEffect)
    const [statusSeries, setStatusSeries] = useState(MOCK_STATUS_SERIES);
    const [recentActivity, setRecentActivity] = useState(MOCK_RECENT_ACTIVITY);
    const [expirations, setExpirations] = useState(MOCK_UPCOMING_EXPIRATIONS);

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Agreement Statistics</h1>
                <p className="mb-0" style={{ color: MUTED }}>Overview of your contract portfolio and key metrics.</p>
            </div>

            {/* Metrics Row */}
            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value="1,248" change="12.5%" icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value="856" change="8.2%" icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value="128" change="6.3%" icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Expired Agreements" value="24" change="4.0%" direction="down" icon={IconCalendar} tone="red" /></Col>
            </Row>

            {/* Charts Row */}
            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Agreements Over Time" description="Monthly trend of created agreements">
                        <LineChart />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Agreements by Status" description="Distribution of agreements by current status">
                        <DonutChart data={statusSeries} />
                    </ChartCard>
                </Col>
            </Row>

            {/* Tables / Lists Row */}
            <Row className="g-3">
                <Col lg={6}><RecentActivity activities={recentActivity} /></Col>
                <Col lg={6}><UpcomingExpirations expirations={expirations} /></Col>
            </Row>
        </div>
    );
}

// --- SUB COMPONENTS ---
function MetricCard({ label, value, change, direction = "up", icon: Icon, tone }) {
    const tones = {
        blue: ["#eaf0ff", BLUE],
        green: ["#e5f8ef", "#08b875"],
        orange: ["#fff3e4", "#ff8500"],
        red: ["#ffebed", "#f3273b"]
    };
    const [background, color] = tones[tone];
    const ChangeIcon = direction === "down" ? IconTrendingDown : IconTrendingUp;

    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e", fontWeight: 500 }}>{label}</span>
                    <span className="rounded-3 d-flex align-items-center justify-content-center" style={{ width: 54, height: 54, color, background }}>
                        <Icon size={27} />
                    </span>
                </div>
                <div className="d-flex align-items-center gap-3 mt-1">
                    <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
                    <span className="fw-semibold d-inline-flex align-items-center gap-1" style={{ color: direction === "down" ? "#f3273b" : "#08ac68" }}>
                        <ChangeIcon size={17} />{change}
                    </span>
                </div>
                <div className="mt-2" style={{ color: MUTED, fontSize: 13 }}>vs Apr 1 – Apr 30, 2025</div>
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

function LineChart() {
    // TODO: Nên sử dụng Recharts hoặc thư viện chart để thay thế đoạn SVG cứng này
    const points = "32,170 120,126 210,120 298,103 386,89 474,48";
    const labels = ["Dec ’24", "Jan ’25", "Feb ’25", "Mar ’25", "Apr ’25", "May ’25"];

    return (
        <svg viewBox="0 0 520 230" className="w-100" role="img" aria-label="Agreement growth line chart" style={{ height: 230 }}>
            <defs>
                <linearGradient id="statistics-area" x1="0" x2="0" y1="0" y2="1">
                    <stop stopColor="#2b64ef" stopOpacity=".18" />
                    <stop offset="1" stopColor="#2b64ef" stopOpacity="0" />
                </linearGradient>
            </defs>
            {[38, 82, 126, 170, 214].map((y, index) => (
                <g key={y}>
                    <line x1="32" x2="505" y1={y} y2={y} stroke="#edf0f5" />
                    <text x="2" y={y + 4} fill="#71809b" fontSize="12">{["1K", "750", "500", "250", "0"][index]}</text>
                </g>
            ))}
            <path d={`M ${points} L 474 214 L 32 214 Z`} fill="url(#statistics-area)" />
            <polyline points={points} fill="none" stroke={BLUE} strokeWidth="2.4" />
            {points.split(" ").map((point) => {
                const [x, y] = point.split(",");
                return <circle key={point} cx={x} cy={y} r="5" fill={BLUE} stroke="#fff" strokeWidth="1.5" />;
            })}
            {labels.map((label, index) => (
                <text key={label} x={32 + index * 88.4} y="226" textAnchor="middle" fill="#71809b" fontSize="12">{label}</text>
            ))}
            <g transform="translate(438 70)">
                <rect width="78" height="75" rx="9" fill="#fff" stroke={BORDER} />
                <text x="14" y="25" fill="#65738d" fontSize="12">May ’25</text>
                <text x="14" y="47" fill={NAVY} fontSize="17" fontWeight="700">690</text>
                <text x="14" y="63" fill="#65738d" fontSize="11">Agreements</text>
            </g>
        </svg>
    );
}

function DonutChart({ data }) {
    const gradient = useMemo(() => {
        const total = data.reduce((sum, item) => sum + item.value, 0);
        return `conic-gradient(${data.map((item, index) => {
            const start = data.slice(0, index).reduce((sum, previous) => sum + (previous.value / total) * 100, 0);
            const end = start + (item.value / total) * 100;
            return `${item.color} ${start.toFixed(2)}% ${(end - .7).toFixed(2)}%`;
        }).join(", ")})`;
    }, [data]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-4 mt-4">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 206, height: 206, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 122, height: 122 }}>
                    <strong style={{ fontSize: 23 }}>1,248</strong>
                    <small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            <div className="w-100" style={{ maxWidth: 260 }}>
                {data.map((item) => (
                    <div key={item.label} className="d-flex align-items-center justify-content-between mb-3 gap-3" style={{ color: "#3f4e6b", fontSize: 13 }}>
                        <span className="d-flex align-items-center gap-2">
                            <i className="rounded-circle" style={{ width: 9, height: 9, background: item.color }} />
                            {item.label}
                        </span>
                        <span className="fw-semibold text-nowrap">{item.value} ({item.percent})</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

function StatusPill({ text, tone }) {
    const tones = {
        green: ["#e9f8ef", "#08a965"],
        orange: ["#fff4e8", "#ff8500"],
        blue: ["#edf2ff", BLUE]
    };
    const [background, color] = tones[tone];

    return (
        <span className="rounded-2 fw-semibold text-nowrap" style={{ padding: "6px 10px", fontSize: 12, background, color }}>
            {text}
        </span>
    );
}

function RecentActivity({ activities }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4 pb-3">
                <h2 className="h6 fw-bold mb-1">Recent Activity</h2>
                <p className="mb-2" style={{ color: MUTED, fontSize: 13 }}>Latest updates and actions</p>

                {activities.map(({ title, detail, status, tone, icon: Icon }, index) => (
                    <div key={index} className={`d-flex align-items-center gap-3 py-3 ${index ? "border-top" : ""}`} style={{ borderColor: BORDER }}>
                        <div className="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: 38, height: 38, background: tone === "green" ? "#eaf8f2" : tone === "orange" ? "#fff4e7" : "#edf2ff", color: tone === "green" ? "#09ae70" : tone === "orange" ? "#ff8909" : BLUE }}>
                            <Icon size={20} />
                        </div>
                        <div className="flex-grow-1" style={{ minWidth: 0 }}>
                            <div className="fw-semibold text-truncate" style={{ fontSize: 13 }}>{title}</div>
                            <small style={{ color: MUTED }}>{detail}</small>
                        </div>
                        <StatusPill text={status} tone={tone} />
                    </div>
                ))}
            </Card.Body>
            <Card.Footer className="bg-white text-center border-top py-3" style={{ borderColor: BORDER }}>
                <button type="button" className="btn btn-link text-decoration-none p-0" style={{ color: BLUE, fontSize: 13 }}>View all activity</button>
            </Card.Footer>
        </Card>
    );
}

function UpcomingExpirations({ expirations }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4 pb-3">
                <div className="d-flex justify-content-between">
                    <div>
                        <h2 className="h6 fw-bold mb-1">Upcoming Expirations</h2>
                        <p className="mb-2" style={{ color: MUTED, fontSize: 13 }}>Management expiring in the next 30 days</p>
                    </div>
                    <button type="button" className="btn btn-link p-0 align-self-start text-decoration-none" style={{ color: BLUE, fontSize: 13 }}>View all</button>
                </div>

                {expirations.map((exp, index) => (
                    <div key={index} className={`d-flex align-items-center gap-3 py-3 ${index ? "border-top" : ""}`} style={{ borderColor: BORDER }}>
                        <div className="rounded-circle d-flex justify-content-center align-items-center" style={{ width: 31, height: 31, background: "#f1f4fa", color: "#425472" }}>
                            <IconFileText size={17} />
                        </div>
                        <div className="flex-grow-1">
                            <div className="fw-semibold" style={{ fontSize: 13 }}>{exp.title}</div>
                            <small style={{ color: MUTED }}>{exp.company}</small>
                        </div>
                        <small className="text-nowrap" style={{ color: MUTED }}>{exp.date}</small>
                        <span className="fw-semibold text-nowrap" style={{ fontSize: 13 }}>{exp.period}</span>
                    </div>
                ))}
            </Card.Body>
            <Card.Footer className="bg-white text-center border-top py-3" style={{ borderColor: BORDER }}>
                <button type="button" className="btn btn-link text-decoration-none p-0" style={{ color: BLUE, fontSize: 13 }}>View all expirations</button>
            </Card.Footer>
        </Card>
    );
}

export default AgreementStatistics;