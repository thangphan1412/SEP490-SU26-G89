import axiosClient from "../../config/api/axiosClient.js";

const CONTRACT_ENDPOINT = "/contracts";

const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
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
        return axiosClient.get(CONTRACT_ENDPOINT + "/" + id, noCacheConfig);
    },

    createContract(data) {
        return axiosClient.post(CONTRACT_ENDPOINT, data);
    },

    updateContract(id, data) {
        return axiosClient.put(CONTRACT_ENDPOINT + "/" + id, data);
    },

    transitionContract(id, data) {
        return axiosClient.post(
            CONTRACT_ENDPOINT + "/" + id + "/transitions",
            data
        );
    },

    signContract(id, electronicSignatureId, comment = null) {
        return this.transitionContract(id, {
            action: "COMPLETE_STEP",
            actorName: localStorage.getItem("fullName") || localStorage.getItem("email"),
            actorRole: localStorage.getItem("role") || localStorage.getItem("roleName"),
            comment,
            electronicSignatureId,
        });
    },

    exportContractPdf(id) {
        return axiosClient.get(CONTRACT_ENDPOINT + "/" + id + "/pdf", {
            responseType: "blob",
            headers: {
                "Cache-Control": "no-cache",
            },
        });
    },

    deleteContract(id) {
        return axiosClient.delete(CONTRACT_ENDPOINT + "/" + id);
    },
};

export default contractApi;
