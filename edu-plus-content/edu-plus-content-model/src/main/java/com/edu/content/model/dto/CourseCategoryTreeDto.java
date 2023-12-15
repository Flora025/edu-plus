package com.edu.content.model.dto;

import com.edu.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    // add an attribute to an existing PO
    List<CourseCategoryTreeDto> childrenTreeNodes;

}
