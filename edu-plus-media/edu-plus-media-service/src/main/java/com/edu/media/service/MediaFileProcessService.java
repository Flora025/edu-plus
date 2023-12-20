package com.edu.media.service;

import com.edu.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * 获取待处理任务
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return 开启成功或失败
     */
    boolean startTask(long id);


    /**
     * 保存任务结果
     *
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     * @return void
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

}
