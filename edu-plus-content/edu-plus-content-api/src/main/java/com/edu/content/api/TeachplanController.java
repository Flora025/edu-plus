package com.edu.content.api;

import com.edu.content.model.dto.BindTeachplanMediaDto;
import com.edu.content.model.dto.SaveTeachplanDto;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划编辑接口
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.getTeachplanTreeNodes(courseId);
    }

    @ApiOperation("创建/修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("删除课程计划")
    @ApiImplicitParam(value = "planId", name = "计划Id", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/teachplan/{planId}")
    public void deleteTeachplan(@PathVariable Long planId) {
        teachplanService.deleteTeachplan(planId);
    }

    @ApiOperation("移动课程计划")
    @ApiImplicitParam(value = "planId", name = "计划Id", required = true, dataType = "Long", paramType = "path")
    @PostMapping("/teachplan/{direction}/{planId}")
    public void moveTeachplan(@PathVariable String direction, @PathVariable Long planId) {
        teachplanService.moveTeachplan(direction, planId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

}
