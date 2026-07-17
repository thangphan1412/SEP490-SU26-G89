package com.fpt.backend.util;

import java.util.regex.Pattern;

public class ValidateEmail {
    public static boolean validateEmail(String email, String regexPattern){
        return Pattern.compile(regexPattern).matcher(email).matches();
    }
}
