import axiosClient from "../../config/api/axiosClient.js";

const DEPARTMENT_ENDPOINT = "/departments";

const departmentApi = {
    getAllDepartments() {
        return axiosClient.get(DEPARTMENT_ENDPOINT);
    },

    searchDepartments(params = {}) {
        return axiosClient.get(DEPARTMENT_ENDPOINT + "/list", { params });
    },

    getDepartmentById(id) {
        return axiosClient.get(DEPARTMENT_ENDPOINT + "/" + id);
    },

    createDepartment(data) {
        return axiosClient.post(DEPARTMENT_ENDPOINT, data);
    },

    updateDepartment(id, data) {
        return axiosClient.put(DEPARTMENT_ENDPOINT + "/" + id, data);
    },
};

export default departmentApi;
