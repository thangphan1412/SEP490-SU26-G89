import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import {
    ContractStatusBadge,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
    styles,
} from "./ContractComponents.jsx";
import {
    CONTRACT_PROJECT_ACTION,
    canManageNewContract,
    formatContractDate,
    formatContractDateTime,
    formatContractStatus,
    getApiErrorMessage,
    unwrapApiResponse,
} from "./contractUtils.js";

function ViewContract() {
    const navigate = useNavigate();
    const { id } = useParams();
    const [contract, setContract] = useState(null);
    const [loading, setLoading] = useState(true);
    const [deleting, setDeleting] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        let active = true;

        const loadContract = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const response = await contractApi.getContractById(id);

                if (active) {
                    setContract(unwrapApiResponse(response));
                }
            } catch (error) {
                if (active) {
                    setErrorMessage(
                        getApiErrorMessage(error, "Unable to load the contract.")
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadContract();

        return () => {
            active = false;
        };
    }, [id]);

    const handleDelete = async () => {
        const confirmed = window.confirm(
            `Delete contract ${contract?.contractNumber || id}?`
        );

        if (!confirmed) {
            return;
        }

        setDeleting(true);
        setErrorMessage("");

        try {
            await contractApi.deleteContract(id);
            navigate("/contract-management/list");
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(error, "Unable to delete the contract.")
            );
            setDeleting(false);
        }
    };

    const canDelete = canManageNewContract(
        contract,
        CONTRACT_PROJECT_ACTION.DELETE
    );
    const canEdit = canManageNewContract(
        contract,
        CONTRACT_PROJECT_ACTION.EDIT
    );
    const action = contract && (canDelete || canEdit) ? (
        <div style={styles.actions}>
            {canDelete && (
                <button
                    type="button"
                    style={localStyles.deleteButton}
                    onClick={handleDelete}
                    disabled={deleting}
                >
                    {deleting ? "Deleting..." : "Delete"}
                </button>
            )}

            {canEdit && (
                <PrimaryButton
                    onClick={() =>
                        navigate(`/contract-management/update/${contract.id}`)
                    }
                >
                    <Icon name="edit" size={20} color="#ffffff" />
                    Edit Contract
                </PrimaryButton>
            )}
        </div>
    ) : null;

    return (
        <PagePanel
            title="Contract Details"
            description="Review contract information loaded from the backend."
            action={action}
        >
            {loading ? (
                <section style={styles.card}>Loading contract...</section>
            ) : contract ? (
                <>
                    <section style={styles.card}>
                        <h2 style={styles.cardTitle}>Contract Overview</h2>

                        <div style={localStyles.overviewGrid}>
                            <div style={localStyles.detailColumn}>
                                <DetailRow
                                    icon="document"
                                    label="Contract ID"
                                    value={contract.contractNumber || "-"}
                                />
                                <DetailRow
                                    icon="document"
                                    label="Title"
                                    value={contract.contractTitle || "-"}
                                />
                                <DetailRow
                                    icon="shield"
                                    label="Status"
                                    value={
                                        <ContractStatusBadge
                                            status={formatContractStatus(
                                                contract.contractStatus
                                            )}
                                        />
                                    }
                                />
                                <DetailRow
                                    icon="link"
                                    label="Project"
                                    value={contract.projectName || "Not linked"}
                                />
                            </div>

                            <div style={localStyles.detailColumn}>
                                <DetailRow
                                    icon="calendar"
                                    label="Effective Date"
                                    value={formatContractDate(
                                        contract.effectiveDate
                                    )}
                                />
                                <DetailRow
                                    icon="calendar"
                                    label="Expiration Date"
                                    value={formatContractDate(
                                        contract.expirationDate
                                    )}
                                />
                                <DetailRow
                                    icon="users"
                                    label="Created By"
                                    value={contract.contractCreatedBy || "-"}
                                />
                                <DetailRow
                                    icon="calendar"
                                    label="Created At"
                                    value={formatContractDateTime(
                                        contract.contractCreatedAt
                                    )}
                                />
                            </div>

                            <div style={localStyles.summaryColumn}>
                                <p style={localStyles.summaryLabel}>Database ID</p>
                                <p style={localStyles.identifier}>{contract.id}</p>

                                <p style={localStyles.summaryLabel}>Project ID</p>
                                <p style={localStyles.identifier}>
                                    {contract.projectId || "-"}
                                </p>
                            </div>
                        </div>
                    </section>

                    {errorMessage ? (
                        <div role="alert" style={localStyles.errorAlert}>
                            {errorMessage}
                        </div>
                    ) : (
                        <InfoAlert>
                            This information was loaded from the Contract detail API.
                        </InfoAlert>
                    )}
                </>
            ) : (
                <div role="alert" style={localStyles.errorAlert}>
                    {errorMessage || "Contract not found."}
                </div>
            )}
        </PagePanel>
    );
}

function DetailRow({ icon, label, value }) {
    return (
        <div style={localStyles.detailRow}>
            <Icon name={icon} size={20} />
            <span style={localStyles.detailLabel}>{label}</span>
            <span style={localStyles.detailValue}>{value}</span>
        </div>
    );
}

const localStyles = {
    overviewGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
        gap: 28,
    },
    detailColumn: {
        borderRight: "1px solid #e3e9f2",
        paddingRight: 28,
    },
    detailRow: {
        minHeight: 48,
        display: "grid",
        gridTemplateColumns: "28px minmax(100px, 124px) 1fr",
        alignItems: "center",
        gap: 10,
        color: "#243452",
        fontSize: 13,
    },
    detailLabel: { color: "#52617f", fontWeight: 700 },
    detailValue: {
        color: "#243452",
        fontWeight: 700,
        overflowWrap: "anywhere",
    },
    summaryColumn: { paddingLeft: 4 },
    summaryLabel: {
        margin: "0 0 6px",
        color: "#52617f",
        fontSize: 13,
        fontWeight: 800,
    },
    identifier: {
        margin: "0 0 18px",
        color: "#243452",
        lineHeight: 1.55,
        fontSize: 13,
        overflowWrap: "anywhere",
    },
    deleteButton: {
        minHeight: 46,
        borderRadius: 7,
        border: "1px solid #fecaca",
        background: "#fff",
        color: "#b91c1c",
        padding: "0 18px",
        fontSize: 15,
        fontWeight: 800,
        cursor: "pointer",
    },
    errorAlert: {
        margin: "22px 28px 24px",
        border: "1px solid #fecaca",
        background: "#fef2f2",
        color: "#b91c1c",
        borderRadius: 7,
        padding: "12px 16px",
    },
};

export default ViewContract;
