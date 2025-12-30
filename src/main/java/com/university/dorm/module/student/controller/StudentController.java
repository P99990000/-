package com.university.dorm.module.student.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.dormitory.entity.Dormitory;
import com.university.dorm.module.dormitory.service.DormitoryService;
import com.university.dorm.module.student.entity.Student;
import com.university.dorm.module.student.service.StudentService;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private DormitoryService dormitoryService;

    private static final byte[] JWT_KEY = "dorm-system-secret-key-2025".getBytes(StandardCharsets.UTF_8);

    @GetMapping
    public Result<List<Student>> list() {
        return Result.success(studentService.list());
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentStudent(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !JWTUtil.verify(token, JWT_KEY)) {
            return Result.error("未登录或token失效");
        }

        String role = (String) JWTUtil.parseToken(token).getPayload("role");
        if (!"student".equals(role)) {
            return Result.error("非学生账号");
        }

        Number userId = (Number) JWTUtil.parseToken(token).getPayload("userId");
        Long studentId = userId.longValue();

        // Handle demo account
        if (studentId == -1L) {
            Student demoStudent = new Student();
            demoStudent.setId(-1L);
            demoStudent.setStudentSn("student");
            demoStudent.setName("演示学生");
            demoStudent.setClassName("演示班级");
            demoStudent.setGender(1);

            // Try to find a dorm for the demo student
            // First try "北10栋" "310" as requested in scenarios
            Dormitory demoDorm = dormitoryService.lambdaQuery()
                    .eq(Dormitory::getBuildingName, "北10栋")
                    .eq(Dormitory::getRoomNumber, "310")
                    .one();

            if (demoDorm == null) {
                // Fallback to first available dorm so user sees some data
                List<Dormitory> list = dormitoryService.list();
                if (!list.isEmpty()) {
                    demoDorm = list.get(0);
                }
            }

            if (demoDorm == null) {
                // Fallback to dummy if DB is empty
                demoDorm = new Dormitory();
                demoDorm.setId(-1L);
                demoDorm.setBuildingName("北10栋");
                demoDorm.setFloor(3);
                demoDorm.setRoomNumber("310");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("student", demoStudent);
            data.put("dorm", demoDorm);
            return Result.success(data);
        }

        Student student = studentService.getById(studentId);
        if (student == null) {
            return Result.error("学生信息不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("student", student);

        if (student.getDormId() != null) {
            Dormitory dorm = dormitoryService.getById(student.getDormId());
            data.put("dorm", dorm);
        }

        return Result.success(data);
    }
}
