package com.edu.content.service.impl;

import com.edu.content.mapper.CourseCategoryMapper;
import com.edu.content.model.dto.CourseCategoryTreeDto;
import com.edu.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 1. get list of dtos from mapper
        // 最终：a list of dtos, 每个dto的children也是a list of dtos
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 2. handle list
        // add all root nodes to a map <id : NodeDto>
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !item.getId().equals(id))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        // return list
        List<CourseCategoryTreeDto> finalcategoryTreeDtos = new ArrayList<> ();
        courseCategoryTreeDtos.stream()
                .filter(item -> !item.getId().equals(id))
                .forEach(curNode -> {
                    // add direct children to the list
                    if (curNode.getParentid().equals(id)) {
                        finalcategoryTreeDtos.add(curNode);
                    }
                    // get curNode's parent from map
                    CourseCategoryTreeDto parentCourseCategoryTreeDto = mapTemp.get(curNode.getParentid());
                    if (parentCourseCategoryTreeDto != null) {
                        if (parentCourseCategoryTreeDto.getChildrenTreeNodes() == null) {
                            parentCourseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<>());
                        }
                        // add curNode to parent's list
                        parentCourseCategoryTreeDto.getChildrenTreeNodes().add(curNode);
                    }
                });

        return finalcategoryTreeDtos;
    }
}
