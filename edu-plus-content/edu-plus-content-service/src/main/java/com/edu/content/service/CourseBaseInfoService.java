package com.edu.content.service;

import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.model.dto.AddCourseDto;
import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.EditCourseDto;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;

public interface CourseBaseInfoService {

    /*
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto query条件
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /*
     * 添加课程基本信息
     * @param companyId 公司id
     * @param addCourseDto
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 获取课程基本信息
     * @param courseId 课程id
     * @return 课程基本信息类
     */
    CourseBaseInfoDto getCourseBase(Long courseId);

    /**
     * 修改课程基本信息
     * @param companyId 公司id
     * @param dto editCourse dto
     * @return CourseBaseInfoDto对象
     */
    CourseBaseInfoDto editCourseBase(Long companyId, EditCourseDto dto);

    /**
     * 删除课程信息 包括所有关联信息
     * @param courseId 课程id
     */
    void deleteCourse(Long courseId);
}
