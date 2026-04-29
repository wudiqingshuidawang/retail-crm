package com.crm.module.auth.controller;

import com.crm.common.Result;
import com.crm.module.auth.dto.LoginDTO;
import com.crm.module.auth.dto.LoginResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResult result = LoginResult.builder()
                .token("dev-token")
                .realName("管理员")
                .role("ADMIN")
                .build();
        return Result.ok(result);
    }
}
