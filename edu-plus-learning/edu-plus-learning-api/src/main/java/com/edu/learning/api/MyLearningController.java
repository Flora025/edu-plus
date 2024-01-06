package com.edu.learning.api;

import com.edu.base.model.RestResponse;
import com.edu.learning.service.LearningService;
import com.edu.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的学习接口
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {

    @Autowired
    LearningService learningService;

    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("courseId") Long teachplanId, @PathVariable("mediaId") String mediaId) {

        //get userid
        SecurityUtil.XcUser user = SecurityUtil.getUser(); //获取当前线程绑定的用户
        String userId = null;
        if (user != null) {
            userId = user.getId();
        }

        return learningService.getVideo(userId, courseId, teachplanId, mediaId);
    }

}
