import axiosClient from "../../config/api/axiosClient.js";

const PERMISSION_API_BASE_URL = "http://localhost:8080/api/permissions";

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

export async function listPermissions(params, signal) {
  const response = await axiosClient.get(PERMISSION_API_BASE_URL + "/list", {
    params,
    signal,
  });
  return getResponseData(response);
}

export async function viewPermission(permissionId, signal) {
  const response = await axiosClient.get(
    `${PERMISSION_API_BASE_URL}/${permissionId}`,
    { signal }
  );

  return getResponseData(response);
}

export async function listPermissionProjects(signal) {
  const response = await axiosClient.get(
    `${PERMISSION_API_BASE_URL}/projects`,
    { signal }
  );

  return getResponseData(response);
}

export async function listPermissionActions(signal) {
  const response = await axiosClient.get(
    `${PERMISSION_API_BASE_URL}/actions`,
    { signal }
  );

  return getResponseData(response);
}

export async function createPermission(permission) {
  const response = await axiosClient.post(PERMISSION_API_BASE_URL, permission);
  return getResponseData(response);
}

export async function updatePermission(permissionId, permission) {
  const response = await axiosClient.put(
    `${PERMISSION_API_BASE_URL}/${permissionId}`,
    permission
  );

  return getResponseData(response);
}

export async function deletePermission(permissionId) {
  await axiosClient.delete(`${PERMISSION_API_BASE_URL}/${permissionId}`);
}
