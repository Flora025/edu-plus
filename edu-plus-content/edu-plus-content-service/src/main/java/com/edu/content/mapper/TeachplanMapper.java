package com.edu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * 查询课程的课程计划 树形结构
     * @param courseId 课程id
     * @return list of dto
     */
    public List<TeachplanDto> selectTreeNodes(Long courseId);

    /**
     * 查询某章节的所有子章节
     * @param planId 章节id
     * @return 所有子章节dto
     */
    public List<Teachplan> selectSubNodes(Long planId);

}
