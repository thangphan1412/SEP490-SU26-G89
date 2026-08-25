import axios from "axios";

const COMPANY_API_BASE_URL = "http://localhost:8080/api/company-profile";

// Khai báo hàm lấy Token giống hệt bên userApi.js
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token ? { "Authorization": `Bearer ${token}` } : {};
};

export const getCompanyProfile = () => {
    return axios.get(COMPANY_API_BASE_URL, {
        headers: { "Cache-Control": "no-cache", ...getAuthHeader() },
    });
};

export const updateCompanyProfile = (profileData) => {
    return axios.put(COMPANY_API_BASE_URL, profileData, {
        headers: { "Cache-Control": "no-cache", ...getAuthHeader() },
    });
};