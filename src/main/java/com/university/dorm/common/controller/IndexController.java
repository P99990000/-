package com.university.dorm.common.controller;

import com.university.dorm.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/api")
    public Result<String> index() {
        return Result.success("欢迎访问全校宿舍卫生管理系统 API");
    }
}
