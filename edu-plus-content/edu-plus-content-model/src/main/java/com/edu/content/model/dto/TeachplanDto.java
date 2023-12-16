package com.edu.content.model.dto;

import com.edu.content.model.po.Teachplan;
import com.edu.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeachplanDto extends Teachplan {
    TeachplanMedia teachplanMedia;

    List<TeachplanDto> teachplanTreeNodes;
}
