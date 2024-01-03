package com.edu.ucenter.service;

import com.edu.ucenter.model.dto.AuthParamsDto;
import com.edu.ucenter.model.dto.XcUserExt;

// 认证service 有多种impl方式
public interface AuthService {
    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.edu.ucenter.model.po.XcUser 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
