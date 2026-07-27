import axiosClient from "../../config/api/axiosClient.js";

const API_BASE_URL = String(
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");
const PHASE_API_BASE_URL = `${API_BASE_URL}/phases`;

const noCacheHeaders = {
  "Cache-Control": "no-cache",
};

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

export async function viewPhase(phaseId, signal) {
  const response = await axiosClient.get(
    `${PHASE_API_BASE_URL}/${phaseId}`,
    { headers: noCacheHeaders, signal }
  );

  return getResponseData(response);
}
