import axiosClient from "../../config/api/axiosClient.js";

const CONTRACT_TEMPLATE_ENDPOINT =
    "contract-templates";
const noCacheConfig = {
    headers: {
        "Cache-Control": "no-cache",
    },
};

const contractTemplateApi = {
    getAllContractTemplates(params = {}) {
        return axiosClient.get(CONTRACT_TEMPLATE_ENDPOINT, {
            ...noCacheConfig,
            params,
        });
    },

    getContractTemplateById(id) {
        return axiosClient.get(
            CONTRACT_TEMPLATE_ENDPOINT + "/" + id,
            noCacheConfig
        );
    },

    createContractTemplate(data) {
        return axiosClient.post(CONTRACT_TEMPLATE_ENDPOINT, data);
    },

    updateContractTemplate(id, data) {
        return axiosClient.put(CONTRACT_TEMPLATE_ENDPOINT + "/" + id, data);
    },

    deleteContractTemplate(id) {
        return axiosClient.delete(CONTRACT_TEMPLATE_ENDPOINT + "/" + id);
    },

    createContractTemplateVersion(id, data) {
        return axiosClient.post(
            CONTRACT_TEMPLATE_ENDPOINT + "/" + id + "/versions",
            data
        );
    },
};

export default contractTemplateApi;
