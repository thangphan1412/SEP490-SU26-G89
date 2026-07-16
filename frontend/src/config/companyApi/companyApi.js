import axios from "axios";

const COMPANY_API_BASE_URL = "http://localhost:8080/api/company-profile";

// Lấy thông tin công ty
export const getCompanyProfile = () => {
    return axios.get(COMPANY_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
            // "Authorization": `Bearer ${localStorage.getItem("token")}` // Bật lên nếu FE đã cấu hình token
        },
    });
};

// Cập nhật thông tin công ty
export const updateCompanyProfile = (profileData) => {
    return axios.put(COMPANY_API_BASE_URL, profileData, {
        headers: {
            "Cache-Control": "no-cache",
            // "Authorization": `Bearer ${localStorage.getItem("token")}`
        },
    });
};