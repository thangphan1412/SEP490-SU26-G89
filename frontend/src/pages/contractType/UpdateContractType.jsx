import { useState } from "react";
import { Button, Card, Form, Stack } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import { ContractTypeFormFields, ContractTypeLayout, getContractTypeByCode } from "./ContractTypeComponents.jsx";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

function UpdateContractType() {
  const navigate = useNavigate();
  const location = useLocation();
  const selectedContractType = getContractTypeByCode(location.state?.code || "NDA");
  const [form, setForm] = useState({
    typeCode: selectedContractType.code,
    typeName: selectedContractType.name,
    description: selectedContractType.description,
    validityDays: String(selectedContractType.validityDays),
    category: selectedContractType.category,
    status: selectedContractType.status,
  });
  const handleChange = ({ target: { name, value } }) => setForm((current) => ({ ...current, [name]: value }));

  const handleSubmit = (event) => {
    event.preventDefault();
    navigate("/contract-types/detail", { state: { code: form.typeCode } });
  };

  return (
    <ContractTypeLayout activeItem="details">
      <Card className="contract-type-card contract-type-update-card">
        <Card.Body>
          <header className="contract-type-form-heading">
            <h1>Update Contract Type</h1>
            <p>Modify the details of the contract type.</p>
          </header>
          <Form id="update-contract-form" onSubmit={handleSubmit} className="contract-type-form contract-type-update-form">
            <ContractTypeFormFields form={form} onChange={handleChange} showHelp={false} />
          </Form>
          <Stack direction="horizontal" className="contract-type-actions contract-type-update-actions">
            <Button
              variant="outline-secondary"
              onClick={() => navigate("/contract-types/detail", { state: { code: selectedContractType.code } })}
            >
              Cancel
            </Button>
            <Button type="submit" form="update-contract-form" variant="primary">Update Contract Type</Button>
          </Stack>
        </Card.Body>
      </Card>
    </ContractTypeLayout>
  );
}

export default UpdateContractType;
