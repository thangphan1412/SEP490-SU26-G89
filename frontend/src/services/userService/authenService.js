import axios from "axios";
import axiosClient from "../../config/api/axiosClient.js";
const authenService = {
    login(data){
        console.log("Calling POST /auth/login");
        return axiosClient.post("/auth/login", data)
    },
    forgot(data){
        console.log("Calling POST /auth/forgot-password")
        return axiosClient.post("/auth/forgot-password",data);
    },
    reset(data){
        return axiosClient.post("/auth/forgot-password", data)
    }
}
export default authenService