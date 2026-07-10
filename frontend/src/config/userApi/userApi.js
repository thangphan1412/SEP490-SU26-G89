import axios from "axios";

// Đổi đường dẫn này theo đúng chuẩn API BE Spring Boot của bạn nhé
const USER_API_BASE_URL = "http://localhost:8080/api/users";

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