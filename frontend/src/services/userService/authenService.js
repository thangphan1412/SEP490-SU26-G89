import axios from "axios";
import axiosClient from "../../config/api/axiosClient.js";
const authenService = {
    login(data){
        console.log("Calling POST /auth/login");
        return axiosClient.post("/auth/login", data)
    }
}
export default authenService