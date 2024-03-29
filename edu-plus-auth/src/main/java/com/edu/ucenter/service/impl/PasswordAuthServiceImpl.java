package com.edu.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.ucenter.feignclient.CheckCodeClient;
import com.edu.ucenter.mapper.XcUserMapper;
import com.edu.ucenter.model.dto.AuthParamsDto;
import com.edu.ucenter.model.dto.XcUserExt;
import com.edu.ucenter.model.po.XcUser;
import com.edu.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 账号密码认证
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;


    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //校验验证码

        String checkcode = authParamsDto.getCheckcode();
        String checkcodeKey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcodeKey) || StringUtils.isBlank(checkcode)) {
            throw new RuntimeException("验证码为空");
        }

        // 通过feign调用checkcode服务中的校验
        Boolean verify = checkCodeClient.verify(checkcodeKey, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码错误");
        }

        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);

        // 校验密码
        // 取出数据库存储的正确密码
        String passwordDb = user.getPassword();
        String passwordForm = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }

        return xcUserExt;
    }
}

