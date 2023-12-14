package com.edu.content.api;


import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(value = "Course info interface",tags = "Course info interface")
@RestController // 返回一个对象而不是视图
public class CourseBaseInfoController {

    @ApiOperation("Course info query")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        // Test
        CourseBase courseBase = new CourseBase();
        courseBase.setName("测试名称");
        courseBase.setCreateDate(LocalDateTime.now());
        List<CourseBase> courseBases = new ArrayList<>();
        courseBases.add(courseBase);

        return new PageResult<CourseBase>(courseBases,10,1,10);
    }
}
