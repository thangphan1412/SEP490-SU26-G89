import axiosClient from "../../config/api/axiosClient.js";

const PHASE_API_BASE_URL = "http://localhost:8080/api/phases";

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

export async function viewPhase(phaseId, signal) {
  const response = await axiosClient.get(
    `${PHASE_API_BASE_URL}/${phaseId}`,
    { signal }
  );

  return getResponseData(response);
}
