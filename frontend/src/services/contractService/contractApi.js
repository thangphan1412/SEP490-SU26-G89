import axios from "axios";

const CONTRACT_ENDPOINT = "http://localhost:8080/api/v1/contracts";

const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};

const contractApi = {
    getAllContracts(params) {
        return axios.get(CONTRACT_ENDPOINT + "/list", {
            ...noCacheConfig,
            params,
        });
    },

    getProjectOptions() {
        return axios.get(
            CONTRACT_ENDPOINT + "/project-options",
            noCacheConfig
        );
    },

    getContractById(id) {
        return axios.get(CONTRACT_ENDPOINT + "/" + id, noCacheConfig);
    },

    createContract(data) {
        return axios.post(CONTRACT_ENDPOINT, data);
    },

    updateContract(id, data) {
        return axios.put(CONTRACT_ENDPOINT + "/" + id, data);
    },

    transitionContract(id, data) {
        return axios.post(
            CONTRACT_ENDPOINT + "/" + id + "/transitions",
            data
        );
    },

    deleteContract(id, actor) {
        return axios.delete(CONTRACT_ENDPOINT + "/" + id, {
            params: actor,
        });
    },
};

export default contractApi;
