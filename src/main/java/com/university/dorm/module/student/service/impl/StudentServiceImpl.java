package com.university.dorm.module.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.university.dorm.module.student.entity.Student;
import com.university.dorm.module.student.mapper.StudentMapper;
import com.university.dorm.module.student.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
}
