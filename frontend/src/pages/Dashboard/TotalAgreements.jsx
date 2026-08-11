import React, { useState, useEffect } from "react";
import { Button, Card, Col, Form, Row, Table, Pagination, Stack, Spinner } from "react-bootstrap";
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

// SỬ DỤNG ĐÚNG API MÀ BẠN CỦA BẠN VỪA TẠO
import contractApi from "../../services/contractService/contractApi.js";

// --- CONSTANTS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

// --- MAIN COMPONENT ---
function TotalAgreements() {
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("All Status");
    const [agreements, setAgreements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const fetchContracts = async (currPage = 0, currentKeyword = search, currentStatus = status) => {
        setLoading(true);
        try {
            const params = {
                search: currentKeyword,
                status: currentStatus === "All Status" ? "" : currentStatus,
                page: currPage,
                sortBy: "contractCreatedAt", // Đảm bảo sortBy khớp với tên trường trong Entity
                sortDirection: "desc"
            };

            const response = await contractApi.getAllContracts(params);
            const pageData = response.data.data;

            // Lấy chính xác biến items theo DTO của BE
            setAgreements(pageData.items || []);
            setPagination({
                page: currPage,
                size: pageData.size || 10,
                totalElements: pageData.totalElements || 0,
                totalPages: pageData.totalPages || 0
            });
        } catch (error) {
            console.error("Lỗi khi tải danh sách hợp đồng:", error);
            setAgreements([]);
        } finally {
            setLoading(false);
        }
    };

    // Gọi API khi vừa load trang hoặc khi thay đổi Filter/Search
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            fetchContracts(0, search, status);
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [search, status]);

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
                        "All Status",
                        "NEW",
                        "PENDING_INTERNAL_APPROVAL",
                        "PENDING_DIRECTOR_SIGNATURE",
                        "PENDING_PARTNER_SIGNATURE",
                        "ACTIVE",
                        "ENDED",
                        "CANCELLED"
                    ]}
                    style={{ minWidth: 266 }}
                />
                <SearchField value={search} onChange={setSearch} className="ms-lg-auto" style={{ minWidth: 300 }} />
                {/*<Button variant="outline-primary" className="d-flex align-items-center gap-2 px-3" style={{ minHeight: 42, borderColor: "#87a7ff", color: BLUE }}>*/}
                {/*    <IconUpload size={18} />Export*/}
                {/*</Button>*/}
            </div>

            {/* Metrics - Đã thay đổi bỏ Value, thay bằng các Status */}
            <Row className="g-3 mb-4">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value={pagination.totalElements} change="-" icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value="-" change="-" icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value="-" change="-" icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Expired Agreements" value="-" change="-" direction="down" icon={IconCalendar} tone="red" /></Col>
            </Row>

            <AgreementsTable
                rows={agreements}
                loading={loading}
                pagination={pagination}
                fetchContracts={fetchContracts}
                search={search}
                status={status}
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
                {(options || [value]).map((option) => <option key={option} value={option}>{option}</option>)}
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

function MetricCard({ label, value, change, direction = "up", icon: Icon, tone }) {
    const tones = { blue: ["#eaf0ff", BLUE], green: ["#e5f8ef", "#08b875"], orange: ["#fff3e4", "#ff8500"], red: ["#ffebed", "#f3273b"] };
    const [background, color] = tones[tone];
    const ChangeIcon = direction === "down" ? IconTrendingDown : IconTrendingUp;
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <span style={{ color: "#52617e" }}>{label}</span>
                    <span className="rounded-3 d-flex justify-content-center align-items-center" style={{ width: 54, height: 54, background, color }}><Icon size={27} /></span>
                </div>
                <div className="d-flex align-items-center gap-3">
                    <span className="fw-bold" style={{ fontSize: 27 }}>{value}</span>
                    <span className="fw-semibold d-flex align-items-center gap-1" style={{ color: "#08ac68" }}><ChangeIcon size={17} />{change}</span>
                </div>
            </Card.Body>
        </Card>
    );
}

