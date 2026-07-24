import axios from "axios";

const PROJECT_API_BASE_URL = "http://localhost:8080/api/projects";

const noCacheConfig = {
  headers: {
    "Cache-Control": "no-cache",
  },
};

export const listProjects = (params) => {
  return axios.get(`${PROJECT_API_BASE_URL}/list`, {
    ...noCacheConfig,
    params,
  });
};

export const viewProject = (projectId) => {
  return axios.get(`${PROJECT_API_BASE_URL}/view/${projectId}`, noCacheConfig);
};

export const listProjectEmployees = () => {
  return axios.get(`${PROJECT_API_BASE_URL}/employees`, noCacheConfig);
};

export const listProjectRoles = () => {
  return axios.get(`${PROJECT_API_BASE_URL}/roles`, noCacheConfig);
};

export const createProject = (project) => {
  return axios.post(`${PROJECT_API_BASE_URL}/create`, project);
};

export const updateProject = (projectId, project) => {
  return axios.put(`${PROJECT_API_BASE_URL}/update/${projectId}`, project);
};

export const deleteProject = (projectId) => {
  return axios.delete(`${PROJECT_API_BASE_URL}/delete/${projectId}`);
};

export const listProjectPermissionConfigurations = (projectId) => {
  return axios.get(
    `${PROJECT_API_BASE_URL}/${projectId}/permission-configurations`,
    noCacheConfig
  );
};

export const configureProjectPermission = (projectId, permissionId, configuration) => {
  return axios.put(
    `${PROJECT_API_BASE_URL}/${projectId}/permissions/${permissionId}/configure`,
    configuration
  );
};
