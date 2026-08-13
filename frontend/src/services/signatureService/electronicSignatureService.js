import axiosClient from "../../config/api/axiosClient.js";

const electronicSignatureService = {

    getAllElectronicSignature() {
        return axiosClient.get("list-electronic-signatures");
    },

    getElectronicSignatureById(id) {
        return axiosClient.get(`electronic-by/${id}`);
    },

    createElectronicSignature(data) {
        return axiosClient.post(
            "create/electronic-signatures",
            data
        );
    },

    updateElectronicSignature(id, data) {
        return axiosClient.put(
            `update/electronic-by/${id}`,
            data
        );
    }
};

export default electronicSignatureService;