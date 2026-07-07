import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    CancelButton,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
    contractParties,
    contractStatuses,
    contractTypes,
    getContractById,
} from "./ContractComponents.jsx";

function UpdateContract() {
    const navigate = useNavigate();
    const { id } = useParams();
    const [contract, setContract] = useState(getContractById(id));

    const handleChange = (event) => {
        const { name, value } = event.target;
        setContract((current) => ({ ...current, [name]: value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        navigate(`/contract-management/view/${contract.id}`);
    };

    return (
        <PagePanel
            title="Update Contract"
            description="Update contract details, dates, ownership, and current status."
            action={
                <div style={styles.actions}>
                    <CancelButton onClick={() => navigate(`/contract-management/view/${contract.id}`)} />
                    <PrimaryButton type="submit"><Icon name="save" size={19} color="#fff" />Save Changes</PrimaryButton>
                </div>
            }
        >
            <form onSubmit={handleSubmit}>
                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Contract Information</h2>
                    <div style={localStyles.formGrid}>
                        <TextField label="Contract ID" name="contractNumber" value={contract.contractNumber} onChange={handleChange} />
                        <TextField label="Title" name="title" value={contract.title} onChange={handleChange} />
                        <SelectField label="Party / Parties" name="party" value={contract.party} onChange={handleChange} options={contractParties} />
                        <SelectField label="Contract Type" name="type" value={contract.type} onChange={handleChange} options={contractTypes} />
                        <SelectField label="Status" name="status" value={contract.status} onChange={handleChange} options={contractStatuses} />
                        <TextField label="Effective Date" name="effectiveDate" value={contract.effectiveDate} onChange={handleChange} icon="calendar" />
                        <TextField label="Expiration Date" name="expirationDate" value={contract.expirationDate} onChange={handleChange} icon="calendar" />
                        <TextField label="Owner" name="owner" value={contract.owner} onChange={handleChange} />
                        <TextField label="Project" name="project" value={contract.project} onChange={handleChange} />
                        <TextField label="Contract Value" name="value" value={contract.value} onChange={handleChange} icon="dollar" />
                    </div>
                    <div style={localStyles.fullWidth}>
                        <label htmlFor="description" style={styles.label}>Description</label>
                        <textarea id="description" name="description" value={contract.description} onChange={handleChange} style={styles.textarea} />
                    </div>
                </section>
                <InfoAlert>Changes are only reflected on this mock screen until the API layer is connected.</InfoAlert>
            </form>
        </PagePanel>
    );
}

function TextField({ label, name, value, onChange, icon }) {
    return (
        <div>
            <label htmlFor={name} style={styles.label}>{label}</label>
            <div style={localStyles.inputWrap}>
                {icon && <span style={localStyles.leftIcon}><Icon name={icon} size={18} color="#53617e" /></span>}
                <input id={name} name={name} value={value} onChange={onChange} style={{ ...styles.input, paddingLeft: icon ? 42 : 13 }} />
            </div>
        </div>
    );
}

function SelectField({ label, name, value, onChange, options }) {
    return (
        <div>
            <label htmlFor={name} style={styles.label}>{label}</label>
            <div style={localStyles.inputWrap}>
                <select id={name} name={name} value={value} onChange={onChange} style={styles.input}>
                    {options.map((option) => <option key={option}>{option}</option>)}
                </select>
                <span style={localStyles.rightIcon}><Icon name="chevron" size={18} color="#243452" /></span>
            </div>
        </div>
    );
}

const localStyles = {
    formGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: 30, rowGap: 18 },
    inputWrap: { position: "relative" },
    leftIcon: { position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", display: "flex", zIndex: 1 },
    rightIcon: { position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" },
    fullWidth: { marginTop: 18 },
};

export default UpdateContract;
