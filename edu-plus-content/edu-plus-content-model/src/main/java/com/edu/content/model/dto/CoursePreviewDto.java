package com.edu.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 课程预览模型类
 */
@Data
@ToString
public class CoursePreviewDto {
    // baseinfo
    private CourseBaseInfoDto courseBase;

    // market
    private List<TeachplanDto> teachplans;

    // TODO: teachplan

    // teacher

}
