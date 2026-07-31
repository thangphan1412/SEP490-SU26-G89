import React, { useMemo, useState, useEffect } from "react";
import { Card, Col, Form, Row, Table } from "react-bootstrap";
import {
    IconArrowLeft,
    IconArrowRight,
    IconCalendar,
    IconClock,
    IconDots,
    IconEye,
    IconSearch,
    IconSignature,
    IconTrendingDown,
    IconTrendingUp,
} from "@tabler/icons-react";

// --- CONSTANTS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const MOCK_PENDING_AGE = [
    { label: "0 – 3 Days", value: 46, percent: "35.9%", color: "#2361ed" },
    { label: "4 – 7 Days", value: 32, percent: "25.0%", color: "#2ab784" },
    { label: "8 – 14 Days", value: 24, percent: "18.8%", color: "#ff9800" },
    { label: "15 – 30 Days", value: 16, percent: "12.5%", color: "#fa4455" },
    { label: "> 30 Days", value: 8, percent: "6.2%", color: "#63728e" }
];

const MOCK_PARTIES = [
    { name: "Acme Corporation", value: 24 },
    { name: "Tech Solutions Inc.", value: 18 },
    { name: "Blue Ltd.", value: 14 },
    { name: "Innovatech Solutions", value: 12 },
    { name: "Global Services LLC", value: 10 }
];

const MOCK_AGREEMENTS = [
    { id: 1, name: "Service Agreement", party: "Acme Corporation", requestedTo: "Jane Smith", reqDate: "May 20, 2025", dueDate: "May 27, 2025", pending: "4", status: "Due in 7 Days" },
    { id: 2, name: "Software License Agreement", party: "Tech Solutions Inc.", requestedTo: "Mike Johnson", reqDate: "May 16, 2025", dueDate: "May 23, 2025", pending: "6", status: "Overdue" },
    { id: 3, name: "NDA Agreement", party: "Blue Ltd.", requestedTo: "Sarah Lee", reqDate: "May 19, 2025", dueDate: "May 26, 2025", pending: "5", status: "Due in 3 Days" },
    { id: 4, name: "Employment Agreement", party: "Innovatech Solutions", requestedTo: "Robert Brown", reqDate: "May 21, 2025", dueDate: "May 28, 2025", pending: "3", status: "Due in 7 Days" }
];

// --- MAIN COMPONENT ---
function PendingSignatureAgreements() {
    // TODO: Liên kết với API
    const [search, setSearch] = useState("");
    const [agreements, setAgreements] = useState(MOCK_AGREEMENTS);
    const [pendingAge, setPendingAge] = useState(MOCK_PENDING_AGE);
    const [partyData, setPartyData] = useState(MOCK_PARTIES);

    // Lọc table
    const filtered = useMemo(() => {
        return agreements.filter((agreement) => {
            const searchString = Object.values(agreement).join(" ").toLowerCase();
            return searchString.includes(search.toLowerCase());
        });
    }, [search, agreements]);

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Pending Signature Agreements</h1>
                <p className="mb-0" style={{ color: MUTED }}>Track and manage agreements waiting for signatures.</p>
            </div>

            {/* Filters */}
            <div className="d-flex flex-wrap gap-3 mb-4">
                <SelectField value="All Parties" style={{ minWidth: 252 }} />
                <SelectField value="All Types" style={{ minWidth: 252 }} />
                <SelectField value="Sort by: Requested Date" style={{ minWidth: 228 }} />
                <SearchField value={search} onChange={setSearch} />
            </div>

            {/* Metrics */}
            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value="128" change="6.3%" icon={IconSignature} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Overdue" value="15" change="3.5%" direction="down" icon={IconClock} tone="red" /></Col>
                <Col xl={3} md={6}><MetricCard label="Due in 7 Days" value="32" change="8.0%" icon={IconCalendar} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Avg. Days Pending" value="4.6" change="1.2" direction="down" icon={IconClock} tone="slate" /></Col>
            </Row>

            {/* Charts */}
            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Pending by Party" description="Top parties with pending signature requests">
                        <PartyChart data={partyData} />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Pending by Age" description="Number of agreements by pending duration">
                        <AgeChart data={pendingAge} />
                    </ChartCard>
                </Col>
            </Row>

            <PendingTable rows={filtered} />
        </div>
    );
}

// --- SUB COMPONENTS ---
function SelectField({ value, style }) {
    return (
        <div className="position-relative" style={style}>
            <Form.Select value={value} className="fw-medium ps-3" style={{ height: 42, borderColor: "#dce3ee", color: "#2d3c5d", fontSize: 13 }} readOnly>
                <option>{value}</option>
            </Form.Select>
        </div>
    );
}

function SearchField({ value, onChange }) {
    return (
        <div className="position-relative flex-grow-1" style={{ minWidth: 260 }}>
            <IconSearch className="position-absolute top-50 translate-middle-y" style={{ left: 15, color: "#4c5b78", zIndex: 1 }} size={19} />
            <Form.Control value={value} onChange={(e) => onChange(e.target.value)} placeholder="Search agreements or parties..." className="ps-5" style={{ height: 42, borderColor: "#dce3ee", fontSize: 13 }} />
        </div>
    );
}

