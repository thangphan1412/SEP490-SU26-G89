import axios from "axios";
import axiosClient from "../../config/api/axiosClient.js";
const authenService = {
    login(data) {
        console.log("Calling POST /auth/login");
        return axiosClient.post("/auth/login", data);
    },
    forgot(data) {
        console.log("Calling POST /auth/forgot-password");
        return axiosClient.post("/auth/forgot-password", data);
    },
    reset(data) {
        // ĐÃ SỬA LỖI COPY-PASTE: Đổi từ forgot-password thành reset-password
        return axiosClient.post("/auth/reset-password", data);
    },
    // THÊM HÀM NÀY CHO CHỨC NĂNG ĐỔI BẰNG MẬT KHẨU CŨ (Cách 1)
    changePassword(data) {
        console.log("Calling POST /auth/change-password");
        return axiosClient.post("/auth/change-password", data);
    }
};
export default authenService