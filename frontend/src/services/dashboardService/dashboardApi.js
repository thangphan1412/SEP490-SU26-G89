import axiosClient from "../../config/api/axiosClient.js";

const dashboardApi = {
    getOverview() {
        return axiosClient.get("/dashboard/overview");
    },
    getStatisticalReports() {
        return axiosClient.get("/dashboard/statistical-reports");
    },
    getPendingSignatures() {
        return axiosClient.get("/dashboard/pending-signatures");
    }
};

export default dashboardApi;