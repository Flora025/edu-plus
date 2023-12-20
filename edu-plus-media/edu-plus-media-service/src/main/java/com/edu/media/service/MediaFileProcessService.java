package com.edu.media.service;

import com.edu.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * 获取待处理任务
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启一个任务
     * @param id 任务id
     * @return 开启成功或失败
     */
    public boolean startTask(long id);


}
