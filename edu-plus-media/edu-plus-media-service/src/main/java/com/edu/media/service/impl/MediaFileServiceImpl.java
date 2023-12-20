package com.edu.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.base.exception.EduPlusException;
import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.base.model.RestResponse;
import com.edu.media.mapper.MediaFilesMapper;
import com.edu.media.mapper.MediaProcessMapper;
import com.edu.media.model.dto.QueryMediaParamsDto;
import com.edu.media.model.dto.UploadFileParamsDto;
import com.edu.media.model.dto.UploadFileResultDto;
import com.edu.media.model.po.MediaFiles;
import com.edu.media.model.po.MediaProcess;
import com.edu.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @description TODO
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {
    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            EduPlusException.cast("文件不存在");
        }

        // 文件名
        String filename = uploadFileParamsDto.getFilename();

        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        // 文件mimetype
        String mimeType = getMimeType(extension);

        // 文件md5编码
        String fileMd5 = getFileMd5(file);

        // 文件默认目录
        String defaultFolderPath = getDefaultFolderPath();

        // 存储到minio中的对象名
        String objectName = defaultFolderPath + fileMd5 + extension;

        // 将文件上传到minio
        boolean uploaded = addMediaFilesToMinIO(localFilePath, mimeType, bucketFiles, objectName);
        uploadFileParamsDto.setFileSize(file.length());

        // 将文件存储到数据库 使用代理对象
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketFiles, objectName);
        // 准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 将文件信息写入db
     *
     * @param companyId           公司id
     * @param fileMd5             md5编码
     * @param uploadFileParamsDto 文件信息dto
     * @param bucket              bucket名
     * @param objectName          实例名
     * @return mediafile实例
     */
    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        // 查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            // 如果条目不存在 创建并填入信息
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5); // 主键是md5
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");

            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("保存文件信息到数据库失败");
                EduPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
        }
        // 添加到待处理任务表（需要和入库放在同一个事务）
        addWaitingTask(mediaFiles);
        log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 上传的媒资文件
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        // 如果是avi视频 -> 添加到视频待处理表格
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();

            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1"); // 未处理
            mediaProcess.setFailCount(0); // 失败次数默认为0
            mediaProcess.setUrl(null);;

            mediaProcessMapper.insert(mediaProcess);
        }
    }


    /**
     * 将文件写入minIO
     *
     * @param localFilePath 本地路径
     * @param mimeType      mimetype
     * @param bucket        bucket名
     * @param objectName
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            // init upload实例 并上传
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);

            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
//            System.out.println("上传成功");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
            EduPlusException.cast("上传文件到文件系统失败");
        }
        return false;

    }

    /**
     * 获取文件默认存储目录路径 年/月/日
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * 获取文件的md5
     *
     * @param file 文件
     * @return md5编码
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据文件扩展名 返回mimetype
     */
    private String getMimeType(String extension) {
        if (extension == null)
            extension = "";

        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        // 通用mimeType 字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            // 如果md5在数据库存在 -> 查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();

            //查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        // 文件流
        InputStream fileInputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                // 如果能读到 说明分块已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {

        }
        // 分块未存在
        return RestResponse.success(false);

    }

    //得到分块文件的目录

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0)
                + "/" + fileMd5.charAt(1)
                + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        // 分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        // 分块上传到minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 1 ----- 找到分块文件 调用minio sdk合并文件
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        //文件名称
        String fileName = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(mergeFilePath) // 最终合并后的文件
                .sources(sourceObjectList)
                .build();

        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            // 合并失败
            e.printStackTrace();
            log.error("合并文件出错 bucket:{}, objectName:{},error:{}",
                    bucket_video,
                    mergeFilePath,
                    e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        // 2 ------- 校验合并后文件是否和原文件一致
        // 1. 下载文件
        File file = downloadFileFromMinIO(bucket_video, mergeFilePath);
        // 2. 计算合并后文件md5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            // compare
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error("校验合并文件md5不一致");
                return RestResponse.validfail(false, "文件校验失败");
            }
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        // 3 ------- 成功->文件信息入库
        // 需要用代理对象调 否则事务不生效
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5,
                uploadFileParamsDto, bucket_video, mergeFilePath);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }

        // 4 ------- 清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }
    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        // 临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            // 创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }


}
