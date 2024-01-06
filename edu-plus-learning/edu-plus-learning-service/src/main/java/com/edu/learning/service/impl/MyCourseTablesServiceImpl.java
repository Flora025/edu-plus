package com.edu.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.base.exception.EduPlusException;
import com.edu.base.model.PageResult;
import com.edu.content.model.po.CoursePublish;
import com.edu.learning.feignclient.ContentServiceClient;
import com.edu.learning.mapper.XcChooseCourseMapper;
import com.edu.learning.mapper.XcCourseTablesMapper;
import com.edu.learning.model.dto.MyCourseTableParams;
import com.edu.learning.model.dto.XcChooseCourseDto;
import com.edu.learning.model.dto.XcCourseTablesDto;
import com.edu.learning.model.po.XcChooseCourse;
import com.edu.learning.model.po.XcCourseTables;
import com.edu.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 选课接口实现
@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 调用内容管理服务 查询课程收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        // 课程收费标准
        String charge = coursepublish.getCharge();
        // 选课记录
        XcChooseCourse chooseCourse = null;
        if ("201000".equals(charge)) {
            // if免费课程 写入选课记录&我的课程表
            // 添加免费课程写入选课记录
            chooseCourse = addFreeCoruse(userId, coursepublish);
            // 添加到我的课程表
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
        } else {
            // if收费课程 写入选课记录
            chooseCourse = addChargeCoruse(userId, coursepublish);
        }


        // 判断学生的学习资格
        // 注意这里要新建一个choose course dto用来返回
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, xcChooseCourseDto);

        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }


    /**
     * 添加免费课程,免费课程加入选课记录表、我的课程表
     */
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {

        // 1 check是否已经存在相同记录（db里没有约束 因此可能重复）
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId) // 相同课程
                .eq(XcChooseCourse::getOrderType, "700001") // 免费课程
                .eq(XcChooseCourse::getStatus, "701001"); // 选课成功

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }

        // 2 写入选课表
        XcChooseCourse xcChooseCourse = new XcChooseCourse();

        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);// 免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");// 免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");// 选课成功

        xcChooseCourse.setValidDays(365);// 免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        // 插入db
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }

    // 添加收费课程
    public XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish) {

        // 查询待支付交易记录 如果存在未支付->直接返回 不要写入选课记录
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId()) // 当前课程
                .eq(XcChooseCourse::getOrderType, "700002")// 收费订单
                .eq(XcChooseCourse::getStatus, "701002");// 状态为待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }

        // 如果已支付 则写入选课记录表
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");// 收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");// 待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));

        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    /**
     * 添加到我的课程表
     *
     * @param xcChooseCourse
     * @return
     */
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse) {
        // check 选课status 选课成功才能向课程表添加
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)) {
            EduPlusException.cast("选课未成功，无法添加到课程表");
        }

        // 查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables != null) {
            return xcCourseTables;
        }

        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());

        // 插入db
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;

    }

    /**
     * 根据课程和用户查询[我的课程表]中某一门课程
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 查询到的课程记录
     */
    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        // 1 判断是否未支付/未选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            // 未支付/未选课
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }

        // 2 判断是否到期
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (!isExpires) {
            // 没有过期 -> 正常学习
            xcCourseTablesDto.setLearnStatus("702001");
            return xcCourseTablesDto;

        } else {
            // 已过期
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }

    }

    @Override
    public boolean saveChooseCourseStauts(String choosecourseId) {
        // 更具选课id查询选课表
        XcChooseCourse chooseCourse = xcChooseCourseMapper.selectById(choosecourseId);
        if (chooseCourse == null) {
            log.debug("接受购买课程消息，根据选课id无法找到对应选课记录");
            return false;
        }
        String status = chooseCourse.getStatus();
        if ("701002".equals(status)) {
            // 更新选课记录状态为支付成功
            chooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(chooseCourse);
            if (i <= 0) {
                log.debug("添加选课记录失败：{}", chooseCourse);
                EduPlusException.cast("添加选课记录失败");
            }

            // 向我的课表插入记录
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
        }

        return true;
    }

    @Override
    public PageResult<XcCourseTables> getMycoursetables(MyCourseTableParams params) {
        //get page num
        long pageNo = params.getPage();
        long pageSize = 4;
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);

        String userId = params.getUserId();

        // 根据用户id&分页参数查询
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);
        Page<XcCourseTables> pageResult = xcCourseTablesMapper.selectPage(page, queryWrapper);
        List<XcCourseTables> records = pageResult.getRecords();

        //记录总数 + 返回
        long total = pageResult.getTotal();
        return new PageResult<>(records, total, pageNo, pageSize);

    }


}
