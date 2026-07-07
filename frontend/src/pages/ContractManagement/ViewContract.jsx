import { useNavigate, useParams } from "react-router-dom";
import {
    ContractStatusBadge,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
    getContractById,
    styles,
} from "./ContractComponents.jsx";

function ViewContract() {
    const navigate = useNavigate();
    const { id } = useParams();
    const contract = getContractById(id);

    return (
        <PagePanel
            title="Contract Details"
            description="Review contract information, parties, dates, and linked project."
            action={
                <PrimaryButton onClick={() => navigate(`/contract-management/update/${contract.id}`)}>
                    <Icon name="edit" size={20} color="#ffffff" />
                    Edit Contract
                </PrimaryButton>
            }
        >
            <section style={styles.card}>
                <h2 style={styles.cardTitle}>Contract Overview</h2>
                <div style={localStyles.overviewGrid}>
                    <div style={localStyles.detailColumn}>
                        <DetailRow icon="document" label="Contract ID" value={contract.contractNumber} />
                        <DetailRow icon="document" label="Title" value={contract.title} />
                        <DetailRow icon="users" label="Party" value={contract.party} />
                        <DetailRow icon="dollar" label="Status" value={<ContractStatusBadge status={contract.status} />} />
                    </div>
                    <div style={localStyles.detailColumn}>
                        <DetailRow icon="calendar" label="Effective Date" value={contract.effectiveDate} />
                        <DetailRow icon="calendar" label="Expiration Date" value={contract.expirationDate} />
                        <DetailRow icon="users" label="Owner" value={contract.owner} />
                        <DetailRow icon="link" label="Project" value={contract.project} />
                    </div>
                    <div style={localStyles.summaryColumn}>
                        <p style={localStyles.summaryLabel}>Contract Value</p>
                        <h3 style={localStyles.summaryValue}>{contract.value}</h3>
                        <p style={localStyles.summaryLabel}>Type</p>
                        <p style={localStyles.summaryText}>{contract.type}</p>
                        <p style={localStyles.summaryLabel}>Description</p>
                        <p style={localStyles.description}>{contract.description}</p>
                    </div>
                </div>
            </section>

            <section style={styles.card}>
                <h2 style={{ ...styles.cardTitle, marginBottom: 4 }}>Contract Files & Activity</h2>
                <p style={localStyles.subText}>Static placeholders for the first frontend screen. These can be connected to file storage and audit log APIs later.</p>
                <div style={localStyles.metricGrid}>
                    <MetricItem icon="document" label="Documents" value="1" description="Main contract file" />
                    <MetricItem icon="shield" label="Approvals" value="2" description="Pending legal review" />
                    <MetricItem icon="task" label="Tasks" value="3" description="Open follow-ups" />
                    <MetricItem icon="calendar" label="Last Updated" value="May 12" description="By Alex Morgan" />
                </div>
            </section>
            <InfoAlert>Use Edit Contract to update this mock contract record.</InfoAlert>
        </PagePanel>
    );
}

function DetailRow({ icon, label, value }) {
    return (
        <div style={localStyles.detailRow}>
            <Icon name={icon} size={20} />
            <span style={localStyles.detailLabel}>{label}</span>
            <span style={localStyles.detailValue}>{value}</span>
        </div>
    );
}

function MetricItem({ icon, label, value, description }) {
    return (
        <div style={localStyles.metricItem}>
            <span style={styles.iconCircle}><Icon name={icon} size={28} /></span>
            <div>
                <p style={localStyles.metricLabel}>{label}</p>
                <h3 style={localStyles.metricValue}>{value}</h3>
                <p style={localStyles.metricDescription}>{description}</p>
            </div>
        </div>
    );
}

const localStyles = {
    overviewGrid: { display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: 28 },
    detailColumn: { borderRight: "1px solid #e3e9f2", paddingRight: 28 },
    detailRow: { minHeight: 42, display: "grid", gridTemplateColumns: "28px minmax(92px, 124px) 1fr", alignItems: "center", gap: 10, color: "#243452", fontSize: 13 },
    detailLabel: { color: "#52617f", fontWeight: 700 },
    detailValue: { color: "#243452", fontWeight: 700 },
    summaryColumn: { paddingLeft: 4 },
    summaryLabel: { margin: "0 0 6px", color: "#52617f", fontSize: 13, fontWeight: 800 },
    summaryValue: { margin: "0 0 18px", fontSize: 28, fontWeight: 800 },
    summaryText: { margin: "0 0 18px", color: "#243452", fontWeight: 700 },
    description: { margin: 0, color: "#52617f", lineHeight: 1.55, fontSize: 13 },
    subText: { margin: "0 0 18px", color: "#52617f", fontSize: 14 },
    metricGrid: { display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(190px, 1fr))", gap: 18 },
    metricItem: { border: "1px solid #d9e2ef", borderRadius: 8, padding: "18px", display: "flex", alignItems: "center", gap: 16 },
    metricLabel: { margin: 0, color: "#52617f", fontSize: 12 },
    metricValue: { margin: "4px 0", fontSize: 24, fontWeight: 800 },
    metricDescription: { margin: 0, color: "#52617f", fontSize: 12 },
};

export default ViewContract;
