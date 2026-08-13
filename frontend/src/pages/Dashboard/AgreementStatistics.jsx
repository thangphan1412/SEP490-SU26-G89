import React, { useState, useEffect, useMemo } from "react";
import { Card, Col, Row, Spinner } from "react-bootstrap";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
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

import dashboardApi from "../../services/dashboardService/dashboardApi.js";

// --- CONSTANTS ---
const BLUE = "#1f5eff";
const NAVY = "#101a3e";
const MUTED = "#687694";
const BORDER = "#e7ebf3";

function AgreementStatistics() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalAgreements: 0,
        activeAgreements: 0,
        pendingSignatures: 0,
        expiredAgreements: 0,
        statusDistribution: [],
        upcomingExpirations: [],
        recentActivities: []
    });

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const response = await dashboardApi.getOverview();
                setStats(response.data.data);
            } catch (error) {
                console.error("Lỗi khi tải dữ liệu Dashboard:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchDashboardData();
    }, []);

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

            {/* Metrics Row - DỮ LIỆU THẬT */}
            <Row className="g-3 mb-3">
                <Col xl={3} md={6}><MetricCard label="Total Agreements" value={stats.totalAgreements} change="0%" icon={IconFileInvoice} tone="blue" /></Col>
                <Col xl={3} md={6}><MetricCard label="Active Agreements" value={stats.activeAgreements} change="0%" icon={IconCircleCheck} tone="green" /></Col>
                <Col xl={3} md={6}><MetricCard label="Pending Signatures" value={stats.pendingSignatures} change="0%" icon={IconSignature} tone="orange" /></Col>
                <Col xl={3} md={6}><MetricCard label="Expired Agreements" value={stats.expiredAgreements} change="0%" direction="down" icon={IconCalendar} tone="red" /></Col>
            </Row>

            {/* Charts Row */}
            <Row className="g-3 mb-3">
                <Col lg={6}>
                    <ChartCard title="Agreements Over Time" description="Monthly trend of created agreements">
                        {/* THAY THẾ LineChart CŨ BẰNG COMPONENT MỚI, TRUYỀN DATA VÀO */}
                        <AgreementsLineChart data={stats.agreementsOverTime} />
                    </ChartCard>
                </Col>
                <Col lg={6}>
                    <ChartCard title="Agreements by Status" description="Distribution of agreements by current status">
                        <DonutChart data={stats.statusDistribution} total={stats.totalAgreements} />
                    </ChartCard>
                </Col>
            </Row>

            {/* Tables / Lists Row */}
            <Row className="g-3">
                {/*<Col lg={6}><RecentActivity activities={stats.recentActivities} /></Col>*/}
                <Col lg={12}><UpcomingExpirations expirations={stats.upcomingExpirations} /></Col>
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
            </Card.Body>
        </Card>
    );
}

