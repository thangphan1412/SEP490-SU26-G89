import { Button, Card, Col, ProgressBar, Row, Stack, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { Icon, InfoAlert, PagePanel, PrimaryButton, StatusBadge } from "./ProjectComponents.jsx";
import "../../assets/styles/css/projectStyles/ViewProject.css";

const overviewLeft = [
    ["document", "Project Name", "Digital Contract Rollout"],
    ["building", "Department", "IT"],
    ["users", "Project Owner", "Alex Morgan"],
    ["dollar", "Status", "Active", "badge"],
    ["flag", "Priority", "High", "priority"],
];

const overviewMiddle = [
    ["calendar", "Start Date", "May 01, 2025"],
    ["calendar", "End Date", "Jul 31, 2025"],
    ["dollar", "Budget", "$120,000"],
    ["location", "Location", "New York, NY, USA"],
];

const metricItems = [
    ["Milestones", "8", "Total Milestones", "flag"],
    ["Team Members", "6", "Active Members", "users"],
    ["Open Tasks", "12", "Tasks Remaining", "task"],
    ["Spent Budget", "$81,600", "68% of Budget", "dollar"],
];

const documents = [
    ["document", "Master Service Agreement", "Contract", "Signed", "Alex Morgan", "May 10, 2025"],
    ["document", "NDA with Supplier A", "Contract", "In Review", "Morgan Lee", "May 08, 2025"],
    ["document", "Procurement Policy Update", "Document", "Approved", "Taylor Smith", "May 06, 2025"],
    ["document", "Implementation Checklist", "Document", "Active", "Jamie Lee", "May 05, 2025"],
    ["document", "Vendor Onboarding Contract", "Contract", "Draft", "Jordan Kim", "May 03, 2025"],
    ["document", "Rollout Timeline", "Document", "Completed", "Casey Brown", "May 01, 2025"],
];

function DetailRow({ icon, label, value, type }) {
    return (
        <Stack direction="horizontal" className="view-project-detail-row">
            <Icon name={icon} size={20} />
            <span className="view-project-detail-label">{label}</span>
            {type === "badge" ? <StatusBadge status={value} /> : type === "priority" ? <StatusBadge status="On Hold" /> : <span className="view-project-detail-value">{value}</span>}
        </Stack>
    );
}

function ViewProject() {
    const navigate = useNavigate();

    return (
        <PagePanel
            title="Project Details"
            description="Review project information, progress, and related contract documents."
            action={<PrimaryButton onClick={() => navigate("/project-management/update")}><Icon name="edit" size={20} color="#ffffff" />Edit Project</PrimaryButton>}
        >
            <Card as="section" className="project-card">
                <Card.Title as="h2" className="project-card-title">Project Overview</Card.Title>
                <Row className="view-project-overview-grid">
                    <Col xs={12} md={6} lg={4} className="view-project-detail-column">
                        {overviewLeft.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} type={item[3]} />)}
                    </Col>
                    <Col xs={12} md={6} lg={4} className="view-project-detail-column">
                        {overviewMiddle.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} />)}
                    </Col>
                    <Col xs={12} lg={4} className="view-project-progress-column">
                        <p className="view-project-progress-title">Progress</p>
                        <div className="view-project-progress-row"><ProgressBar now={68} className="view-project-progress-bar" /><strong>68%</strong></div>
                        <p className="view-project-description-title">Description</p>
                        <p className="view-project-description-text">Company-wide initiative to implement and standardize digital contract management processes across all departments. This project includes system rollout, policy updates, training, and vendor onboarding.</p>
                    </Col>
                </Row>
                <Row className="view-project-metric-grid">
                    {metricItems.map(([label, value, description, icon]) => (
                        <Col xs={12} md={6} lg={3} key={label}>
                            <Stack direction="horizontal" className="view-project-metric-item">
                                <span className="project-icon-circle"><Icon name={icon} size={28} /></span>
                                <div><p className="view-project-metric-label">{label}</p><h3 className="view-project-metric-value">{value}</h3><p className="view-project-metric-description">{description}</p></div>
                            </Stack>
                        </Col>
                    ))}
                </Row>
            </Card>

            <Card as="section" className="project-card">
                <Card.Title as="h2" className="project-card-title view-project-documents-title">Related Contracts & Documents</Card.Title>
                <p className="view-project-sub-text">All contracts and documents associated with this project.</p>
                <div className="view-project-table-wrap">
                    <Table hover responsive={false} className="view-project-table mb-0">
                        <thead>
                            <tr>{["Document Name", "Type", "Status", "Owner", "Last Updated", "Actions"].map((h) => <th key={h} className="view-project-th">{h}</th>)}</tr>
                        </thead>
                        <tbody>
                            {documents.map(([icon, name, type, status, owner, updated]) => (
                                <tr key={name} className="view-project-row">
                                    <td className="view-project-name-cell"><Icon name={icon} size={22} />{name}</td>
                                    <td className="view-project-td">{type}</td>
                                    <td className="view-project-td"><StatusBadge status={status} /></td>
                                    <td className="view-project-td">{owner}</td>
                                    <td className="view-project-td">{updated}</td>
                                    <td className="view-project-td"><Button type="button" variant="light" className="view-project-action-button"><Icon name="dots" size={18} color="#111827" /></Button></td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </div>
            </Card>
            <InfoAlert>To make changes to this project, click <strong>Edit Project</strong>.</InfoAlert>
        </PagePanel>
    );
}

export default ViewProject;
