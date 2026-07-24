import axios from "axios";

const PERMISSION_API_BASE_URL = "http://localhost:8080/api/permissions";

const noCacheConfig = {
  headers: {
    "Cache-Control": "no-cache",
  },
};

export const listPermissions = (params) => {
  return axios.get(`${PERMISSION_API_BASE_URL}/list`, {
    ...noCacheConfig,
    params,
  });
};

export const viewPermission = (permissionId) => {
  return axios.get(`${PERMISSION_API_BASE_URL}/view/${permissionId}`, noCacheConfig);
};

export const listPermissionProjects = () => {
  return axios.get(`${PERMISSION_API_BASE_URL}/projects`, noCacheConfig);
};

export const listPermissionRoles = () => {
  return axios.get(`${PERMISSION_API_BASE_URL}/roles`, noCacheConfig);
};

export const createPermission = (permission) => {
  return axios.post(`${PERMISSION_API_BASE_URL}/create`, permission);
};

export const updatePermission = (permissionId, permission) => {
  return axios.put(`${PERMISSION_API_BASE_URL}/update/${permissionId}`, permission);
};

export const deletePermission = (permissionId) => {
  return axios.delete(`${PERMISSION_API_BASE_URL}/delete/${permissionId}`);
};
