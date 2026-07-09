/* eslint-disable react-refresh/only-export-components */
import { Col, Form, Nav, Row, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {
  IconCirclePlus,
  IconFileDescription,
  IconFileText,
  IconPencil,
  IconChevronDown,
} from "@tabler/icons-react";
import HeaderForm from "../../components/layout/HeaderForm.jsx";

export const contractTypeRecords = [
  {
    code: "NDA",
    name: "Non-Disclosure Contract",
    description: "Contract to protect confidential information shared between parties for various business purposes.",
    validityDays: 365,
    category: "Legal",
    status: "Active",
    createdBy: "Alex Morgan",
    createdAt: "May 10, 2025 10:30 AM",
    updatedAt: "May 20, 2025 03:15 PM",
  },
  {
    code: "MSA",
    name: "Master Service Contract",
    description: "Governs the long-term business relationship between service providers and clients.",
    validityDays: 730,
    category: "Commercial",
    status: "Active",
    createdBy: "Alex Morgan",
    createdAt: "May 11, 2025 09:15 AM",
    updatedAt: "May 18, 2025 02:10 PM",
  },
  {
    code: "SOW",
    name: "Statement of Work",
    description: "Defines the scope of work, project deliverables, milestones, and responsibilities.",
    validityDays: 180,
    category: "Commercial",
    status: "Active",
    createdBy: "Jamie Lee",
    createdAt: "May 12, 2025 11:00 AM",
    updatedAt: "May 19, 2025 04:30 PM",
  },
  {
    code: "SA",
    name: "Service Contract",
    description: "Contract for service performance and delivery obligations.",
    validityDays: 365,
    category: "Commercial",
    status: "Active",
    createdBy: "Taylor Smith",
    createdAt: "May 13, 2025 08:45 AM",
    updatedAt: "May 20, 2025 10:20 AM",
  },
  {
    code: "LOA",
    name: "Letter of Contract",
    description: "Letter confirming the terms and mutual understanding between parties.",
    validityDays: 90,
    category: "Legal",
    status: "Inactive",
    createdBy: "Alex Morgan",
    createdAt: "May 14, 2025 01:20 PM",
    updatedAt: "May 21, 2025 09:00 AM",
  },
  {
    code: "POA",
    name: "Power of Attorney",
    description: "Authorization for one party to act on behalf of another.",
    validityDays: 365,
    category: "Legal",
    status: "Inactive",
    createdBy: "Jamie Lee",
    createdAt: "May 15, 2025 03:40 PM",
    updatedAt: "May 22, 2025 11:15 AM",
  },
];

export function getContractTypeByCode(code) {
  const normalizedCode = String(code || "NDA").toUpperCase();
  return contractTypeRecords.find((item) => item.code === normalizedCode) || contractTypeRecords[0];
}

const sidebarItems = [
  { id: "list", label: "Contract Types", icon: IconFileText, path: "/contract-types" },
  { id: "create", label: "Create Contract Type", icon: IconCirclePlus, path: "/contract-types/new" },
  { id: "details", label: "Contract Type Details", icon: IconFileDescription, path: "/contract-types/detail" },
  { divider: true },
  { id: "templates", label: "Contract Templates", icon: IconFileText },
  { id: "create-template", label: "Create Contract Template", icon: IconCirclePlus },
  { id: "template-details", label: "Template Details", icon: IconFileDescription },
  { id: "template-editor", label: "Template Editor", icon: IconPencil },
];

export function ContractTypeLayout({ activeItem, children }) {
  const navigate = useNavigate();

  return (
    <div className="contract-type-create-page">
      <HeaderForm />
      <div className="contract-type-layout">
        <aside className="contract-type-sidebar">
          <p className="contract-type-sidebar-title">Template Management</p>
          <Nav className="contract-type-sidebar-nav">
            {sidebarItems.map((item, index) => {
              if (item.divider) {
                return <div className="contract-type-sidebar-divider" key={`divider-${index}`} />;
              }

              const ItemIcon = item.icon;
              return (
                <Nav.Link
                  key={item.id}
                  active={item.id === activeItem}
                  onClick={() => item.path && navigate(item.path)}
                  className="contract-type-sidebar-link"
                >
                  <ItemIcon size={22} stroke={1.8} />
                  <span>{item.label}</span>
                </Nav.Link>
              );
            })}
          </Nav>
        </aside>
        <main className="contract-type-main">{children}</main>
      </div>
    </div>
  );
}

export function ContractTypeFormFields({ form, onChange, showHelp = true }) {
  return (
    <>
      <ContractField
        label="Type Code"
        name="typeCode"
        value={form.typeCode}
        onChange={onChange}
        help={showHelp ? "Short, unique code for the contract type." : undefined}
      />
      <ContractField
        label="Type Name"
        name="typeName"
        value={form.typeName}
        onChange={onChange}
        help={showHelp ? "Enter the full name of the contract type." : undefined}
      />

      <Form.Group controlId="description">
        <RequiredLabel>Description</RequiredLabel>
        <Form.Control
          as="textarea"
          rows={4}
          name="description"
          value={form.description}
          onChange={onChange}
          required
        />
        {showHelp && <Form.Text>Provide a brief description of the contract type.</Form.Text>}
      </Form.Group>

      <Row className="contract-type-form-row">
        <Col md={6}>
          <ContractField
            label="Default Validity (days)"
            name="validityDays"
            type="number"
            min="1"
            value={form.validityDays}
            onChange={onChange}
            help={showHelp ? "Number of days the contract is valid by default." : undefined}
          />
        </Col>
        <Col md={6}>
          <Form.Group controlId="category">
            <RequiredLabel>Category</RequiredLabel>
            <div className="contract-type-select-wrap">
              <Form.Select name="category" value={form.category} onChange={onChange} required>
                <option>Legal</option>
                <option>Commercial</option>
                <option>Human Resources</option>
                <option>Procurement</option>
              </Form.Select>
              <IconChevronDown size={18} />
            </div>
            {showHelp && <Form.Text>Select the category this contract type belongs to.</Form.Text>}
          </Form.Group>
        </Col>
      </Row>

      <Form.Group controlId="status">
        <RequiredLabel>Status</RequiredLabel>
        <Stack direction="horizontal" className="contract-type-status-options">
          {["Active", "Inactive"].map((status) => (
            <Form.Check
              key={status}
              type="radio"
              name="status"
              id={`status-${status.toLowerCase()}`}
              label={status}
              value={status}
              checked={form.status === status}
              onChange={onChange}
            />
          ))}
        </Stack>
        {showHelp && <Form.Text>Set the initial status for this contract type.</Form.Text>}
      </Form.Group>
    </>
  );
}

function RequiredLabel({ children }) {
  return <Form.Label>{children} <span className="contract-type-required">*</span></Form.Label>;
}

function ContractField({ label, help, ...controlProps }) {
  return (
    <Form.Group controlId={controlProps.name}>
      <RequiredLabel>{label}</RequiredLabel>
      <Form.Control {...controlProps} required />
      {help && <Form.Text>{help}</Form.Text>}
    </Form.Group>
  );
}
