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
