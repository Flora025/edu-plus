package com.edu.ucenter.service;

import com.edu.ucenter.model.po.XcUser;

public interface WxAuthService {

    public XcUser wxAuth(String code);

}
