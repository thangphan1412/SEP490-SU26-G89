import { Button, Card } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import { IconCalendar, IconPencil, IconUser } from "@tabler/icons-react";
import { ContractTypeLayout, getContractTypeByCode } from "./ContractTypeComponents.jsx";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

function ViewContractType() {
  const navigate = useNavigate();
  const location = useLocation();
  const contractType = getContractTypeByCode(location.state?.code || "NDA");

  return (
    <ContractTypeLayout activeItem="details">
      <div className="contract-type-detail-heading">
        <div>
          <nav className="contract-type-breadcrumb" aria-label="Breadcrumb">
            <button type="button" onClick={() => navigate("/contract-types")}>Contract Types</button>
            <span>/</span>
            <span>{contractType.code}</span>
          </nav>
          <h1>Contract Type Details</h1>
          <p>View detailed information of the selected contract type.</p>
        </div>
        <Button
          variant="outline-primary"
          className="contract-type-edit-button"
          onClick={() => navigate("/contract-types/update", { state: { code: contractType.code } })}
        >
          <IconPencil size={18} />
          Edit
        </Button>
      </div>

      <Card className="contract-type-detail-card">
        <Card.Body>
          <section className="contract-type-summary">
            <div className="contract-type-code-tile">{contractType.code}</div>
            <div>
              <h2>{contractType.name}</h2>
              <p>{contractType.description}</p>
            </div>
          </section>

          <section className="contract-type-info-grid">
            <DetailItem label="Type Code" value={contractType.code} />
            <DetailItem label="Type Name" value={contractType.name} />
            <DetailItem label="Description" value={contractType.description} />
            <DetailItem label="Default Validity (days)" value={contractType.validityDays} />
            <DetailItem label="Category" value={contractType.category} />
            <DetailItem
              label="Status"
              value={<span className="contract-type-status"><i />{contractType.status}</span>}
            />
          </section>

          <section className="contract-type-audit-grid">
            <AuditItem icon={IconUser} label="Created By" value={contractType.createdBy} />
            <AuditItem icon={IconCalendar} label="Created At" value={contractType.createdAt} />
            <AuditItem icon={IconCalendar} label="Updated At" value={contractType.updatedAt} />
          </section>
        </Card.Body>
      </Card>
    </ContractTypeLayout>
  );
}

function DetailItem({ label, value }) {
  return (
    <div className="contract-type-detail-item">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function AuditItem({ icon: Icon, label, value }) {
  return (
    <div className="contract-type-audit-item">
      <span className="contract-type-audit-icon"><Icon size={22} /></span>
      <div><span>{label}</span><strong>{value}</strong></div>
    </div>
  );
}

export default ViewContractType;
