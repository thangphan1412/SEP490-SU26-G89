import { useState } from "react";
import { Button, Card, Col, Form, Nav, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { IconChevronDown, IconCirclePlus, IconFileDescription, IconFileText, IconPencil } from "@tabler/icons-react";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

const initialForm = {
  typeCode: "",
  typeName: "",
  description: "",
  validityDays: "",
  category: "Legal",
  status: "Active",
};

function CreateContractType() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleChange = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await contractTypeApi.createContractType({
        contractTypeCode: form.typeCode,
        contractTypeName: form.typeName,
        description: form.description,
        validityDays: Number(form.validityDays),
        category: form.category,
        status: form.status,
      });
      navigate("/contract-types");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to create contract type.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="contract-type-create-page">
      <div className="contract-type-layout">
        <aside className="contract-type-sidebar">
          <p className="contract-type-sidebar-title">Template Management</p>
          <Nav className="contract-type-sidebar-nav">
            <Nav.Link className="contract-type-sidebar-link" onClick={() => navigate("/contract-types")}><IconFileText size={22} stroke={1.8} /><span>Contract Types</span></Nav.Link>
            <Nav.Link active className="contract-type-sidebar-link" onClick={() => navigate("/contract-types/new")}><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Type</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconFileDescription size={22} stroke={1.8} /><span>Contract Type Details</span></Nav.Link>
            <div className="contract-type-sidebar-divider" />
            <Nav.Link className="contract-type-sidebar-link"><IconFileText size={22} stroke={1.8} /><span>Contract Templates</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Template</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconFileDescription size={22} stroke={1.8} /><span>Template Details</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconPencil size={22} stroke={1.8} /><span>Template Editor</span></Nav.Link>
          </Nav>
        </aside>

        <main className="contract-type-main">
          <Card className="contract-type-card">
            <Card.Body>
              <header className="contract-type-form-heading">
                <h1>Create Contract Type</h1>
                <p>Add a new contract type to the system.</p>
              </header>
              {error && <div className="alert alert-danger">{error}</div>}

              <Form id="create-contract-form" onSubmit={handleSubmit} className="contract-type-form">
                <Form.Group controlId="typeCode">
                  <Form.Label>Type Code <span className="contract-type-required">*</span></Form.Label>
                  <Form.Control name="typeCode" value={form.typeCode} onChange={handleChange} required />
                  <Form.Text>Short, unique code for the contract type.</Form.Text>
                </Form.Group>

                <Form.Group controlId="typeName">
                  <Form.Label>Type Name <span className="contract-type-required">*</span></Form.Label>
                  <Form.Control name="typeName" value={form.typeName} onChange={handleChange} required />
                  <Form.Text>Enter the full name of the contract type.</Form.Text>
                </Form.Group>

                <Form.Group controlId="description">
                  <Form.Label>Description <span className="contract-type-required">*</span></Form.Label>
                  <Form.Control as="textarea" rows={4} name="description" value={form.description} onChange={handleChange} required />
                  <Form.Text>Provide a brief description of the contract type.</Form.Text>
                </Form.Group>

                <Row className="contract-type-form-row">
                  <Col md={6}>
                    <Form.Group controlId="validityDays">
                      <Form.Label>Default Validity (days) <span className="contract-type-required">*</span></Form.Label>
                      <Form.Control type="number" min="1" name="validityDays" value={form.validityDays} onChange={handleChange} required />
                      <Form.Text>Number of days the contract is valid by default.</Form.Text>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group controlId="category">
                      <Form.Label>Category <span className="contract-type-required">*</span></Form.Label>
                      <div className="contract-type-select-wrap">
                        <Form.Select name="category" value={form.category} onChange={handleChange} required>
                          <option>Legal</option>
                          <option>Commercial</option>
                          <option>Human Resources</option>
                          <option>Procurement</option>
                        </Form.Select>
                        <IconChevronDown size={18} />
                      </div>
                      <Form.Text>Select the category this contract type belongs to.</Form.Text>
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group controlId="status">
                  <Form.Label>Status <span className="contract-type-required">*</span></Form.Label>
                  <Stack direction="horizontal" className="contract-type-status-options">
                    {["Active", "Inactive"].map((status) => (
                      <Form.Check
                        key={status}
                        type="radio"
                        name="status"
                        id={`create-status-${status.toLowerCase()}`}
                        label={status}
                        value={status}
                        checked={form.status === status}
                        onChange={handleChange}
                      />
                    ))}
                  </Stack>
                  <Form.Text>Set the initial status for this contract type.</Form.Text>
                </Form.Group>
              </Form>

              <Stack direction="horizontal" className="contract-type-actions">
                <Button variant="outline-secondary" onClick={() => navigate("/contract-types")}>Cancel</Button>
                <Button type="submit" form="create-contract-form" variant="primary" disabled={isSubmitting}>
                  {isSubmitting ? "Creating..." : "Create Contract Type"}
                </Button>
              </Stack>
            </Card.Body>
          </Card>
        </main>
      </div>
    </div>
  );
}

export default CreateContractType;
