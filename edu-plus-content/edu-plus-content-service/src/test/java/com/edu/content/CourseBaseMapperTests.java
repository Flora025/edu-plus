package com.edu.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.mapper.CourseBaseMapper;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper() {
        // Test1: select coursebase by id
        CourseBase courseBase = courseBaseMapper.selectById(74L);
        Assertions.assertNotNull(courseBase); // assert that the obj exists

        // Test2: test pagination query using mybatis plus

        // 2.1 init a query dto
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");
        queryCourseParamsDto.setPublishStatus("203001");

        // 2.2 create query wrapper
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 1) query by course name
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());
        // 2) query by course audit status
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());

        // 2.3 define pagination params & create page obj
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);//页码
        pageParams.setPageSize(3L);//每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 2.4 query with page object and query wrapper
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        // 2.5 collect results and return PageResult
        List<CourseBase> items = pageResult.getRecords();
        // page data
        long total = pageResult.getTotal();
        long pageNo = pageParams.getPageNo();
        long pageSize = pageParams.getPageSize();
        // List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, total, pageNo, pageSize);

        System.out.println(courseBasePageResult);

    }
}
