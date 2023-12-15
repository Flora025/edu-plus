package com.edu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.content.model.dto.CourseCategoryTreeDto;
import com.edu.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    public List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
