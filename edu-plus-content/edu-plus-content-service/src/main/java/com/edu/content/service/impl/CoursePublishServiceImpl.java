package com.edu.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.edu.base.exception.CommonError;
import com.edu.base.exception.EduPlusException;
import com.edu.content.config.MultipartSupportConfig;
import com.edu.content.feignclient.MediaServiceClient;
import com.edu.content.mapper.CourseBaseMapper;
import com.edu.content.mapper.CourseMarketMapper;
import com.edu.content.mapper.CoursePublishMapper;
import com.edu.content.mapper.CoursePublishPreMapper;
import com.edu.content.model.dto.CourseBaseInfoDto;
import com.edu.content.model.dto.CoursePreviewDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.model.po.CourseBase;
import com.edu.content.model.po.CourseMarket;
import com.edu.content.model.po.CoursePublish;
import com.edu.content.model.po.CoursePublishPre;
import com.edu.content.service.CourseBaseInfoService;
import com.edu.content.service.CoursePublishService;
import com.edu.content.service.TeachplanService;
import com.edu.messagesdk.model.po.MqMessage;
import com.edu.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

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

    @Override
    public void publish(Long companyId, Long courseId) {
        // 查询prepub表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            EduPlusException.cast("课程无审核记录");
        }


        // validation
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")) {
            EduPlusException.cast("课程未审核通过");
        }

        // 向课程pub表写入相关数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);

        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            // insert
            coursePublishMapper.insert(coursePublish);
        } else {
            // update
            coursePublishMapper.updateById(coursePublish);
        }
        // 向消息表写入数据 TODO
        saveCoursePublishMessage(courseId);

        // 将prepub表数据删除
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;

        try {
            // 配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            // 加载模板
            // 选指定模板路径,classpath下templates下
            // 得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");

            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            // 准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            // 静态化
            // 参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            // 创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            // 输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}", e.toString());
            EduPlusException.cast("课程静态化异常");
        }

        return htmlFile;

    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
        if (course == null) {
            EduPlusException.cast("上传静态文件异常"); // 然后开始熔断降级
        }

    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            EduPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }


}
