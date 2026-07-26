import axiosClient from "../../config/api/axiosClient.js";

const API_BASE_URL = String(
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");
const PROJECT_API_BASE_URL = `${API_BASE_URL}/projects`;

const noCacheHeaders = {
  "Cache-Control": "no-cache",
};

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

export async function listProjects(params, signal) {
  const response = await axiosClient.get(PROJECT_API_BASE_URL, {
    headers: noCacheHeaders,
    params,
    signal,
  });

  return getResponseData(response);
}

export async function viewProject(projectId, signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/${projectId}`,
    { headers: noCacheHeaders, signal }
  );

  return getResponseData(response);
}

export async function listProjectEmployees(signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/employees`,
    { headers: noCacheHeaders, signal }
  );

  return getResponseData(response);
}

export async function listProjectRoles(signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/roles`,
    { headers: noCacheHeaders, signal }
  );

  return getResponseData(response);
}

export async function createProject(project) {
  const response = await axiosClient.post(PROJECT_API_BASE_URL, project);
  return getResponseData(response);
}

export async function updateProject(projectId, project) {
  const response = await axiosClient.put(
    `${PROJECT_API_BASE_URL}/${projectId}`,
    project
  );

  return getResponseData(response);
}

export async function deleteProject(projectId) {
  const response = await axiosClient.delete(`${PROJECT_API_BASE_URL}/${projectId}`);
  return response.data?.message || "";
}

export async function listProjectPermissionConfigurations(projectId, signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/${projectId}/permission-configurations`,
    { headers: noCacheHeaders, signal }
  );

  return getResponseData(response);
}

export async function configureProjectPermission(
  projectId,
  permissionId,
  configuration
) {
  const response = await axiosClient.put(
    `${PROJECT_API_BASE_URL}/${projectId}/permissions/${permissionId}`,
    configuration
  );

  return getResponseData(response);
}
