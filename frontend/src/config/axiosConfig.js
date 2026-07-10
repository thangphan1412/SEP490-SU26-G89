import axios from "axios";

const PROJECT_API_BASE_URL = "http://localhost:8080/api/projects";
const PERMISSION_API_BASE_URL = "http://localhost:8080/api/permissions";

export const listProjects = (params) => {
    return axios.get(PROJECT_API_BASE_URL + "/list", {
        params: params,
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const viewProject = (projectId) => {
    return axios.get(PROJECT_API_BASE_URL + "/view" + "/" + projectId, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const listProjectEmployees = () => {
    return axios.get(PROJECT_API_BASE_URL + "/employees", {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const createProject = (project) => {
    return axios.post(PROJECT_API_BASE_URL + "/create", project);
};

export const listPermissions = (params) => {
    return axios.get(PERMISSION_API_BASE_URL + "/list", {
        params: params,
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const viewPermission = (permissionId) => {
    return axios.get(PERMISSION_API_BASE_URL + "/view/" + permissionId, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const listPermissionProjects = () => {
    return axios.get(PERMISSION_API_BASE_URL + "/projects", {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const createPermission = (permission) => {
    return axios.post(PERMISSION_API_BASE_URL + "/create", permission);
};

export const updatePermission = (permissionId, permission) => {
    return axios.put(PERMISSION_API_BASE_URL + "/update/" + permissionId, permission);
};

export const deletePermission = (permissionId) => {
    return axios.delete(PERMISSION_API_BASE_URL + "/delete/" + permissionId);
};
