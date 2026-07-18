import { useEffect, useState } from "react";
import { Button, Card, Nav } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { IconCalendar, IconCirclePlus, IconFileDescription, IconFileText, IconPencil, IconUser } from "@tabler/icons-react";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

function ViewContractType() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [contractType, setContractType] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadContractType = async () => {
      try {
        const response = await contractTypeApi.getContractTypeById(id);
        setContractType(response.data?.data || null);
      } catch (requestError) {
        setError(requestError.response?.data?.message || "Unable to load contract type.");
      } finally {
        setIsLoading(false);
      }
    };

    loadContractType();
  }, [id]);

  const code = contractType?.contractTypeCode || contractType?.typeCode || contractType?.code || "-";
  const name = contractType?.contractTypeName || contractType?.typeName || contractType?.name || "-";
  const status = contractType?.contractTypeStatus || contractType?.status || "Inactive";

  return (
    <div className="contract-type-create-page">
      <div className="contract-type-layout">
        <aside className="contract-type-sidebar">
          <p className="contract-type-sidebar-title">Template Management</p>
          <Nav className="contract-type-sidebar-nav">
            <Nav.Link className="contract-type-sidebar-link" onClick={() => navigate("/contract-types")}><IconFileText size={22} stroke={1.8} /><span>Contract Types</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link" onClick={() => navigate("/contract-types/new")}><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Type</span></Nav.Link>
            <Nav.Link active className="contract-type-sidebar-link" onClick={() => navigate(`/contract-types/detail/${id}`)}><IconFileDescription size={22} stroke={1.8} /><span>Contract Type Details</span></Nav.Link>
            <div className="contract-type-sidebar-divider" />
            <Nav.Link className="contract-type-sidebar-link"><IconFileText size={22} stroke={1.8} /><span>Contract Templates</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Template</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconFileDescription size={22} stroke={1.8} /><span>Template Details</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconPencil size={22} stroke={1.8} /><span>Template Editor</span></Nav.Link>
          </Nav>
        </aside>

        <main className="contract-type-main">
          <div className="contract-type-detail-heading">
            <div>
              <nav className="contract-type-breadcrumb" aria-label="Breadcrumb">
                <button type="button" onClick={() => navigate("/contract-types")}>Contract Types</button>
                <span>/</span>
                <span>{code}</span>
              </nav>
              <h1>Contract Type Details</h1>
              <p>View detailed information of the selected contract type.</p>
            </div>
            <Button
              variant="outline-primary"
              className="contract-type-edit-button"
              onClick={() => navigate(`/contract-types/update/${id}`)}
              disabled={!contractType}
            >
              <IconPencil size={18} /> Edit
            </Button>
          </div>

          {isLoading && <div className="alert alert-info">Loading contract type...</div>}
          {error && <div className="alert alert-danger">{error}</div>}

          <Card className="contract-type-detail-card">
            <Card.Body>
              <section className="contract-type-summary">
                <div className="contract-type-code-tile">{code}</div>
                <div>
                  <h2>{name}</h2>
                  <p>{contractType?.description || "-"}</p>
                </div>
              </section>

              <section className="contract-type-info-grid">
                <DetailItem label="Type Code" value={code} />
                <DetailItem label="Type Name" value={name} />
                <DetailItem label="Description" value={contractType?.description || "-"} />
                <DetailItem label="Default Validity (days)" value={contractType?.validityDays || "-"} />
                <DetailItem label="Category" value={contractType?.category || "-"} />
                <DetailItem label="Status" value={<span className="contract-type-status"><i />{status}</span>} />
              </section>

              <section className="contract-type-audit-grid">
                <AuditItem icon={IconUser} label="Created By" value={contractType?.createdBy || "-"} />
                <AuditItem icon={IconCalendar} label="Created At" value={contractType?.createdAt || "-"} />
                <AuditItem icon={IconCalendar} label="Updated At" value={contractType?.updatedAt || "-"} />
              </section>
            </Card.Body>
          </Card>
        </main>
      </div>
    </div>
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
