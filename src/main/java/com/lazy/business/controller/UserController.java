package com.lazy.business.controller;

import com.lazy.business.service.UserService;
import com.lazy.spring.annotation.*;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/16 下午9:41
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getUser")
    public void get(@RequestParam("name")String name){
        System.out.println(name);
        System.out.println(userService.get());
    }
}
