    package com.fpt.backend.constant;

    public class ApiConstant {
        public static final String API = "/api/v1";
        public static class USER{
            public static final String USER = API+"/user";
        }
        public static class Authentication{
            public static final String LOGIN ="/auth/login";
        }
        public static class Department {
            public static final String DEPARTMENTS = API + "/departments";
            public static final String LIST = "/list";
            public static final String BY_ID = "/{id}";
        }
    }
