package com.edu.media.service;

import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.base.model.RestResponse;
import com.edu.media.model.dto.QueryMediaParamsDto;
import com.edu.media.model.dto.UploadFileParamsDto;
import com.edu.media.model.dto.UploadFileResultDto;
import com.edu.media.model.po.MediaFiles;

import java.io.File;

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
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件
     *
     * @param companyId           公司id
     * @param uploadFileParamsDto 上传文件信息
     * @param localFilePath       文件本地路径
     * @return 文件信息返回结果
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName);

    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName);

    /**
     * 从minio下载文件到本地
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * 上传文件到minio
     * @param localFilePath
     * @param mimeType
     * @param bucket bucket名
     * @param objectName
     * @return 返回是否成功
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);
    /**
     * 检查文件是否存在
     * @param fileMd5 md5编码
     * @return true=存在
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     * @param fileMd5 md5编码
     * @param chunkIndex 分块序号
     * @return true=存在
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     * @param fileMd5
     * @param chunk
     * @param localChunkFilePath
     * @return
     */
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    /**
     * 合并分块
     * @param companyId 机构id
     * @param fileMd5 文件md5编码
     * @param chunkTotal 分块总数
     * @param uploadFileParamsDto 文件信息dto
     * @return 合并结果
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

    /**
     * 根据媒资id查询文件
     * @param mediaId 媒资id
     * @return MediaFiles
     */
    MediaFiles getFileById(String mediaId);
}
