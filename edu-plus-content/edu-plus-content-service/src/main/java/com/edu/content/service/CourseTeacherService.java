package com.edu.content.service;

import com.edu.content.model.dto.AddCourseTeacherDto;
import com.edu.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 查询课程教师信息
     */
    public List<CourseTeacher> getCourseTeacher(Long courseId);

    /**
     * 新增教师信息
     * @param addCourseTeacherDto 教师信息请求dto
     * @return 新增的课程教师信息
     */
    public CourseTeacher addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto);

    /**
     * 修改教师信息
     * @param courseTeacher 教师信息
     * @return 修改后的教师信息
     */
    public CourseTeacher editCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除教师信息
     * @param courseId 课程id
     * @param teacherId 教师id
     */
    public void deleteCourseTeacher(Long courseId, Long teacherId);
}
