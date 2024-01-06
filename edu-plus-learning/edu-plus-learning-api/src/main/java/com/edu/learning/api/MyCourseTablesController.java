package com.edu.learning.api;

import com.edu.base.exception.EduPlusException;
import com.edu.base.model.PageResult;
import com.edu.learning.model.dto.MyCourseTableParams;
import com.edu.learning.model.dto.XcChooseCourseDto;
import com.edu.learning.model.dto.XcCourseTablesDto;
import com.edu.learning.model.po.XcCourseTables;
import com.edu.learning.service.MyCourseTablesService;
import com.edu.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的课程表接口
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        // get当前用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            EduPlusException.cast("请登录");
        }
        String userId = user.getId();

        // 添加选课
        XcChooseCourseDto xcChooseCourseDto = myCourseTablesService.addChooseCourse(userId, courseId);

        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        // get当前用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            EduPlusException.cast("请登录");
        }
        String userId = user.getId();

        return myCourseTablesService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            EduPlusException.cast("请登陆后查看课程表");
        }

        String userId = user.getId();
        params.setUserId(userId); // userid必须是从controller穿进去;
        
        return myCourseTablesService.getMycoursetables(params);
    }

}
