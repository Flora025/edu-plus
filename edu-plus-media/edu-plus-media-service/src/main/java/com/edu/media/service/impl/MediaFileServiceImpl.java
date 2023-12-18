package com.edu.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.base.exception.EduPlusException;
import com.edu.base.model.PageParams;
import com.edu.base.model.PageResult;
import com.edu.media.mapper.MediaFilesMapper;
import com.edu.media.model.dto.QueryMediaParamsDto;
import com.edu.media.model.dto.UploadFileParamsDto;
import com.edu.media.model.dto.UploadFileResultDto;
import com.edu.media.model.po.MediaFiles;
import com.edu.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @version 1.0
 * @description TODO
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucketFiles;


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

        // 将文件存储到数据库
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketFiles, objectName);

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
    private MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
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
        return mediaFiles;
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
    private boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
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
}