function DonutChart({ data, total }) {
    // Xử lý chống lỗi NaN khi database trống (chưa có hợp đồng nào)
    const gradient = useMemo(() => {
        if (!data || data.length === 0 || total === 0) return "conic-gradient(#e7ebf3 100%)";

        let startPercent = 0;
        return `conic-gradient(${data.map((item) => {
            const endPercent = startPercent + (item.value / total) * 100;
            const segment = `${item.color} ${startPercent.toFixed(2)}% ${(endPercent - 0.5).toFixed(2)}%`;
            startPercent = endPercent;
            return segment;
        }).join(", ")})`;
    }, [data, total]);

    return (
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-center gap-4 mt-4">
            <div className="rounded-circle position-relative flex-shrink-0" style={{ width: 206, height: 206, background: gradient }}>
                <div className="rounded-circle bg-white position-absolute top-50 start-50 translate-middle d-flex flex-column align-items-center justify-content-center" style={{ width: 122, height: 122 }}>
                    <strong style={{ fontSize: 23 }}>{total}</strong>
                    <small style={{ color: MUTED }}>Total</small>
                </div>
            </div>
            <div className="w-100" style={{ maxWidth: 260 }}>
                {data && data.length > 0 ? data.map((item) => (
                    <div key={item.label} className="d-flex align-items-center justify-content-between mb-3 gap-3" style={{ color: "#3f4e6b", fontSize: 13 }}>
                        <span className="d-flex align-items-center gap-2">
                            <i className="rounded-circle" style={{ width: 9, height: 9, background: item.color }} />
                            {item.label}
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

// Biểu đồ Line dùng thư viện Recharts
function AgreementsLineChart({ data }) {
    // Nếu chưa có dữ liệu hoặc mảng rỗng thì báo trống
    if (!data || data.length === 0) {
        return <div className="text-center text-muted py-5 mt-4 fst-italic">No trend data available</div>;
    }

    return (
        <div style={{ width: '100%', height: 230 }}>
            {/* ResponsiveContainer giúp biểu đồ tự động co giãn theo màn hình */}
            <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    {/* Kẻ đường lưới ngang (bỏ dọc) giống hệt thiết kế cũ */}
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#edf0f5" />

                    {/* Trục X hiển thị Tên Tháng */}
                    <XAxis
                        dataKey="month"
                        axisLine={false}
                        tickLine={false}
                        tick={{ fill: '#71809b', fontSize: 12 }}
                        dy={10}
                    />

                    {/* Trục Y hiển thị số lượng */}
                    <YAxis
                        axisLine={false}
                        tickLine={false}
                        tick={{ fill: '#71809b', fontSize: 12 }}
                    />

                    {/* Tooltip khi di chuột vào sẽ hiện ô thông tin rất mượt */}
                    <Tooltip
                        contentStyle={{ borderRadius: 8, border: '1px solid #e7ebf3', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}
                        cursor={{ stroke: '#e7ebf3', strokeWidth: 2 }}
                        formatter={(value) => [`${value} Agreements`, 'Total']}
                    />

                    {/* Đường nét vẽ biểu đồ */}
                    <Line
                        type="monotone"
                        dataKey="count"
                        stroke="#1f5eff"
                        strokeWidth={3}
                        dot={{ r: 4, fill: '#1f5eff', stroke: '#fff', strokeWidth: 2 }}
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
                        <p className="mb-2" style={{ color: MUTED, fontSize: 13 }}>Contracts expiring in the next 30 days</p>
                    </div>
                </div>

                {expirations && expirations.length > 0 ? expirations.map((exp, index) => (
                    <div key={index} className={`d-flex align-items-center gap-3 py-3 ${index ? "border-top" : ""}`} style={{ borderColor: BORDER }}>
                        <div className="rounded-circle d-flex justify-content-center align-items-center" style={{ width: 31, height: 31, background: "#f1f4fa", color: "#425472" }}>
                            <IconFileText size={17} />
                        </div>
                        <div className="flex-grow-1">
                            <div className="fw-semibold" style={{ fontSize: 13 }}>{exp.title}</div>
                            <small style={{ color: MUTED }}>{exp.company}</small>
                        </div>
                        <small className="text-nowrap" style={{ color: MUTED }}>{exp.date}</small>
                        <span className="fw-semibold text-nowrap text-danger" style={{ fontSize: 13 }}>{exp.period}</span>
                    </div>
                )) : (
                    <div className="text-center py-4 text-muted fst-italic">No contracts expiring soon.</div>
                )}
            </Card.Body>
        </Card>
    );
}

function RecentActivity({ activities }) {
    return (
        <Card className="h-100 border shadow-sm" style={{ borderColor: BORDER, borderRadius: 10 }}>
            <Card.Body className="p-4 pb-3">
                <h2 className="h6 fw-bold mb-1">Recent Activity</h2>
                <p className="mb-2" style={{ color: MUTED, fontSize: 13 }}>Latest updates and actions</p>
                <div className="text-center py-4 text-muted fst-italic">Activity log integration pending...</div>
            </Card.Body>
        </Card>
    );
}

export default AgreementStatistics;