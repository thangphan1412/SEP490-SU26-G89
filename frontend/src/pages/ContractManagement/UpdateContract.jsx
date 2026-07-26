import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import {
    CancelButton,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
    styles,
} from "./ContractComponents.jsx";
import ContractForm from "./ContractForm.jsx";
import {
    getApiErrorMessage,
    loadProjectOptions,
    mapContractToForm,
    toContractRequest,
    unwrapApiResponse,
    validateContract,
} from "./contractUtils.js";

function UpdateContract() {
    const navigate = useNavigate();
    const { id } = useParams();
    const formRef = useRef(null);
    const [contract, setContract] = useState(null);
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        let active = true;

        const loadContract = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const [contractResponse, projectItems] = await Promise.all([
                    contractApi.getContractById(id),
                    loadProjectOptions(),
                ]);

                if (active) {
                    setContract(
                        mapContractToForm(unwrapApiResponse(contractResponse))
                    );
                    setProjects(projectItems);
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

    const handleChange = (event) => {
        const { name, value } = event.target;
        setContract((current) => ({ ...current, [name]: value }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (submitting || !contract) {
            return;
        }

        const validationMessage = validateContract(contract);

        if (validationMessage) {
            setErrorMessage(validationMessage);
            return;
        }

        setSubmitting(true);
        setErrorMessage("");

        try {
            await contractApi.updateContract(
                id,
                toContractRequest(contract, false)
            );
            navigate(`/contract-management/view/${id}`);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(error, "Unable to update the contract.")
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <PagePanel
            title="Update Contract"
            description="Update the contract information stored by the backend."
            action={
                <div style={styles.actions}>
                    <CancelButton
                        onClick={() =>
                            navigate(`/contract-management/view/${id}`)
                        }
                    />

                    <PrimaryButton
                        onClick={() => {
                            if (!submitting && contract) {
                                formRef.current?.requestSubmit();
                            }
                        }}
                    >
                        <Icon name="save" size={19} color="#fff" />
                        {submitting ? "Saving..." : "Save Changes"}
                    </PrimaryButton>
                </div>
            }
        >
            {loading ? (
                <section style={styles.card}>Loading contract...</section>
            ) : contract ? (
                <form ref={formRef} onSubmit={handleSubmit}>
                    <ContractForm
                        contract={contract}
                        onChange={handleChange}
                        projects={projects}
                        creatorReadOnly
                    />

                    {errorMessage ? (
                        <div role="alert" style={localStyles.errorAlert}>
                            {errorMessage}
                        </div>
                    ) : (
                        <InfoAlert>
                            Saving sends the changes to the Contract update API.
                        </InfoAlert>
                    )}
                </form>
            ) : (
                <div role="alert" style={localStyles.errorAlert}>
                    {errorMessage || "Contract not found."}
                </div>
            )}
        </PagePanel>
    );
}

const localStyles = {
    errorAlert: {
        margin: "0 28px 24px",
        border: "1px solid #fecaca",
        background: "#fef2f2",
        color: "#b91c1c",
        borderRadius: 7,
        padding: "12px 16px",
    },
};

export default UpdateContract;
