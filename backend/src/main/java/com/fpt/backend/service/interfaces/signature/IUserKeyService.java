package com.fpt.backend.service.interfaces.signature;

import com.fpt.backend.dto.response.signature.UserKeyInfoResponse;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;

public interface IUserKeyService {
    UserKeyInfoResponse generateUserKey(Users user);

}
