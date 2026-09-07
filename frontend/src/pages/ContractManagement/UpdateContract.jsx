import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import contractTemplateApi from "../../services/contractTemplateService/contractTemplateApi.js";
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
    CONTRACT_PROJECT_ACTION,
    canManageNewContract,
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
    const [contractTypes, setContractTypes] = useState([]);
    const [contractTemplates, setContractTemplates] = useState([]);
    const [projectContext, setProjectContext] = useState(null);
    const [loadingProjectContext, setLoadingProjectContext] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        let active = true;

        const loadContract = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const [
                    contractResponse,
                    projectItems,
                    typeResponse,
                    templateResponse,
                ] = await Promise.all([
                    contractApi.getContractById(id),
                    loadProjectOptions(),
                    contractTypeApi.getAllContractTypes(),
                    contractTemplateApi.getAllContractTemplates(),
                ]);

                if (active) {
                    const contractPayload = unwrapApiResponse(contractResponse);
                    const typeItems = unwrapApiResponse(typeResponse);
                    const templateItems = unwrapApiResponse(templateResponse);
                    const availableTypes = Array.isArray(typeItems)
                        ? typeItems
                        : [];
                    if (!canManageNewContract(
                        contractPayload,
                        CONTRACT_PROJECT_ACTION.EDIT
                    )) {
                        setContract(null);
                        setErrorMessage(
                            "You do not have permission to edit this NEW contract."
                        );
                    } else {
                        const selectedType = availableTypes.find(
                            (item) => item.id === contractPayload.contractTypeId
                        );
                        setContract(mapContractToForm(
                            contractPayload,
                            selectedType?.activeWorkflow || null
                        ));
                    }
                    setProjects(projectItems);
                    setContractTypes(availableTypes);
                    setContractTemplates(
                        Array.isArray(templateItems) ? templateItems : []
                    );
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

    const selectedProjectId = contract?.projectId;
    const contractReady = contract !== null;

    useEffect(() => {
        if (!contractReady) {
            return undefined;
        }

        let active = true;
        const loadContext = async () => {
            setLoadingProjectContext(true);
            try {
                const response = selectedProjectId
                    ? await contractApi.getProjectContext(selectedProjectId)
                    : await contractApi.getStandaloneContext();
                if (active) {
                    setProjectContext(unwrapApiResponse(response) || null);
                }
            } catch (error) {
                if (active) {
                    setProjectContext(null);
                    setErrorMessage(getApiErrorMessage(
                        error,
                        selectedProjectId
                            ? "Unable to load project phases, tasks and members."
                            : "Unable to load standalone contract users."
                    ));
                }
            } finally {
                if (active) {
                    setLoadingProjectContext(false);
                }
            }
        };

        loadContext();
        return () => {
            active = false;
        };
    }, [contractReady, selectedProjectId]);

    const handleChange = (event) => {
        const { name, value, type, checked } = event.target;
        const nextValue = type === "checkbox" ? checked : value;

        if (name === "projectId") {
            setProjectContext(null);
            setLoadingProjectContext(false);
        }

        setContract((current) => {
            if (name === "projectId") {
                return {
                    ...current,
                    projectId: value,
                    projectName: "",
                    phaseId: "",
                    taskId: "",
                    workflowAssignees: [],
                };
            }

            if (name === "phaseId") {
                return {
                    ...current,
                    phaseId: value,
                    taskId: "",
                };
            }

            if (name === "contractTypeId") {
                const selectedType = contractTypes.find(
                    (item) => item.id === value
                );
                return {
                    ...current,
                    contractTypeId: value,
                    contractTemplateId: "",
                    contractTemplateVersionId: "",
                    contractContent: "",
                    contractLayoutJson: "",
                    attributeValues: {},
                    saveAsTemplateVersion: false,
                    templateVersionName: "",
                    templateVersionNote: "",
                    workflowDefinition: selectedType?.activeWorkflow || null,
                    workflowAssignees: [],
                };
            }

            if (name.startsWith("workflowAssignee.")) {
                const workflowStepId = name.slice("workflowAssignee.".length);
                const remainingAssignments = (current.workflowAssignees || [])
                    .filter((item) => item.workflowStepId !== workflowStepId);
                return {
                    ...current,
                    workflowAssignees: value
                        ? [
                            ...remainingAssignments,
                            { workflowStepId, userId: value },
                        ]
                        : remainingAssignments,
                };
            }

            if (name === "contractTemplateId") {
                const template = contractTemplates.find(
                    (item) => item.id === value
                );
                const latestVersion = Array.isArray(template?.versions)
                    ? template.versions[0]
                    : null;
                return {
                    ...current,
                    contractTemplateId: value,
                    contractTemplateVersionId: latestVersion?.id || "",
                    contractContent: latestVersion?.templateContent || "",
                    contractLayoutJson: latestVersion?.layoutJson || "",
                    attributeValues: {},
                    saveAsTemplateVersion: false,
                    templateVersionName: "",
                    templateVersionNote: "",
                };
            }

            if (name === "contractTemplateVersionId") {
                const template = contractTemplates.find(
                    (item) => item.id === current.contractTemplateId
                );
                const version = template?.versions?.find(
                    (item) => item.id === value
                );
                return {
                    ...current,
                    contractTemplateVersionId: value,
                    contractContent: version?.templateContent || "",
                    contractLayoutJson: version?.layoutJson || "",
                    attributeValues: {},
                };
            }

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
                        contractTypes={contractTypes}
                        contractTemplates={contractTemplates}
                        projectContext={projectContext}
                        loadingProjectContext={loadingProjectContext}
                        loadingContractOptions={loading}
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
