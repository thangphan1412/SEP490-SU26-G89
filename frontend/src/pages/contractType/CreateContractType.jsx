import { useState } from "react";
import { Button, Card, Col, Form, Nav, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {
  IconChevronDown,
  IconCirclePlus,
  IconFileDescription,
  IconFileText,
  IconPencil,
} from "@tabler/icons-react";
import HeaderForm from "../../components/layout/HeaderForm.jsx";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

const initialForm = {
  typeCode: "NDA",
  typeName: "Non-Disclosure Agreement",
  description: "Agreement to protect confidential information shared between parties.",
  validityDays: "365",
  category: "Legal",
  status: "Active",
};

const sidebarItems = [
  { label: "Agreement Types", icon: IconFileText, path: "/contract-types" },
  { label: "Create Agreement Type", icon: IconCirclePlus, active: true },
  { label: "Agreement Type Details", icon: IconFileDescription },
  { divider: true },
  { label: "Contract Templates", icon: IconFileText },
  { label: "Create Contract Template", icon: IconCirclePlus },
  { label: "Template Details", icon: IconFileDescription },
  { label: "Template Editor", icon: IconPencil },
];

function CreateContractType() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);

  const handleChange = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    navigate("/contract-types");
  };

  return (
    <div className="agreement-create-page">
      <HeaderForm />

      <div className="agreement-layout">
        <aside className="agreement-sidebar">
          <p className="agreement-sidebar-title">Template Management</p>
          <Nav className="agreement-sidebar-nav">
            {sidebarItems.map((item, index) => {
              if (item.divider) {
                return <div className="agreement-sidebar-divider" key={`divider-${index}`} />;
              }

              const ItemIcon = item.icon;
              return (
                <Nav.Link
                  key={item.label}
                  active={item.active}
                  onClick={() => item.path && navigate(item.path)}
                  className="agreement-sidebar-link"
                >
                  <ItemIcon size={22} stroke={1.8} />
                  <span>{item.label}</span>
                </Nav.Link>
              );
            })}
          </Nav>
        </aside>

        <main className="agreement-main">
          <Card className="agreement-card">
            <Card.Body>
              <header className="agreement-form-heading">
                <h1>Create Agreement Type</h1>
                <p>Add a new agreement type to the system.</p>
              </header>

              <Form id="create-agreement-form" onSubmit={handleSubmit} className="agreement-form">
                <AgreementField
                  label="Type Code"
                  name="typeCode"
                  value={form.typeCode}
                  onChange={handleChange}
                  help="Short, unique code for the agreement type."
                />
                <AgreementField
                  label="Type Name"
                  name="typeName"
                  value={form.typeName}
                  onChange={handleChange}
                  help="Enter the full name of the agreement type."
                />

                <Form.Group controlId="description">
                  <RequiredLabel>Description</RequiredLabel>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    required
                  />
                  <Form.Text>Provide a brief description of the agreement type.</Form.Text>
                </Form.Group>

                <Row className="agreement-form-row">
                  <Col md={6}>
                    <AgreementField
                      label="Default Validity (days)"
                      name="validityDays"
                      type="number"
                      min="1"
                      value={form.validityDays}
                      onChange={handleChange}
                      help="Number of days the agreement is valid by default."
                    />
                  </Col>
                  <Col md={6}>
                    <Form.Group controlId="category">
                      <RequiredLabel>Category</RequiredLabel>
                      <div className="agreement-select-wrap">
                        <Form.Select name="category" value={form.category} onChange={handleChange} required>
                          <option>Legal</option>
                          <option>Commercial</option>
                          <option>Human Resources</option>
                          <option>Procurement</option>
                        </Form.Select>
                        <IconChevronDown size={18} />
                      </div>
                      <Form.Text>Select the category this agreement type belongs to.</Form.Text>
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group controlId="status">
                  <RequiredLabel>Status</RequiredLabel>
                  <Stack direction="horizontal" className="agreement-status-options">
                    {["Active", "Inactive"].map((status) => (
                      <Form.Check
                        key={status}
                        type="radio"
                        name="status"
                        id={`status-${status.toLowerCase()}`}
                        label={status}
                        value={status}
                        checked={form.status === status}
                        onChange={handleChange}
                      />
                    ))}
                  </Stack>
                  <Form.Text>Set the initial status for this agreement type.</Form.Text>
                </Form.Group>
              </Form>

              <Stack direction="horizontal" className="agreement-actions">
                <Button variant="outline-secondary" onClick={() => navigate("/contract-types")}>
                  Cancel
                </Button>
                <Button type="submit" form="create-agreement-form" variant="primary">
                  Create Agreement Type
                </Button>
              </Stack>
            </Card.Body>
          </Card>
        </main>
      </div>
    </div>
  );
}

function RequiredLabel({ children }) {
  return <Form.Label>{children} <span className="agreement-required">*</span></Form.Label>;
}

function AgreementField({ label, help, ...controlProps }) {
  return (
    <Form.Group controlId={controlProps.name}>
      <RequiredLabel>{label}</RequiredLabel>
      <Form.Control {...controlProps} required />
      <Form.Text>{help}</Form.Text>
    </Form.Group>
  );
}

export default CreateContractType;
