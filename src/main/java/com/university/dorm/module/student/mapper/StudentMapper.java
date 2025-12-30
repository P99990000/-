package com.university.dorm.module.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.dorm.module.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
