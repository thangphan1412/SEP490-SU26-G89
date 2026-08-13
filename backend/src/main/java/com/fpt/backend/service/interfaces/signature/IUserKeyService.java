package com.fpt.backend.service.interfaces.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;

public interface IUserKeyService {
    public UserKeys generateUserKey(Users user);

}
