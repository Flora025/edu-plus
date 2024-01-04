package com.edu.learning.service;

import com.edu.learning.model.dto.XcChooseCourseDto;
import com.edu.learning.model.dto.XcCourseTablesDto;

/**
 * 选课相关接口
 */
public interface MyCourseTablesService {
    /**
     * 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return 选课信息dto
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 判断学习资格（正常学习/没有选课或选课后未支付/过期需续期）
     * @param userId
     * @param courseId
     * @return
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

}
