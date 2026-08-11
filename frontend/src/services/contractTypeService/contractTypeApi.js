import axios from "axios";

const CONTRACT_TYPE_ENDPOINT = "http://localhost:8080/api/v1/contract-types";
const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};

const contractTypeApi = {
    getAllContractTypes() {
        return axios.get(CONTRACT_TYPE_ENDPOINT, noCacheConfig);
    },

    getContractTypeById(id) {
        return axios.get(CONTRACT_TYPE_ENDPOINT + "/" + id, noCacheConfig);
    },

    getWorkflowOptions() {
        return axios.get(
            CONTRACT_TYPE_ENDPOINT + "/workflow-options",
            noCacheConfig
        );
    },

    createContractType(data) {
        return axios.post(CONTRACT_TYPE_ENDPOINT, data);
    },

    updateContractType(id, data) {
        return axios.put(CONTRACT_TYPE_ENDPOINT + "/" + id, data);
    },

    deleteContractType(id) {
        return axios.delete(CONTRACT_TYPE_ENDPOINT + "/" + id);
    },
};

export default contractTypeApi;
