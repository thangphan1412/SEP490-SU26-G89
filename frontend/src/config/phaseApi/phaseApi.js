import axios from "axios";

const PHASE_API_BASE_URL = "http://localhost:8080/api/phases";

const noCacheConfig = {
  headers: {
    "Cache-Control": "no-cache",
  },
};

export const listProjectPhases = (projectId) => {
  return axios.get(`${PHASE_API_BASE_URL}/project/${projectId}`, noCacheConfig);
};

export const viewPhase = (phaseId) => {
  return axios.get(`${PHASE_API_BASE_URL}/view/${phaseId}`, noCacheConfig);
};
