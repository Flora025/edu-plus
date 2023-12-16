package com.edu.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.base.exception.EduPlusException;
import com.edu.content.mapper.CourseTeacherMapper;
import com.edu.content.model.dto.AddCourseTeacherDto;
import com.edu.content.model.po.CourseTeacher;
import com.edu.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeacher = courseTeacherMapper.selectList(queryWrapper);

        return courseTeacher;
    }

    /**
     * 新增教师信息
     * @param addCourseTeacherDto 教师信息请求dto
     * @return 教师信息
     */
    @Override
    public CourseTeacher addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto) {
        // create teacher
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(addCourseTeacherDto, courseTeacher);
        courseTeacher.setCreateDate(LocalDateTime.now());

        // insert into db
        int i = courseTeacherMapper.insert(courseTeacher);
        if (i <= 0) {
            EduPlusException.cast("新增教师失败");
        }

        // query and return
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }
}
