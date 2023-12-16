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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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

    /**
     * 修改教师信息
     * @param courseTeacher 教师信息
     * @return 修改后的教师信息
     */
    @Transactional
    @Override
    public CourseTeacher editCourseTeacher(CourseTeacher courseTeacher) {
        Long teacherId = courseTeacher.getId();
        CourseTeacher courseTeacherCur = courseTeacherMapper.selectById(teacherId);
        if (courseTeacherCur == null) {
            EduPlusException.cast("教师不存在");
        }

        CourseTeacher courseTeacherNew = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacherCur, courseTeacherNew);

        int i = courseTeacherMapper.updateById(courseTeacherNew);
        if (i <= 0) {
            EduPlusException.cast("修改教师信息失败");
        }
        return courseTeacherMapper.selectById(courseTeacherNew.getId());
    }

    /**
     * 删除教师信息
     * @param courseId 课程id
     * @param teacherId 教师id
     */
    @Transactional
    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        // create query
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);

        // delete teacher by query
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete <= 0) {
            EduPlusException.cast("删除教师失败");
        }
    }
}
