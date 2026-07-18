import { useEffect, useMemo, useState } from "react";
import { Nav } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { IconCirclePlus, IconFileDescription, IconFileText, IconPencil } from "@tabler/icons-react";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import "../../assets/styles/css/layoutStyles/ContractTypes.css";

const getContractTypeCode = (item) => item.contractTypeCode || item.typeCode || item.code || "-";
const getContractTypeName = (item) => item.contractTypeName || item.typeName || item.name || "-";

function ListContractType() {
  const navigate = useNavigate();
  const [contractTypes, setContractTypes] = useState([]);
  const [search, setSearch] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    contractTypeApi.getAllContractTypes()
      .then((response) => {
        if (isMounted) {
          setContractTypes(Array.isArray(response.data?.data) ? response.data.data : []);
        }
      })
      .catch((requestError) => {
        if (isMounted) {
          setContractTypes([]);
          setError(requestError.response?.data?.message || "Unable to load contract types.");
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredContractTypes = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    if (!normalizedSearch) {
      return contractTypes;
    }

    return contractTypes.filter((item) => (
      `${getContractTypeCode(item)} ${getContractTypeName(item)} ${item.description || ""}`
        .toLowerCase()
        .includes(normalizedSearch)
    ));
  }, [contractTypes, search]);

  return (
    <div className="contract-type-create-page">
      <div className="contract-type-layout">
        <aside className="contract-type-sidebar">
          <p className="contract-type-sidebar-title">Template Management</p>
          <Nav className="contract-type-sidebar-nav">
            <Nav.Link active className="contract-type-sidebar-link" onClick={() => navigate("/contract-types")}><IconFileText size={22} stroke={1.8} /><span>Contract Types</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link" onClick={() => navigate("/contract-types/new")}><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Type</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconFileDescription size={22} stroke={1.8} /><span>Contract Type Details</span></Nav.Link>
            <div className="contract-type-sidebar-divider" />
            <Nav.Link className="contract-type-sidebar-link"><IconFileText size={22} stroke={1.8} /><span>Contract Templates</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconCirclePlus size={22} stroke={1.8} /><span>Create Contract Template</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconFileDescription size={22} stroke={1.8} /><span>Template Details</span></Nav.Link>
            <Nav.Link className="contract-type-sidebar-link"><IconPencil size={22} stroke={1.8} /><span>Template Editor</span></Nav.Link>
          </Nav>
        </aside>

        <main className="contract-type-main">
          <div className="contract-types-topbar">
            <div>
              <h1>Contract Types</h1>
              <p className="contract-types-subtitle">Manage all contract types used in contracts.</p>
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
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search contract types..."
                  aria-label="Search contract types"
                />
                <button type="button" className="btn btn-outline-secondary">Search</button>
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
                  {isLoading ? (
                    <tr><td colSpan={5} className="text-center py-5">Loading contract types...</td></tr>
                  ) : error ? (
                    <tr><td colSpan={5} className="text-center text-danger py-5">{error}</td></tr>
                  ) : filteredContractTypes.length === 0 ? (
                    <tr><td colSpan={5} className="text-center py-5">No contract types found.</td></tr>
                  ) : filteredContractTypes.map((item) => {
                    const code = getContractTypeCode(item);
                    const name = getContractTypeName(item);
                    const status = item.contractTypeStatus || item.status || "Inactive";

                    return (
                      <tr key={item.id ?? code}>
                        <td>{code}</td>
                        <td>{name}</td>
                        <td>{item.description || "-"}</td>
                        <td><span className={`badge ${status === "Active" ? "bg-success" : "bg-secondary"}`}>{status}</span></td>
                        <td>
                          <button
                            type="button"
                            className="btn btn-sm btn-light"
                            onClick={() => navigate(`/contract-types/detail/${item.id}`)}
                            disabled={item.id == null}
                            aria-label={`View ${name}`}
                          >
                            View
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default ListContractType;