function MetricCard({ label, value, change, direction = "up", icon: Icon, tone }) {
    const tones = {
        blue: ["#eaf0ff", BLUE],
        green: ["#e5f8ef", "#08b875"],
        red: ["#ffebed", "#f3273b"],
        slate: ["#edf1fb", "#42527b"]
    };
    const [background, color] = tones[tone];
    const ChangeIcon = direction === "down" ? IconTrendingDown : IconTrendingUp;

    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e" }}>{label}</span>
                    <span className="rounded-3 d-flex justify-content-center align-items-center" style={{ width: 54, height: 54, background, color }}>
                        <Icon size={27} />
                    </span>
                </div>
                <div className="d-flex align-items-center gap-3">
                    <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
                    <span className="fw-semibold d-flex align-items-center gap-1" style={{ color: direction === "down" ? "#f3273b" : "#08ac68" }}>
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

function PartyChart({ data }) {
    return (
        <div className="pt-2 px-2">
            {data.map((row, index) => (
                <div className="d-flex align-items-center gap-3 mb-3" key={index}>
                    <span style={{ width: 150, color: "#3d4a68", fontSize: 13 }}>{row.name}</span>
                    <div className="flex-grow-1" style={{ height: 10, background: "#edf0f6", borderRadius: 4 }}>
                        <div className="h-100 rounded" style={{ width: `${(row.value / 24) * 100}%`, background: BLUE }} />
                    </div>
                    <span className="fw-medium" style={{ width: 24, fontSize: 13 }}>{row.value}</span>
                </div>
            ))}
            <div className="d-flex justify-content-between ps-5 ms-5 mt-2" style={{ color: MUTED, fontSize: 12 }}>
                <span>0</span><span>10</span><span>20</span><span>30</span>
            </div>
        </div>
    );
}

function AgeChart({ data }) {
    const gradient = useMemo(() => {
        const total = data.reduce((sum, item) => sum + item.value, 0);
        return `conic-gradient(${data.map((item, index) => {
            const start = data.slice(0, index).reduce((sum, previous) => sum + (previous.value / total) * 100, 0);
            const end = start + (item.value / total) * 100;
            return `${item.color} ${start.toFixed(2)}% ${(end - .7).toFixed(2)}%`;
        }).join(", ")})`;
    }, [data]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-4 mt-3">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 178, height: 178, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 106, height: 106 }}>
                    <strong style={{ fontSize: 23 }}>128</strong>
                    <small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            <div className="w-100" style={{ maxWidth: 260 }}>
                {data.map((item) => (
                    <div key={item.label} className="d-flex justify-content-between mb-3 gap-3" style={{ color: "#3f4e6b", fontSize: 13 }}>
                        <span className="d-flex align-items-center gap-2">
                            <i className="rounded-circle" style={{ width: 9, height: 9, background: item.color }} />{item.label}
                        </span>
                        <span className="fw-semibold">{item.value} ({item.percent})</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

function Pill({ value }) {
    return (
        <span className="rounded-2 fw-semibold text-nowrap" style={{ padding: "6px 10px", fontSize: 12, background: value === "Overdue" ? "#ffedef" : "#fff4e8", color: value === "Overdue" ? "#f12f43" : "#ff8500" }}>
            {value}
        </span>
    );
}

function Actions() {
    return (
        <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm bg-white border" aria-label="View agreement" style={{ borderColor: "#dce3ee" }}><IconEye size={17} /></button>
            <button type="button" className="btn btn-sm bg-white border" aria-label="More actions" style={{ borderColor: "#dce3ee" }}><IconDots size={17} /></button>
        </div>
    );
}

function PendingTable({ rows }) {
    const headers = ["Agreement Name", "Party", "Requested To", "Requested Date", "Due Date", "Days Pending", "Status", "Actions"];

    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-3 px-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">Pending Agreements <span style={{ color: MUTED }}>(128)</span></h2>
                </div>
                <div className="table-responsive">
                    <Table className="align-middle mb-0" style={{ minWidth: 1050, fontSize: 13 }}>
                        <thead style={{ background: "#fafbfe", color: "#3d4a67" }}>
                        <tr>
                            {headers.map((header) => <th key={header} className="fw-semibold text-nowrap px-4 py-3">{header}</th>)}
                        </tr>
                        </thead>
                        <tbody>
                        {rows.map((row) => (
                            <tr key={row.id}>
                                <td className="px-4 py-3 text-nowrap fw-semibold">{row.name}</td>
                                <td className="px-4 py-3 text-nowrap">{row.party}</td>
                                <td className="px-4 py-3 text-nowrap">{row.requestedTo}</td>
                                <td className="px-4 py-3 text-nowrap">{row.reqDate}</td>
                                <td className="px-4 py-3 text-nowrap">{row.dueDate}</td>
                                <td className="px-4 py-3 text-nowrap">{row.pending}</td>
                                <td className="px-4 py-3 text-nowrap"><Pill value={row.status} /></td>
                                <td className="px-4 py-2"><Actions /></td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                </div>
                <div className="d-flex flex-wrap gap-3 justify-content-between align-items-center px-4 py-3 border-top" style={{ color: MUTED, borderColor: BORDER, fontSize: 13 }}>
                    <span>Showing {rows.length ? 1 : 0} to {rows.length} of 128 results</span>
                    <div className="d-flex align-items-center gap-2">
                        <IconArrowLeft size={16} />
                        <button type="button" className="btn btn-sm border" style={{ width: 36, height: 36, borderColor: "#8ba9ff", color: BLUE }}>1</button>
                        {["2", "3", "4", "5", "…", "32"].map((page) => <button type="button" key={page} className="btn btn-sm border-0">{page}</button>)}
                        <IconArrowRight size={16} />
                    </div>
                </div>
            </Card.Body>
        </Card>
    );
}

export default PendingSignatureAgreements;