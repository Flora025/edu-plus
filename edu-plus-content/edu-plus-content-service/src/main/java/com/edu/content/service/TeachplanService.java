package com.edu.content.service;

import com.edu.content.model.dto.SaveTeachplanDto;
import com.edu.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {

    /**
     * 查询课程计划树型结构
     * @param courseId 课程id
     */
    public List<TeachplanDto> getTeachplanTreeNodes(long courseId);

    /**
     * 新增/修改课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param planId plan id
     */
    public void deleteTeachplan(Long planId);

    /**
     * 移动课程计划
     * @param direction 移动方向
     * @param planId 计划id
     */
    public void moveTeachplan(String direction, Long planId);
}
