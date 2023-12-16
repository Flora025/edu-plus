package com.edu.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.content.mapper.TeachplanMapper;
import com.edu.content.model.dto.SaveTeachplanDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.Teachplan;
import com.edu.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> getTeachplanTreeNodes(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long planId = saveTeachplanDto.getId();
        if (planId == null) {
            // 目录还未创建 -> 新增
            Teachplan teachplanNew = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplanNew);

            // 取出同父同级别的课程计划数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplanNew.setOrderby(count + 1);

            teachplanMapper.insert(teachplanNew);

        } else {
            // 目录已创建 -> 修改/更新
            Teachplan teachplan = teachplanMapper.selectById(planId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    /**
     * 计算同级别计划的count
     * @param courseId 课程id
     * @param parentId 父层级id
     * @return 当前最大序号
     */
    private int getTeachplanCount(Long courseId, Long parentId) {
        // sql: select count(1) from teachplan where course_id=117 and parentid=268
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);

        return teachplanMapper.selectCount(queryWrapper);
    }

}