function StatusBadge({ status }) {
    const s = status ? status.toUpperCase() : "NEW";

    if (s === "ACTIVE") return <span className="d-inline-flex align-items-center gap-1 rounded-2 fw-semibold" style={{ padding: "5px 9px", background: "#e9f8ef", color: "#08a965" }}><IconCheck size={14} stroke={3} />Active</span>;

    // Gộp cả 2 trạng thái chờ ký vào chung 1 màu cam
    if (s === "PENDING_DIRECTOR_SIGNATURE" || s === "PENDING_PARTNER_SIGNATURE")
        return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#fff4e8", color: "#ff8500" }}>Pending Signature</span>;

    if (s === "PENDING_INTERNAL_APPROVAL")
        return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#fff4e8", color: "#d97706" }}>Pending Approval</span>;

    if (s === "ENDED" || s === "EXPIRED")
        return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#ffedef", color: "#f12f43" }}>Ended</span>;

    if (s === "CANCELLED")
        return <span className="rounded-2 fw-semibold" style={{ padding: "6px 10px", background: "#f1f5f9", color: "#64748b" }}>Canceled</span>;

    return <span className="rounded-2 fw-semibold" style={{ padding: "5px 9px", background: "#eff2f6", color: "#52617e" }}>● Draft / New</span>;
}

function Actions() {
    return (
        <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm bg-white border" aria-label="View agreement" style={{ borderColor: "#dce3ee" }}><IconEye size={17} /></button>
            <button type="button" className="btn btn-sm bg-white border" aria-label="More actions" style={{ borderColor: "#dce3ee" }}><IconDots size={17} /></button>
        </div>
    );
}

function AgreementsTable({ rows, loading, pagination, fetchContracts, search, status }) {
    // ĐÃ THAY ĐỔI CỘT: Xóa Value, Đổi Party thành Project
    const headers = ["Contract No", "Title", "Project", "Status", "Type", "Effective Date", "Expiry Date", "Actions"];

    return (
        <Card className="border shadow-sm overflow-hidden" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-0">
                <div className="p-4 border-bottom" style={{ borderColor: BORDER }}>
                    <h2 className="h6 fw-bold mb-0">Contract List ({pagination.totalElements})</h2>
                </div>
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
                                    {/* Project Name lấy từ dữ liệu trả về */}
                                    <td className="px-4 py-3 text-nowrap">{row.projectName || 'General'}</td>
                                    <td className="px-4 py-3 text-nowrap"><StatusBadge status={row.contractStatus} /></td>
                                    <td className="px-4 py-3 text-nowrap">{row.contractTypeName || 'Standard'}</td>
                                    <td className="px-4 py-3 text-nowrap">{row.effectiveDate || 'N/A'}</td>
                                    <td className="px-4 py-3 text-nowrap">{row.expirationDate || 'N/A'}</td>
                                    <td className="px-4 py-2"><Actions /></td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </Table>
                </div>

                {/* Pagination Controls */}
                <Stack direction="horizontal" className="justify-content-between align-items-center px-4 py-3 border-top text-muted" style={{ fontSize: 13 }}>
                    <span>Showing {rows.length > 0 ? (pagination.page * pagination.size) + 1 : 0} to {(pagination.page * pagination.size) + rows.length} of {pagination.totalElements} results</span>
                    <div className="d-flex align-items-center gap-2">
                        <Pagination className="mb-0">
                            <Pagination.Prev disabled={pagination.page === 0 || loading} onClick={() => fetchContracts(pagination.page - 1, search, status)} />
                            <Pagination.Item active>{pagination.page + 1}</Pagination.Item>
                            <Pagination.Next disabled={(pagination.page + 1) >= pagination.totalPages || loading} onClick={() => fetchContracts(pagination.page + 1, search, status)} />
                        </Pagination>
                    </div>
                </Stack>
            </Card.Body>
        </Card>
    );
}

export default TotalAgreements;