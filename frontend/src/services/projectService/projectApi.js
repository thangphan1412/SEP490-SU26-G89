import axiosClient from "../../config/api/axiosClient.js";

const PROJECT_API_BASE_URL = "http://localhost:8080/api/projects";

function getResponseData(response) {
  const responseBody = response.data;

  if (responseBody !== null && responseBody !== undefined) {
    const actualData = responseBody.data;

    if (actualData !== null && actualData !== undefined) {
      return actualData;
    }
  }

  return responseBody;
}

export async function listProjects(params, signal) {
  const response = await axiosClient.get(PROJECT_API_BASE_URL, {
    params,
    signal,
  });

  return getResponseData(response);
}

export async function viewProject(projectId, signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/${projectId}`,
    { signal }
  );

  return getResponseData(response);
}

export async function listProjectEmployees(signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/employees`,
    { signal }
  );

  return getResponseData(response);
}

export async function listProjectUserStatuses(signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/user-statuses`,
    { signal }
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

export async function approveProject(projectId) {
  const response = await axiosClient.post(
    `${PROJECT_API_BASE_URL}/${projectId}/approve`
  );

  return response.data?.message || "Project approved successfully";
}

export async function deleteProject(projectId) {
  const response = await axiosClient.delete(`${PROJECT_API_BASE_URL}/${projectId}`);
  return response.data?.message || "";
}

export async function listProjectPermissionConfigurations(projectId, signal) {
  const response = await axiosClient.get(
    `${PROJECT_API_BASE_URL}/${projectId}/permission-configurations`,
    { signal }
  );

  return getResponseData(response);
}
