import axiosClient from "../../config/api/axiosClient.js";

const electronicSignatureService = {
    createUpdateChallenge(id, keyCode) {
        return axiosClient.post(
            `/electronic-signatures/${id}/update-challenge`,
            { keyCode }
        );
    },

    getAllElectronicSignature(
        search = "",
        type = "All",
        status = "All"
    ) {
        const params = {};

        if (search && search.trim() !== "") {
            params.search = search.trim();
        }

        if (type && type !== "All") {
            params.type = type;
        }

        if (status && status !== "All") {
            params.status = status;
        }

        return axiosClient.get(
            "list-electronic-signatures",
            { params }
        );
    },

    getElectronicSignatureById(id) {
        return axiosClient.get(
            `electronic-by/${id}`
        );
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