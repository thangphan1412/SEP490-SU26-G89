import React, { useMemo, useState, useEffect } from "react";
import { Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import {
    IconArrowLeft,
    IconArrowRight,
    IconCalendar,
    IconCheck,
    IconCircleCheck,
    IconDots,
    IconEye,
    IconFileInvoice,
    IconSearch,
    IconSignature,
    IconTrendingDown,
    IconTrendingUp,
    IconUpload,
} from "@tabler/icons-react";

// --- CONSTANTS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

const MOCK_AGREEMENTS = [
    { id: 1, name: "Master Services Agreement", party: "Acme Corporation", status: "Active", type: "Service", startDate: "May 01, 2025", endDate: "May 01, 2026", value: "$125,000" },
    { id: 2, name: "Software License Agreement", party: "Tech Solutions Inc.", status: "Active", type: "License", startDate: "Apr 15, 2025", endDate: "Apr 15, 2026", value: "$45,000" },
    { id: 3, name: "NDA Agreement", party: "Blue Ltd.", status: "Active", type: "NDA", startDate: "May 10, 2025", endDate: "May 10, 2026", value: "$0" },
    { id: 4, name: "Vendor Agreement", party: "Global Services LLC", status: "Active", type: "Vendor", startDate: "Apr 20, 2025", endDate: "Apr 20, 2026", value: "$75,000" },
    { id: 5, name: "Service Agreement", party: "Innovatech Solutions", status: "Pending Signature", type: "Service", startDate: "May 18, 2025", endDate: "May 18, 2026", value: "$60,000" },
    { id: 6, name: "Employment Agreement", party: "John Doe", status: "Pending Signature", type: "Employment", startDate: "May 20, 2025", endDate: "May 20, 2026", value: "$0" },
    { id: 7, name: "Partnership Agreement", party: "Future Holdings", status: "Draft", type: "Partnership", startDate: "May 21, 2025", endDate: "May 21, 2026", value: "$0" },
    { id: 8, name: "Consulting Agreement", party: "Bright Consulting", status: "Expired", type: "Service", startDate: "May 01, 2024", endDate: "May 01, 2025", value: "$30,000" }
];

// --- MAIN COMPONENT ---
function TotalAgreements() {
    // TODO: Gắn API vào đây
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("All Status");
    const [agreements, setAgreements] = useState(MOCK_AGREEMENTS);

    const filtered = useMemo(() => {
        return agreements.filter((agreement) => {
            const matchesSearch = Object.values(agreement).join(" ").toLowerCase().includes(search.toLowerCase());
            const matchesStatus = status === "All Status" || agreement.status === status;
            return matchesSearch && matchesStatus;
        });
    }, [search, status, agreements]);

    return (
        <div className="bg-white" style={{ color: NAVY, fontFamily: "Inter, system-ui, sans-serif" }}>
            <div className="mb-4">
                <h1 className="fw-bold mb-1" style={{ fontSize: 25 }}>Total Agreements</h1>
                <p className="mb-0" style={{ color: MUTED }}>View and manage all agreements in the system.</p>
            </div>

            {/* Filters */}
            <div className="d-flex flex-wrap align-items-center gap-3 mb-4">
                <SelectField value={status} onChange={setStatus} options={["All Status", "Active", "Pending Signature", "Draft", "Expired"]} style={{ minWidth: 266 }} />
                <SelectField icon={IconCalendar} value="May 1 – May 24, 2025" style={{ minWidth: 248 }} />
                <SearchField value={search} onChange={setSearch} className="ms-lg-auto" style={{ minWidth: 300 }} />
                <Button variant="outline-primary" className="d-flex align-items-center gap-2 px-3" style={{ minHeight: 42, borderColor: "#87a7ff", color: BLUE }}>
                    <IconUpload size={18} />Export
                </Button>
            </div>

            {/* Metrics */}
            <Row className="g-3 mb-4">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value="1,248" change="12.5%" icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value="856" change="8.2%" icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value="128" change="6.3%" icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Expired Agreements" value="24" change="4.0%" direction="down" icon={IconCalendar} tone="red" /></Col>
            </Row>

            <AgreementsTable rows={filtered} />
        </div>
    );
}

// --- SUB COMPONENTS ---
function SelectField({ value, onChange, options, icon: Icon, style }) {
    return (
        <div className="position-relative" style={style}>
            {Icon && <Icon className="position-absolute top-50 translate-middle-y" style={{ left: 16, color: "#3d4b68", zIndex: 1 }} size={18} />}
            <Form.Select value={value} onChange={(e) => onChange?.(e.target.value)} className={`fw-medium ${Icon ? "ps-5" : "ps-3"}`} style={{ height: 42, borderColor: "#dce3ee", color: "#2d3c5d", fontSize: 13 }}>
                {(options || [value]).map((option) => <option key={option} value={option}>{option}</option>)}
            </Form.Select>
        </div>
    );
}

function SearchField({ value, onChange, className, style }) {
    return (
        <div className={`position-relative ${className}`} style={style}>
            <IconSearch className="position-absolute top-50 translate-middle-y" style={{ left: 15, color: "#4c5b78", zIndex: 1 }} size={19} />
            <Form.Control value={value} onChange={(e) => onChange(e.target.value)} placeholder="Search agreements, parties, or tags..." className="ps-5" style={{ height: 42, borderColor: "#dce3ee", fontSize: 13 }} />
        </div>
    );
}

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
                    <span style={{ color: "#52617e" }}>{label}</span>
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

function StatusBadge({ status }) {
    if (status === "Active") return <span className="d-inline-flex align-items-center gap-1 rounded-2" style={{ padding: "5px 9px", background: "#e9f8ef", color: "#08a965" }}><IconCheck size={14} stroke={3} />Active</span>;
    if (status === "Pending Signature") return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#fff4e8", color: "#ff8500" }}>Pending Signature</span>;
    if (status === "Expired") return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#ffedef", color: "#f12f43" }}>Expired</span>;
    return <span className="rounded-2" style={{ padding: "5px 9px", background: "#eff2f6", color: "#52617e" }}>● Draft</span>;
}

function Actions() {
    return (
        <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm bg-white border" aria-label="View agreement" style={{ borderColor: "#dce3ee" }}><IconEye size={17} /></button>
            <button type="button" className="btn btn-sm bg-white border" aria-label="More actions" style={{ borderColor: "#dce3ee" }}><IconDots size={17} /></button>
        </div>
    );
}

function AgreementsTable({ rows }) {
    const headers = ["Agreement Name", "Party", "Status", "Type", "Effective Date", "Expiry Date", "Value", "Actions"];

    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">All Agreements (1,248)</h2>
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
                                <td className="px-4 py-3 text-nowrap"><StatusBadge status={row.status} /></td>
                                <td className="px-4 py-3 text-nowrap">{row.type}</td>
                                <td className="px-4 py-3 text-nowrap">{row.startDate}</td>
                                <td className="px-4 py-3 text-nowrap">{row.endDate}</td>
                                <td className="px-4 py-3 text-nowrap">{row.value}</td>
                                <td className="px-4 py-2"><Actions /></td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                </div>
                <div className="d-flex flex-wrap gap-3 justify-content-between align-items-center px-4 py-3 border-top" style={{ color: MUTED, borderColor: BORDER, fontSize: 13 }}>
                    <span>Showing {rows.length ? 1 : 0} to {rows.length} of 1,248 results</span>
                    <div className="d-flex align-items-center gap-2">
                        <IconArrowLeft size={16} />
                        <button type="button" className="btn btn-sm border" style={{ width: 36, height: 36, borderColor: "#8ba9ff", color: BLUE }}>1</button>
                        {["2", "3", "4", "5", "…", "156"].map((page) => <button type="button" key={page} className="btn btn-sm border-0">{page}</button>)}
                        <IconArrowRight size={16} />
                    </div>
                </div>
            </Card.Body>
        </Card>
    );
}

export default TotalAgreements;