package com.edu.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.base.exception.EduPlusException;
import com.edu.content.mapper.TeachplanMapper;
import com.edu.content.mapper.TeachplanMediaMapper;
import com.edu.content.model.dto.SaveTeachplanDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.Teachplan;
import com.edu.content.model.po.TeachplanMedia;
import com.edu.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

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

    /**
     * 删除指定id的章节
     * 对于大章节，只有在无小章节时可删除
     * 对于小章节，会将teachplan_media表关联的信息也删除
     * @param planId plan id
     */
    @Override
    public void deleteTeachplan(Long planId) {
        Teachplan teachplan = teachplanMapper.selectById(planId);
        if (teachplan == null) {
            EduPlusException.cast("当前章节不存在");
        }

        Integer grade = teachplan.getGrade();
        if (grade == 1) {
            // 当前为大章节时 判断是否有小章节
            List<Teachplan> subPlans = teachplanMapper.selectSubNodes(planId);
            if (subPlans.isEmpty()) {
                // 无小章节 -> 删除
                teachplanMapper.deleteById(planId);
            } else {
                // 有小章节 -> 抛出err
                EduPlusException.cast("课程计划信息还有子级信息，无法操作");
            }

        } else if (grade == 2) {
            // 当前为小章节 删除base info及关联的teachplan media
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, planId);
            int delete = teachplanMediaMapper.delete(queryWrapper);
            if (delete <= 0) {
                EduPlusException.cast("课程章节媒体资源删除失败");
            }

            int i = teachplanMapper.deleteById(planId);
            if (i <= 0) {
                EduPlusException.cast("课程章节删除失败");
            }
        }
    }
}
