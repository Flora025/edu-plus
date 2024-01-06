package com.edu.learning.service.impl;

import com.edu.base.exception.EduPlusException;
import com.edu.base.model.RestResponse;
import com.edu.content.model.po.CoursePublish;
import com.edu.learning.feignclient.ContentServiceClient;
import com.edu.learning.feignclient.MediaServiceClient;
import com.edu.learning.model.dto.XcCourseTablesDto;
import com.edu.learning.service.LearningService;
import com.edu.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {
    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        // 查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        if (coursePublish == null) {
            EduPlusException.cast("课程信息不存在");
        }

        // 校验学习资格
        if (StringUtils.isNotEmpty(userId)) {
            // 在已登陆的情况下
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if (learnStatus.equals("702001")) {
                // 如果学习状态为正常学习 返回对应媒资
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702003")) {
                RestResponse.validfail("选课已过期，请续期");
            }
        }

        // 如果用户未登陆
        String charge = coursePublish.getCharge();
        if (charge.equals("201000")) {
            // 课程免费 -> 可以正常学习 -> 返回媒资
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");

    }
}
