import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
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
    createEmptyContract,
    getApiErrorMessage,
    loadProjectOptions,
    toContractRequest,
    unwrapApiResponse,
    validateContract,
} from "./contractUtils.js";

function CreateContract() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const formRef = useRef(null);
    const [contract, setContract] = useState(() =>
        createEmptyContract(searchParams.get("projectId") || "")
    );
    const [projects, setProjects] = useState([]);
    const [loadingProjects, setLoadingProjects] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        let active = true;

        const loadProjects = async () => {
            try {
                const projectItems = await loadProjectOptions();

                if (active) {
                    setProjects(projectItems);
                }
            } catch (error) {
                if (active) {
                    setErrorMessage(
                        getApiErrorMessage(error, "Unable to load projects.")
                    );
                }
            } finally {
                if (active) {
                    setLoadingProjects(false);
                }
            }
        };

        loadProjects();

        return () => {
            active = false;
        };
    }, []);

    const handleChange = (event) => {
        const { name, value, type, checked } = event.target;
        const nextValue = type === "checkbox" ? checked : value;
        setContract((current) => {
            if (name.startsWith("attributeValues.")) {
                const attributeKey = name.slice("attributeValues.".length);
                return {
                    ...current,
                    attributeValues: {
                        ...(current.attributeValues || {}),
                        [attributeKey]: nextValue,
                    },
                };
            }
            return { ...current, [name]: nextValue };
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (submitting) {
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
            const response = await contractApi.createContract(
                toContractRequest(contract, true)
            );
            const createdContract = unwrapApiResponse(response);

            navigate(
                createdContract?.id
                    ? `/contract-management/view/${createdContract.id}`
                    : "/contract-management/list"
            );
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(error, "Unable to create the contract.")
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <PagePanel
            title="Create Contract"
            description="Create a contract and link it to an existing project."
            action={
                <div style={styles.actions}>
                    <CancelButton
                        onClick={() => navigate("/contract-management/list")}
                    />

                    <PrimaryButton
                        disabled={submitting || (!loadingProjects && projects.length === 0)}
                        onClick={() => {
                            if (!submitting) {
                                formRef.current?.requestSubmit();
                            }
                        }}
                    >
                        <Icon name="save" size={19} color="#fff" />
                        {submitting ? "Saving..." : "Save Contract"}
                    </PrimaryButton>
                </div>
            }
        >
            <form ref={formRef} onSubmit={handleSubmit}>
                <ContractForm
                    contract={contract}
                    onChange={handleChange}
                    projects={projects}
                    loadingProjects={loadingProjects}
                    creatorReadOnly={Boolean(localStorage.getItem("fullName"))}
                />

                {errorMessage ? (
                    <div role="alert" style={localStyles.errorAlert}>
                        {errorMessage}
                    </div>
                ) : (
                    <InfoAlert>
                        {projects.length === 0 && !loadingProjects
                            ? "You need CREATE_CONTRACTS permission in a project before creating a contract."
                            : "Only projects where you have CREATE_CONTRACTS permission are shown."}
                    </InfoAlert>
                )}
            </form>
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

export default CreateContract;
