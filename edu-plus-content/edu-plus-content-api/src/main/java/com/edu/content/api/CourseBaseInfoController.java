package com.edu.content.api;


import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.model.dto.AddCourseDto;
import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.EditCourseDto;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;
import com.edu.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "Course info interface",tags = "Course info interface")
@RestController // 返回一个对象而不是视图
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("Course info query")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
        return pageResult;
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated  AddCourseDto addCourseDto) {
        // TODO: 暂时hardcode
         Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBase(@PathVariable Long courseId) {
        // 取出当前用户身份
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);

        return courseBaseInfoService.getCourseBase(courseId);
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        // TODO: 暂时hardcode
        Long companyId = 1232141425L;
        return courseBaseInfoService.editCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程信息")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        courseBaseInfoService.deleteCourse(courseId);
    }

}
