package com.university.dorm.module.student.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.student.entity.Student;
import com.university.dorm.module.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public Result<List<Student>> list() {
        return Result.success(studentService.list());
    }
}
