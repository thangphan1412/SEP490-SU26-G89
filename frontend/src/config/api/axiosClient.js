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

export default axiosClient;