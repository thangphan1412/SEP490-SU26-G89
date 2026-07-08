import { useNavigate } from "react-router-dom";
import { contractTypeRecords } from "./ContractTypeComponents.jsx";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

function ContractTypes() {
  const navigate = useNavigate();

  return (
    <div className="contract-types-page">
      <div className="contract-types-topbar">
        <div>
          <h1>Contract Types</h1>
          <p className="contract-types-subtitle">
            Manage all contract types used in contracts.
          </p>
        </div>
        <button
          type="button"
          className="btn btn-primary contract-types-button"
          onClick={() => navigate("/contract-types/new")}
        >
          + New Contract Type
        </button>
      </div>

      <div className="contract-types-card shadow-sm">
        <div className="contract-types-card-header">
          <div className="contract-types-search">
            <input
              type="text"
              placeholder="Search contract types..."
              aria-label="Search contract types"
            />
            <button type="button" className="btn btn-outline-secondary">
              Search
            </button>
          </div>
        </div>

        <div className="table-responsive">
          <table className="table contract-types-table mb-0">
            <thead>
              <tr>
                <th>Type Code</th>
                <th>Type Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {contractTypeRecords.map((item) => (
                <tr key={item.code}>
                  <td>{item.code}</td>
                  <td>{item.name}</td>
                  <td>{item.description}</td>
                  <td>
                    <span
                      className={`badge ${
                        item.status === "Active"
                          ? "bg-success"
                          : "bg-secondary"
                      }`}
                    >
                      {item.status}
                    </span>
                  </td>
                  <td>
                    <button
                      type="button"
                      className="btn btn-sm btn-light"
                      onClick={() => navigate("/contract-types/detail", { state: { code: item.code } })}
                      aria-label={`View ${item.name}`}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default ContractTypes;
