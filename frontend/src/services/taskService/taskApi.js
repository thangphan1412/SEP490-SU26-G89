import axiosClient from "../../config/api/axiosClient.js";

const API_BASE_URL = String(
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");
const TASK_API_BASE_URL = `${API_BASE_URL}/tasks`;

function getResponseData(response) {
  if (response.data && response.data.data !== undefined) {
    return response.data.data;
  }

  return response.data;
}

export async function getTasksByPhaseId(phaseId, signal) {
  const response = await axiosClient.get(
    `${TASK_API_BASE_URL}/phase/${phaseId}`,
    { signal }
  );

  return getResponseData(response);
}

export async function createTask(phaseId, task) {
  const response = await axiosClient.post(
    `${TASK_API_BASE_URL}/phase/${phaseId}`,
    task
  );

  return getResponseData(response);
}

export async function updateTask(taskId, task) {
  const response = await axiosClient.put(
    `${TASK_API_BASE_URL}/${taskId}`,
    task
  );

  return getResponseData(response);
}

export async function markTaskAsDone(taskId) {
  const response = await axiosClient.patch(
    `${TASK_API_BASE_URL}/${taskId}/done`
  );

  return getResponseData(response);
}
