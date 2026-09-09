import axiosClient from "../../config/api/axiosClient.js";

const COMPANY_API_BASE_URL = "http://localhost:8080/api/company-profile";

// Khai báo hàm lấy Token giống hệt bên userApi.js
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token ? { "Authorization": `Bearer ${token}` } : {};
};

export const getCompanyProfile = () => {
    return axiosClient.get(COMPANY_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
            // "Authorization": `Bearer ${localStorage.getItem("token")}` // Bật lên nếu FE đã cấu hình token
        },
    });
};

export const updateCompanyProfile = (profileData) => {
    return axiosClient.put(COMPANY_API_BASE_URL, profileData, {
        headers: {
            "Cache-Control": "no-cache",
            // "Authorization": `Bearer ${localStorage.getItem("token")}`
        },
    });
};
