import axiosClient from "../../config/api/axiosClient.js";

const CONTRACT_TYPE_ENDPOINT = "contract-types";
const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};

const contractTypeApi = {
    getAllContractTypes() {
        return axiosClient.get(CONTRACT_TYPE_ENDPOINT, noCacheConfig);
    },

    getContractTypeById(id) {
        return axiosClient.get(CONTRACT_TYPE_ENDPOINT + "/" + id, noCacheConfig);
    },

    getWorkflowOptions() {
        return axiosClient.get(
            CONTRACT_TYPE_ENDPOINT + "/workflow-options",
            noCacheConfig
        );
    },

    createContractType(data) {
        return axiosClient.post(CONTRACT_TYPE_ENDPOINT, data);
    },

    updateContractType(id, data) {
        return axiosClient.put(CONTRACT_TYPE_ENDPOINT + "/" + id, data);
    },

    deleteContractType(id) {
        return axiosClient.delete(CONTRACT_TYPE_ENDPOINT + "/" + id);
    },
};

export default contractTypeApi;
