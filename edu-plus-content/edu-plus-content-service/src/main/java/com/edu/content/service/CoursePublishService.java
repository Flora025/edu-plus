package com.edu.content.service;

import com.edu.content.model.dto.CoursePreviewDto;
import com.edu.content.model.po.CoursePublish;

import java.io.File;

/**
 * 课程发布接口
 */
public interface CoursePublishService {
    /**
     * 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId 公司id
     * @param courseId 课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     */
    void publish(Long companyId, Long courseId);

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     */
    public void  uploadCourseHtml(Long courseId,File file);

    CoursePublish getCoursePublish(Long courseId);

    /**
     * @description 查询缓存中的课程信息
     * @param courseId id of the course
     */
    public CoursePublish getCoursePublishCache(Long courseId);

}
