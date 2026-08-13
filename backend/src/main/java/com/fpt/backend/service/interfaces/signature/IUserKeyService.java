package com.fpt.backend.service.interfaces.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;

public interface IUserKeyService {
    UserKeys generateUserKey(Users user);

}
