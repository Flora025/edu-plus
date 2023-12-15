package com.edu.content.service;

import com.edu.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
