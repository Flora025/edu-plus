package com.edu.content.service;

import com.edu.content.model.dto.CoursePreviewDto;

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
}
