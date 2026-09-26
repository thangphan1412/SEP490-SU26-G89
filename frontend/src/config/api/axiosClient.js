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

    console.log(
        "URL:",
        config.url,
        "| isPublicApi:",
        isPublicApi,
        "| token attached:",
        !!(token && !isPublicApi)
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
        if (error.response?.status === 401 && !error.config?.url?.startsWith("/auth/login")) {
            const sentAuthorization = error.config?.headers?.Authorization;
            const currentToken = localStorage.getItem("token");
            // A late response from an old session must not clear a newer login.
            if (currentToken && sentAuthorization !== `Bearer ${currentToken}`) {
                return Promise.reject(error);
            }
            ["token", "role", "fullName", "departmentName", "hasSignatureKey", "email"]
                .forEach((key) => localStorage.removeItem(key));

            if (window.location.pathname !== "/login") {
                const reason = error.response?.data?.code === "TOKEN_EXPIRED" ? "expired" : "invalid";
                window.location.replace(`/login?session=${reason}`);
            }
        }
        return Promise.reject(error);
    }
);
export default axiosClient;
