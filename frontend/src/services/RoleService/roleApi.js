import axiosClient from "../../config/api/axiosClient.js";

const ROLE_ENDPOINT = "/roles";

const roleApi = {
    getAllRoles() {
        return axiosClient.get(ROLE_ENDPOINT);
    },

    searchRoles(params = {}) {
        return axiosClient.get(ROLE_ENDPOINT + "/list", { params });
    },

    getRoleById(id) {
        return axiosClient.get(ROLE_ENDPOINT + "/" + id);
    },

    getSystemRoleByCode(roleCode) {
        return axiosClient.get(
            ROLE_ENDPOINT + "/system/" + encodeURIComponent(roleCode)
        );
    },

    createRole(data) {
        return axiosClient.post(ROLE_ENDPOINT, data);
    },

    updateRole(id, data) {
        return axiosClient.put(ROLE_ENDPOINT + "/" + id, data);
    },

    deleteRole(id) {
        return axiosClient.delete(ROLE_ENDPOINT + "/" + id);
    },
};

export default roleApi;
