package com.edu.content.api;


import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 返回一个对象而不是视图
public class CourseBaseInfoController {

    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return null; // TODO
    }
}
