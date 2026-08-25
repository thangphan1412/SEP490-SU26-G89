import axiosClient from "../../config/api/axiosClient.js";

const PHASE_API_BASE_URL = "http://localhost:8080/api/phases";

function getResponseData(response) {
  const responseBody = response.data;

  // Backend thường đặt dữ liệu cần dùng trong thuộc tính data của BaseResponse.
  if (responseBody !== null && responseBody !== undefined) {
    const actualData = responseBody.data;

    if (actualData !== null && actualData !== undefined) {
      return actualData;
    }
  }

  // Trả về toàn bộ response body nếu dữ liệu không được bọc trong BaseResponse.
  return responseBody;
}

export async function viewPhase(phaseId, signal) {
  const response = await axiosClient.get(
    `${PHASE_API_BASE_URL}/${phaseId}`,
    { signal }
  );

  return getResponseData(response);
}
