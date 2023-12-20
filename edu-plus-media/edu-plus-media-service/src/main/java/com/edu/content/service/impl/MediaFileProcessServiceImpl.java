package com.edu.content.service.impl;

import com.edu.media.mapper.MediaFilesMapper;
import com.edu.media.mapper.MediaProcessHistoryMapper;
import com.edu.media.mapper.MediaProcessMapper;
import com.edu.media.model.po.MediaFiles;
import com.edu.media.model.po.MediaProcess;
import com.edu.media.model.po.MediaProcessHistory;
import com.edu.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// media process的接口实现
@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
        return mediaProcesses;
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }

    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 更新任务处理结果
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            // 不存在任务
            return;
        }

        // 1. 如果任务执行失败 -> fail count +1
        if (status.equals("3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);

            mediaProcessMapper.updateById(mediaProcess);
            return; // 不用再继续了
        }

        // 2. 如果任务执行成功 -> 拿到url 更新到media_file中的url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        // 更新media process表状态
        mediaProcess.setStatus("2"); // 已完成
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 把media process表记录插入到history（代表已完成）
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 从该任务表media process中删除
        mediaProcessMapper.deleteById(taskId);
    }
}
