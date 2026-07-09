import axios from "axios";

const PROJECT_API_BASE_URL = "http://localhost:8080/api/projects";

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
