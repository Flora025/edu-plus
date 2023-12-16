package com.edu.content.service.impl;

import com.edu.content.mapper.TeachplanMapper;
import com.edu.content.model.dto.TeachplanDto;
import com.edu.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> getTeachplanTreeNodes(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}
