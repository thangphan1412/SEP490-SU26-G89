    package com.fpt.backend.constant;

    public class ApiConstant {
        public static final String API = "/api/v1";
        public static class USER{
            public static final String USER = API+"/user";
        }
        public static class Authentication{
            public static final String LOGIN ="/auth/login";
            public static final String FORGOT ="/auth/forgot-password";
            public static final String RESETPASSWORD ="/auth/reset-password";
        }
    }
