package com.edu.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="AddCourseTeacherDto", description="新增教师基本信息")
public class AddCourseTeacherDto {

    /**
     * 课程标识
     */
    @NotNull(message = "课程id不能为空")
    @ApiModelProperty(value = "课程id", required = true)
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(message = "教师姓名不能为空")
    @ApiModelProperty(value = "教师姓名", required = true)
    private String teacherName;

    /**
     * 教师职位
     */
    @NotEmpty(message = "教师职位不能为空")
    @ApiModelProperty(value = "教师职位", required = true)
    private String position;

    /**
     * 教师简介
     */
    @ApiModelProperty(value = "教师简介")
    private String introduction;

}
