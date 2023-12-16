package com.edu.content.api;

import com.edu.content.model.dto.AddCourseTeacherDto;
import com.edu.content.model.po.CourseTeacher;
import com.edu.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Course teacher controller",tags = "Course teacher controller")
@RestController // 返回一个对象而不是视图
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询课程教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> listCourseTeacher(@PathVariable Long courseId){
        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);
        return courseTeacher;
    }

    @ApiOperation("添加课程教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(@RequestBody @Validated AddCourseTeacherDto addCourseTeacherDto) {
        return courseTeacherService.addCourseTeacher(addCourseTeacherDto);
    }

    @ApiOperation("修改课程教师信息")
    @PutMapping("/courseTeacher")
    public CourseTeacher editCourseTeacher(@RequestBody @Validated CourseTeacher courseTeacher) {
        return courseTeacherService.editCourseTeacher(courseTeacher);
    }

}
