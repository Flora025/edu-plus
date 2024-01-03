package com.edu.search.service;

import com.edu.base.model.PageParams;
import com.edu.search.dto.SearchCourseParamDto;
import com.edu.search.dto.SearchPageResultDto;
import com.edu.search.po.CourseIndex;

/**
 * @description 课程搜索service
 */
public interface CourseSearchService {


    /**
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.edu.base.model.PageResult<com.edu.search.po.CourseIndex> 课程列表
     * @description 搜索课程列表
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
