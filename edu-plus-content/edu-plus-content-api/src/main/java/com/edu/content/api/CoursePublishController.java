package com.edu.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CoursePublishController {
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();

        //设置模型数据
//        modelAndView.addObject("name","小明");

        //设置模板名称
        modelAndView.setViewName("course_template"); // 会自动拼接后缀
        return modelAndView;
    }
}
