package com.atguigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-11 16:39
 * description:
 **/
public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile) {

        String imgUrl ="http://192.168.38.177";

        //获得配置文件的路径
        //配置fdfs的全局链接地址
        String tracker =PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient =new TrackerClient();

        //获得一个trackServer的实例
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //通过tracker获得一个Storage连接客户端
        StorageClient storageClient =new StorageClient(trackerServer,null);

        try {
            //获得上传的二进制文件
            byte[] multipartFileBytes = multipartFile.getBytes();

            //获得文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();//获得文件名
            System.out.println(originalFilename);
            int i = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i+1);

            String[] jpgs = storageClient.upload_file(multipartFileBytes, extName, null);

            for (String jpg: jpgs){
                imgUrl += "/"+jpg;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        return imgUrl;
    }
}
