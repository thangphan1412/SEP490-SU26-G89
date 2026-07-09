import { useState } from "react";
import { Button, Card, Form, Stack } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { ContractTypeFormFields, ContractTypeLayout } from "./ContractTypeComponents.jsx";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

const initialForm = {
  typeCode: "NDA",
  typeName: "Non-Disclosure Contract",
  description: "Contract to protect confidential information shared between parties.",
  validityDays: "365",
  category: "Legal",
  status: "Active",
};

function CreateContractType() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const handleChange = ({ target: { name, value } }) => setForm((current) => ({ ...current, [name]: value }));

  const handleSubmit = (event) => {
    event.preventDefault();
    navigate("/contract-types");
  };

  return (
    <ContractTypeLayout activeItem="create">
      <Card className="contract-type-card">
        <Card.Body>
          <header className="contract-type-form-heading">
            <h1>Create Contract Type</h1>
            <p>Add a new contract type to the system.</p>
          </header>
          <Form id="create-contract-form" onSubmit={handleSubmit} className="contract-type-form">
            <ContractTypeFormFields form={form} onChange={handleChange} />
          </Form>
          <Stack direction="horizontal" className="contract-type-actions">
            <Button variant="outline-secondary" onClick={() => navigate("/contract-types")}>Cancel</Button>
            <Button type="submit" form="create-contract-form" variant="primary">Create Contract Type</Button>
          </Stack>
        </Card.Body>
      </Card>
    </ContractTypeLayout>
  );
}

export default CreateContractType;
