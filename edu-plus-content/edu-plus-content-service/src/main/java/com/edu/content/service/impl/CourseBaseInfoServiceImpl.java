package com.edu.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.base.exception.EduPlusException;
import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.content.mapper.CourseBaseMapper;
import com.edu.content.mapper.CourseCategoryMapper;
import com.edu.content.mapper.CourseMarketMapper;
import com.edu.content.model.dto.AddCourseDto;
import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.QueryCourseParamsDto;
import com.edu.content.model.po.CourseBase;
import com.edu.content.model.po.CourseCategory;
import com.edu.content.model.po.CourseMarket;
import com.edu.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 实现接口
@Service // component
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        // 1. create query wrapper (query by every param)
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 1) query by course name
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());
        // 2) query by course audit status
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());
        // 3) query by publish status
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamsDto.getPublishStatus());

        // 2. create page object
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 3.query with page object and query wrapper
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        // 4. collect results and return PageResult
        List<CourseBase> items = pageResult.getRecords();
        // page data
        long total = pageResult.getTotal();
        long pageNo = pageParams.getPageNo();
        long pageSize = pageParams.getPageSize();

        // List<T> items, long counts, long page, long pageSize
        return new PageResult<>(items, total, pageNo, pageSize);
    }

    @Transactional // 开启事务 因为要往db写数据
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 1. validate data

        if (StringUtils.isBlank(dto.getName())) {
            EduPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            EduPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            EduPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            EduPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            EduPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            EduPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            EduPlusException.cast("收费规则为空");
        }

        // 2. save course base info
        CourseBase courseBase = new CourseBase();
        // copy from dto
        BeanUtils.copyProperties(dto, courseBase);
        // manually set particular info
        // 设置审核状态
        courseBase.setAuditStatus("202002");
        // 设置发布状态
        courseBase.setStatus("203001");
        // 机构id
        courseBase.setCompanyId(companyId);
        // 添加时间
        courseBase.setCreateDate(LocalDateTime.now());

        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0) {
            throw new RuntimeException("新增课程基本信息失败");
        }

        // 3. save course market info
        CourseMarket courseMarket = new CourseMarket();
        // copy from dto
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseBase.getId()); // 通过coursebase的主键连接

        int saved = saveCourseMarket(courseMarket);
        if (saved <= 0) {
            throw new EduPlusException("保存课程营销信息失败");
        }

        return getCourseBaseInfo(courseBase.getId());
    }

    /**
     * 保存课程营销信息
     * @param courseMarket
     * @return 保存结果
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        // 1. validate market info
        // charge is null -> throw exception
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new RuntimeException("收费规则没有选择");
        }

        // if charge required -> check pricing info
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }

        // 2. save info
        // select info by id
        CourseMarket courseMarketSelect = courseMarketMapper.selectById(courseMarket.getId());
        // if not exists -> insert
        if (courseMarketSelect == null) {
            return courseMarketMapper.insert(courseMarket);
        } else { // if exists -> update
            BeanUtils.copyProperties(courseMarket, courseMarketSelect);
            courseMarketSelect.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketSelect);
        }
    }

    /**
     * 查询课程信息
     * @param courseId 课程id
     * @return 课程info dto（包括base info和market info）
     */
    private CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        // get base info (required)
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }

        // get market info
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // copy baseInfo and marketInfo into dto
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        // base&market里没有mtname和stname -> 需要去course category里查询
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }
}



