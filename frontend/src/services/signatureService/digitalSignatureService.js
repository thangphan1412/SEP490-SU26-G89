import axiosClient from "../../config/api/axiosClient.js";

const digitalSignatureService = {
    getMyPublicKey() {
        return axiosClient.get("/signature/keys/me", {
            headers: { "Cache-Control": "no-cache" },
        });
    },

    verify(signatureId, file) {
        const formData = new FormData();
        formData.append("file", file);
        return axiosClient.post(`/signature/${signatureId}/verify`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
        });
    },

    verifyStored(signatureId) {
        return axiosClient.get(`/signature/${signatureId}/verify-stored`);
    },
};

export default digitalSignatureService;
