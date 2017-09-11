package com.suje.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by zqmao on 2017/8/8.
 */

public class FileUtil {

    private static int CHUNK_SIZE = 5 * 1024 * 1024;

    public static File getFile(Context context, String id, String url){
        String fileName = "";
        int start = url.lastIndexOf("/");
        int end = url.indexOf("?");
        if(start > 0){
            if(end != -1){
                fileName = url.substring(start + 1, end);
            } else {
                fileName = url.substring(start + 1);
            }
        }
        int preIndex = fileName.lastIndexOf(".");
        if(preIndex != -1){
            fileName = fileName.substring(0, preIndex) + "_" + id + fileName.substring(preIndex);
        }else{
            fileName = fileName + id;
        }
        try {
			fileName = java.net.URLDecoder.decode(fileName,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        String path = Contants.FILE_DOWNLOAD_PATH + fileName;
        File file = new File(path);
        File dir = file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        return file;
    }

    /**
     *
     * @param fileId 数据库存储主键
     * @param filePath 文件路径
     */
    public static void chunkFile(String fileId, String filePath, Runnable run){
        new ChunkFileThread(fileId, filePath, run).start();
    }

    public static void deleteChunkFile(String fileId, int partId){
        File file = getChunkFile(fileId, partId);
        if(file.exists()){
            file.delete();
        }
    }

    public static File getChunkFile(String fileId, int partId){
        File file = new File(Contants.FILE_CHUNKS_PATH + fileId + "_" + partId);
        return file;
    }

    public static int getChunkCount(String filePath){
        File file = new File(filePath);
        int count = (int) (file.length() / CHUNK_SIZE);//总块数
        if(count == 0){
            count += 1;
        }
        return count;
    }

    public static long getChunkSize(String filePath, int partId){
        File file = new File(filePath);
        int total = getChunkCount(filePath);
        if(total < 1){
            return 0L;
        }else if(total == 1){
            if(partId == 1){
                return file.length();
            }else{
                return 0L;
            }
        }else{
            if(partId < total){
                return partId * CHUNK_SIZE;
            }else {
                return file.length();
            }
        }
    }

    static class ChunkFileThread extends Thread {
    	private String fileId;
    	private String filePath;
    	private Runnable run;
    	
    	public ChunkFileThread(String fileId, String filePath, Runnable run) {
			super();
			this.fileId = fileId;
			this.filePath = filePath;
			this.run = run;
		}

		@Override
    	public void run() {
    		super.run();
    		String outPath = Contants.FILE_CHUNKS_PATH;
            try {
                chunkFile(fileId, filePath, outPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(run);
    	}
    }

    /**
     * 文件分片
     * @param filePath
     * 			需要分片的文件
     * @param outPath
     * 			分片文件存储路径
     * @throws Exception
     */
    private static void chunkFile(String fileId, String filePath,String outPath) throws Exception{
        File file = new File(filePath);
        //检查需要分片的文件是否存在
        if(file == null || !file.exists()){
            throw new Exception("文件不存在");
        }
        File dir = new File(outPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        int partId = 1;//当前块索引
        int totalChunkCount = getChunkCount(filePath);//总块数

        long pos = 0;
        for(;partId<=totalChunkCount;partId++){
            RandomAccessFile raf  = new RandomAccessFile(file, "r");//负责读取数据
            if(partId>1){
                raf.seek(pos);//跳过前[块索引*大小]
            }
            //当前块存储路径
            OutputStream out = new FileOutputStream(outPath + fileId + "_" + partId);
            int bufferLen = 4096;
            byte[] buffer = new byte[bufferLen];//暂存容器
            int n = 0;

            //最后一块
            if(partId==totalChunkCount){
                while ((n = raf.read(buffer,0,bufferLen)) != -1) {
                    out.write(buffer, 0, n);
                    pos += n;
                }
            }else{
                //记录已读字节数
                long readLength = 0;
                while(readLength <= CHUNK_SIZE - 1024){
                    n = raf.read(buffer,0,bufferLen);
                    out.write(buffer, 0, n);
                    readLength += n;
                    pos += n;
                }
            }
            out.close();
            raf.close();
        }
    }
}
