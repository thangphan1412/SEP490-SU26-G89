import axiosClient from "../../config/api/axiosClient.js";

const CONTRACT_ENDPOINT = "/contracts";

const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};
const preparePadesSigning = async (
    contractId,
    electronicSignatureId,
    keyCode,
    pdfBlob,
    pageNumber,
    positionX,
    positionY,
    signatureWidth,
    signatureHeight
) => {

    const formData = new FormData();

    formData.append(
        "electronicSignatureId",
        electronicSignatureId
    );

    formData.append(
        "keyCode",
        keyCode
    );

    formData.append(
        "file",
        pdfBlob,
        "contract.pdf"
    );

    // =========================
    // SIGNATURE POSITION
    // =========================

    formData.append(
        "pageNumber",
        String(pageNumber)
    );

    formData.append(
        "positionX",
        String(positionX)
    );

    formData.append(
        "positionY",
        String(positionY)
    );

    formData.append(
        "signatureWidth",
        String(signatureWidth)
    );

    formData.append(
        "signatureHeight",
        String(signatureHeight)
    );

    return axiosClient.post(
        `/contracts/${contractId}/sign/prepare`,
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
};

const completePadesSigning = async (
    contractId,
    sessionId,
    signatureValue
) => {

    return axiosClient.post(
        `/contracts/${contractId}/sign/complete`,
        {
            sessionId,
            signatureValue,
        }
    );
};

const contractApi = {
    getAllContracts(params) {
        return axiosClient.get(CONTRACT_ENDPOINT + "/list", {
            ...noCacheConfig,
            params,
        });
    },

    getProjectOptions() {
        return axiosClient.get(
            CONTRACT_ENDPOINT + "/project-options",
            noCacheConfig
        );
    },

    getProjectContext(projectId) {
        return axiosClient.get(
            CONTRACT_ENDPOINT + "/project-options/" + projectId + "/context",
            noCacheConfig
        );
    },

    getStandaloneContext() {
        return axiosClient.get(
            CONTRACT_ENDPOINT + "/standalone-context",
            noCacheConfig
        );
    },

    getContractById(id) {
        return axiosClient.get(
            CONTRACT_ENDPOINT + "/" + id,
            noCacheConfig
        );
    },

    createContract(data) {
        return axiosClient.post(
            CONTRACT_ENDPOINT,
            data
        );
    },

    updateContract(id, data) {
        return axiosClient.put(
            CONTRACT_ENDPOINT + "/" + id,
            data
        );
    },

    transitionContract(id, data) {
        return axiosClient.post(
            CONTRACT_ENDPOINT + "/" + id + "/transitions",
            data
        );
    },

    signContract(id, electronicSignatureId, signatureValue, publicKeyCode) {
        return this.transitionContract(id, {
            action: "COMPLETE_STEP",
            actorName:
                localStorage.getItem("fullName") ||
                localStorage.getItem("email"),
            actorRole:
                localStorage.getItem("role") ||
                localStorage.getItem("roleName"),
            comment: null,
            electronicSignatureId,
            signatureValue,
            keyCode: publicKeyCode
        });
    },
    exportContractPdf(id) {
        return axiosClient.get(
            CONTRACT_ENDPOINT + "/" + id + "/pdf",
            {
                responseType: "blob",
                headers: {
                    "Cache-Control": "no-cache",
                },
            }
        );
    },

    deleteContract(id) {
        return axiosClient.delete(
            CONTRACT_ENDPOINT + "/" + id
        );
    },

    // =====================================================
    // PADES
    // =====================================================

    preparePadesSigning,

    completePadesSigning,
};

export default contractApi;