import { Link } from "react-router-dom";

function CreateContractType() {
  return (
    <div className="create-contract-type-page container py-5">
      <div className="card p-4 shadow-sm">
        <h1>Create Contract Type</h1>
        <p className="mb-4">
          This page is under construction. You can return to the Contract Types list for now.
        </p>
        <Link to="/contract-types" className="btn btn-secondary">
          Back to Contract Types
        </Link>
      </div>
    </div>
  );
}

export default CreateContractType;
