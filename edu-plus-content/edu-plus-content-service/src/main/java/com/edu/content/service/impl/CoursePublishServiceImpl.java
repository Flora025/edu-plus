package com.edu.content.service.impl;

import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.CoursePreviewDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.service.CourseBaseInfoService;
import com.edu.content.service.CoursePublishService;
import com.edu.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        // 查询课程基本信息&market info
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBase(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);

        // 查询teachplan
        List<TeachplanDto> teachplanTreeNodes = teachplanService.getTeachplanTreeNodes(courseId);
        coursePreviewDto.setTeachplans(teachplanTreeNodes);

        return coursePreviewDto;
    }
}
