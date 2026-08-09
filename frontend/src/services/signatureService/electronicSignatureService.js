import axiosClient from "../../config/api/axiosClient.js";

const electronicSignatureService ={
    getAllElectronicSignature(data){
        return axiosClient.get("list-electronic-signatures", data)
    },
    getElectronicSignatureById(id) {
        return axiosClient.get(`electronic-by/${id}`);
    },

    createElectronicSignature(formData) {
    return axiosClient.post(
        "create/electronic-signatures",
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
},
}
export default electronicSignatureService