import axios from "axios";

// Đổi đường dẫn này theo đúng chuẩn API BE Spring Boot của bạn nhé
const USER_API_BASE_URL = "http://localhost:8080/api/users";
const PROFILE_API_BASE_URL = "http://localhost:8080/api/profile";

export const getAllUsers = () => {
    return axios.get(USER_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const getUserById = (id) => {
    return axios.get(USER_API_BASE_URL + "/" + id, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const createUser = (userData) => {
    return axios.post(USER_API_BASE_URL, userData, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

export const updateUser = (id, userData) => {
    return axios.put(USER_API_BASE_URL + "/" + id, userData, {
        headers: {
            "Cache-Control": "no-cache",
        },
    });
};

// Lấy token an toàn
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return token ? { "Authorization": `Bearer ${token}` } : {};
};

// Lấy profile của người đang đăng nhập
export const getMyProfile = () => {
    return axios.get(PROFILE_API_BASE_URL, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader() // Rải header vào đây nếu có token
        },
    });
};

// Cập nhật profile của người đang đăng nhập
export const updateMyProfile = (profileData) => {
    return axios.put(PROFILE_API_BASE_URL, profileData, {
        headers: {
            "Cache-Control": "no-cache",
            ...getAuthHeader()
        },
    });
};