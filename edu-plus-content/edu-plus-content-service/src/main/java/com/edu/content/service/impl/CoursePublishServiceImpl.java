package com.edu.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.edu.base.exception.EduPlusException;
import com.edu.content.mapper.CourseBaseMapper;
import com.edu.content.mapper.CourseMarketMapper;
import com.edu.content.mapper.CoursePublishPreMapper;
import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.CoursePreviewDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.CourseBase;
import com.edu.content.model.po.CourseMarket;
import com.edu.content.model.po.CoursePublishPre;
import com.edu.content.service.CourseBaseInfoService;
import com.edu.content.service.CoursePublishService;
import com.edu.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

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

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        // 1. 约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            EduPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        // 本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            EduPlusException.cast("不允许提交其它机构的课程。");
        }

        // 课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            EduPlusException.cast("提交失败，请上传课程图片");
        }

        // 2. 添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        // --课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBase(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // --课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // --转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        // --将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        // --查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.getTeachplanTreeNodes(courseId);
        if (teachplanTree.size() <= 0) {
            EduPlusException.cast("提交失败，还没有添加课程计划");
        }
        // --转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        // --设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        // --教学机构id
        coursePublishPre.setCompanyId(companyId);
        // --提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        // 3. 提交到预发布表
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            // 添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 4. 更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}
