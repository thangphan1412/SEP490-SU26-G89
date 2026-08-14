import axios from "axios";

const axiosClient = axios.create({
    baseURL: "http://localhost:8080/api/v1",
});

axiosClient.interceptors.request.use((config) => {

    const token = localStorage.getItem("token");

    const publicApi = [
        "/auth/login",
        "/auth/forgot-password",
    ];

    const isPublicApi = publicApi.some(
        (path) => config.url?.startsWith(path)
    );

    if (token && !isPublicApi) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    if (config.data instanceof FormData) {
        delete config.headers["Content-Type"];
    }

    return config;
});

axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const requestUrl = error.config?.url || "";
        const isLoginRequest = requestUrl.startsWith("/auth/login");

        if (error.response?.status === 401 && !isLoginRequest) {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            localStorage.removeItem("fullName");
            localStorage.removeItem("departmentName");

            if (window.location.pathname !== "/login") {
                window.location.replace("/login");
            }
        }

        return Promise.reject(error);
    }
);

export default axiosClient;
