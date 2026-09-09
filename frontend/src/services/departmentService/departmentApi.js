import axiosClient from "../../config/api/axiosClient.js";

const DEPARTMENT_ENDPOINT = "/departments";

const departmentApi = {
    getAllDepartments() {
        return axiosClient.get(DEPARTMENT_ENDPOINT);
    },

    searchDepartments(params = {}, signal) {
        return axiosClient.get(DEPARTMENT_ENDPOINT + "/list", { params, signal });
    },

    getDepartmentById(id, signal) {
        return axiosClient.get(DEPARTMENT_ENDPOINT + "/" + id, { signal });
    },

    createDepartment(data) {
        return axiosClient.post(DEPARTMENT_ENDPOINT, data);
    },

    updateDepartment(id, data) {
        return axiosClient.put(DEPARTMENT_ENDPOINT + "/" + id, data);
    },
};

export default departmentApi;
