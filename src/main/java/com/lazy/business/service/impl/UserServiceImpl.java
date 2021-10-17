package com.lazy.business.service.impl;

import com.lazy.business.service.UserService;
import com.lazy.spring.annotation.*;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/16 下午10:25
 */
@Service
public class UserServiceImpl implements UserService {
    public String get() {
        return "Lazy";
    }
}
