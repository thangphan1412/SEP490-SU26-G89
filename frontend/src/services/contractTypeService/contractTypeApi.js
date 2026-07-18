import axiosClient from "../../config/api/axiosClient.js";

const CONTRACT_TYPE_ENDPOINT = "/contract-types";

const contractTypeApi = {
    getAllContractTypes() {
        return axiosClient.get(CONTRACT_TYPE_ENDPOINT);
    },

    getContractTypeById(id) {
        return axiosClient.get(CONTRACT_TYPE_ENDPOINT + "/" + id);
    },

    createContractType(data) {
        return axiosClient.post(CONTRACT_TYPE_ENDPOINT, data);
    },

    updateContractType(id, data) {
        return axiosClient.put(CONTRACT_TYPE_ENDPOINT + "/" + id, data);
    },
};

export default contractTypeApi;
