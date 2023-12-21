package com.edu.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.base.exception.EduPlusException;
import com.edu.content.mapper.TeachplanMapper;
import com.edu.content.mapper.TeachplanMediaMapper;
import com.edu.content.model.dto.BindTeachplanMediaDto;
import com.edu.content.model.dto.SaveTeachplanDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.Teachplan;
import com.edu.content.model.po.TeachplanMedia;
import com.edu.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
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
     *
     * @param courseId 课程id
     * @param parentId 父层级id
     * @return 当前最大序号
     */
    private int getTeachplanCount(Long courseId, Long parentId) {
        // sql: select count(1) from teachplan where course_id=117 and parentid=268
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);

        return teachplanMapper.selectCount(queryWrapper);
    }

    /**
     * 获取指定parent下同级别所有plan
     *
     * @param courseId
     * @param parentId
     * @return list of plans
     */
    private List<Teachplan> getTeachplans(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        queryWrapper.orderByAsc(Teachplan::getOrderby);

        return teachplanMapper.selectList(queryWrapper);
    }

    /**
     * 删除指定id的章节
     * 对于大章节，只有在无小章节时可删除
     * 对于小章节，会将teachplan_media表关联的信息也删除
     *
     * @param planId plan id
     */
    @Transactional
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

    /**
     * 移动课程计划章节
     *
     * @param direction 移动方向
     * @param planId    计划id
     */
    @Transactional
    @Override
    public void moveTeachplan(String direction, Long planId) {
        // 查询当前plan
        Teachplan teachplan = teachplanMapper.selectById(planId);

        // 获得当前层级的所有teach plan
        List<Teachplan> teachplans = getTeachplans(teachplan.getCourseId(), teachplan.getParentid());

        // 获取目标排序信息
        Integer curOrder = teachplan.getOrderby();
        Integer targetOrder;
        if (direction.equals("moveup")) {
            if (curOrder == 1) {
                return; // 第一个 无法继续网上
            }
            targetOrder = curOrder - 1;
        } else {
            if (curOrder == teachplans.size()) {
                return; // 最后一个 无法继续往下
            }
            targetOrder = curOrder + 1;
        }

        // 找到需要交换位置的teachplan
        Teachplan targetTeachplan = teachplans.get(targetOrder - 1);

        // 交换排序信息
        targetTeachplan.setOrderby(curOrder);
        teachplan.setOrderby(targetOrder);

        int targetUpdate = teachplanMapper.updateById(targetTeachplan);
        if (targetUpdate <= 0) {
            EduPlusException.cast("移动失败");
        }
        int curUpdate = teachplanMapper.updateById(teachplan);
        if (curUpdate <= 0) {
            EduPlusException.cast("移动失败");
        }
    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 1. validation
        // 教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            EduPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            EduPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }

        // 获得课程id
        Long courseId = teachplan.getCourseId();

        // 2. delete if there exists bound media
        // 先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));

        // 3. bind media
        // 再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName()); // 名字不一样 单独设置一下
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);

        return teachplanMedia;

    }
}
