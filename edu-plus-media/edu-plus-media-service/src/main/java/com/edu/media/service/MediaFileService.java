package com.edu.media.service;

import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.media.model.dto.QueryMediaParamsDto;
import com.edu.media.model.dto.UploadFileParamsDto;
import com.edu.media.model.dto.UploadFileResultDto;
import com.edu.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @description 媒资文件查询方法
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件
     *
     * @param companyId           公司id
     * @param uploadFileParamsDto 上传文件信息
     * @param localFilePath       文件本地路径
     * @return 文件信息返回结果
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName);
}
