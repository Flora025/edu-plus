package com.edu.learning;

import com.edu.content.model.po.CoursePublish;
import com.edu.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FeignClientTest {

    @Autowired
    ContentServiceClient contentServiceClient;

    // 测试feign client远程调用content接口
    @Test
    public void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(120L);
        Assertions.assertNotNull(coursepublish);
    }
}