import axios from "axios";

const USER_API_BASE_URL = "http://localhost:8080/api/users";
const PROFILE_API_BASE_URL = "http://localhost:8080/api/profile";

// Hàm tiện ích: Lấy token an toàn từ localStorage
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token ? { "Authorization": `Bearer ${token}` } : {};
};

// ĐÃ SỬA: Bổ sung tham số "type" và AuthHeader
export const getAllUsers = (type, keyword, role, department, status) => {
    // Tự động build chuỗi query param (VD: ?type=employee&keyword=abc&role=All...)
    const params = new URLSearchParams({ type });
    if (keyword) params.append("keyword", keyword);
    if (role) params.append("role", role);
    if (department) params.append("department", department);
    if (status) params.append("status", status);

    return axios.get(`${USER_API_BASE_URL}?${params.toString()}`, {
        headers: { "Cache-Control": "no-cache", ...getAuthHeader() },
    });
};

// ĐÃ SỬA: Bổ sung AuthHeader
export const getUserById = (id) => {
    return axios.get(USER_API_BASE_URL + "/" + id, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};

// ĐÃ SỬA: Bổ sung AuthHeader
export const createUser = (userData) => {
    return axios.post(USER_API_BASE_URL, userData, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};

// ĐÃ SỬA: Bổ sung AuthHeader
export const updateUser = (id, userData) => {
    return axios.put(USER_API_BASE_URL + "/" + id, userData, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};

// PROFILE APIs (Giữ nguyên của bạn vì đã chuẩn)
export const getMyProfile = () => {
    return axios.get(PROFILE_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};

export const updateMyProfile = (profileData) => {
    return axios.put(PROFILE_API_BASE_URL, profileData, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};


// Đừng quên import axios và có cái getAuthHeader() như bạn đã làm nhé
export const getAllDepartments = () => {
    return axios.get("http://localhost:8080/api/v1/departments", { // Chỉnh lại cho chuẩn URL của nhóm bạn
        headers: { "Cache-Control": "no-cache", ...getAuthHeader() },
    });
};