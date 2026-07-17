import axios from "axios";

const DEPARTMENT_API_BASE_URL = "http://localhost:8080/api/departments";

export const getAllDepartments = () => {
    return axios.get(DEPARTMENT_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const getDepartmentById = (id) => {
    return axios.get(DEPARTMENT_API_BASE_URL + "/" + id, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const createDepartment = (departmentData) => {
    return axios.post(DEPARTMENT_API_BASE_URL, departmentData, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const updateDepartment = (id, departmentData) => {
    return axios.put(DEPARTMENT_API_BASE_URL + "/" + id, departmentData, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};
