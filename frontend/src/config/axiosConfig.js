import axios from "axios";
const api = axios.create({
    baseURL:"http://localhost:8080/api",
    headers : {
        "Content_Type": "application/json"
    }
})
export default api;
