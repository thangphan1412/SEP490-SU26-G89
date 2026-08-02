import axios from "axios";

const CONTRACT_TEMPLATE_ENDPOINT =
    "http://localhost:8080/api/v1/contract-templates";
const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};

const contractTemplateApi = {
    getAllContractTemplates(params = {}) {
        return axios.get(CONTRACT_TEMPLATE_ENDPOINT, {
            ...noCacheConfig,
            params,
        });
    },

    getContractTemplateById(id) {
        return axios.get(
            CONTRACT_TEMPLATE_ENDPOINT + "/" + id,
            noCacheConfig
        );
    },

    createContractTemplate(data) {
        return axios.post(CONTRACT_TEMPLATE_ENDPOINT, data);
    },

    updateContractTemplate(id, data) {
        return axios.put(CONTRACT_TEMPLATE_ENDPOINT + "/" + id, data);
    },

    deleteContractTemplate(id) {
        return axios.delete(CONTRACT_TEMPLATE_ENDPOINT + "/" + id);
    },

    createContractTemplateVersion(id, data) {
        return axios.post(
            CONTRACT_TEMPLATE_ENDPOINT + "/" + id + "/versions",
            data
        );
    },
};

export default contractTemplateApi;
