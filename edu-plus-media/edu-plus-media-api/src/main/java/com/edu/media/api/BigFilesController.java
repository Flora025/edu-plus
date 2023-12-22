package com.edu.media.api;

import com.edu.base.model.RestResponse;
import com.edu.media.model.dto.UploadFileParamsDto;
import com.edu.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFilesController {
    /*
        总结一下流程：
        本地上传文件到前端
        前端分块
        分块传给媒资服务
        媒资服务接受分块并保存到本地临时文件
        媒资服务上传到minio并写库
     */
    @Autowired
    MediaFileService mediaFileService;

    // 上传前：检查文件
    @ApiOperation(value = "上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam String fileMd5) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }

    // 上传前：检测分块文件
    @ApiOperation(value = "检测分块文件")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }


    // 上传文件
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        Long companyId = 1232141425L;

        // get file path
        String localFilePath = tempFile.getAbsolutePath();
        RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);

        return restResponse;
    }

    // 合并文件
    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");

        return mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);

    }

}
