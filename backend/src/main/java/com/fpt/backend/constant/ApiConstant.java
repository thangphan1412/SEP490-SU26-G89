    package com.fpt.backend.constant;

    public class ApiConstant {
        public static final String API = "/api/v1";
        public static class USER{
            public static final String USER = API+"/user";
            // Thêm Users và Profile
            public static final String USERS = "/api/users";
            public static final String PROFILE = "/api/profile";
            public static final String BY_ID = "/{id}";
        }
        public static class Authentication{
            public static final String LOGIN ="/auth/login";
            public static final String FORGOT ="/auth/forgot-password";
            public static final String RESET_PASSWORD ="/auth/reset-password";
            public static final String CHANGE_PASSWORD ="/auth/change-password";
        }
        public static class Department {
            public static final String DEPARTMENTS = API + "/departments";
            public static final String LIST = "/list";
            public static final String BY_ID = "/{id}";
        }
        public static class Role {
            public static final String ROLES = API + "/roles";
            public static final String LIST = "/list";
            public static final String BY_ID = "/{id}";
        }
        public static class Signatures{
            public static final String SIGNATURES = "/list-electronic-signatures";
            public static final String ELECTRONICSIGNATURES = "/create/electronic-signatures";
            public static final String ELECTRONICBYID = "/electronic-by/{id}";
            public static final String UPDATEELECTRONICSIGNATURE = "update/electronic-by/{id}";
        }
        // Thêm Dashboard Constants
        public static class Dashboard {
            public static final String DASHBOARD = API + "/dashboard";
            public static final String OVERVIEW = "/overview";
            public static final String STATISTICAL_REPORTS = "/statistical-reports";
            public static final String PENDING_SIGNATURES = "/pending-signatures";
        }

        // Thêm Company Constants
        public static class Company {
            public static final String COMPANY_PROFILE = "/api/company-profile";
        }
    }
