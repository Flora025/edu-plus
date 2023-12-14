package com.edu.content.service;

import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;

public interface CourseBaseInfoService {

    /*
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto query条件
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}