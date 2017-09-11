package com.suje.util;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zqmao on 2017/8/24.
 */

public class FileMetaUtil {
    private static final int LENGTH = 1024 * 50;

    public static Meta getFileMeta(File file){
        Meta meta = new Meta(file);
        meta.parse();
        return meta;
    }

    public static class Meta{
        private String header;
        private String tail;
        private String md5;
        private File mFile;
        public Meta(File mFile) {
            super();
            this.mFile = mFile;
        }

        public void parse(){
            this.header = getHeaderMD5(mFile);
            this.md5 = getFileMD5(mFile);
            this.tail = getTailMD5(mFile);
        }

        private String base64(byte[] byts){
            if(byts == null){
                return "";
            }

            String str = Base64.encodeToString(byts, Base64.DEFAULT);
            if(str.endsWith("\n")){
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }

        /**
         * 解析全文件
         * @param file
         * @return
         */
        private String getFileMD5(File file){
            FileInputStream input = null;
            try {
                input = new FileInputStream(file);
                int len;
                byte[] byts = new byte[LENGTH];
                MdUtil.MD5Hander handler = MdUtil.getMD5Hander();
                while((len = input.read(byts)) >= 0){
                    handler.append(byts, 0, len);
                }
                return handler.getMD5String();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(input != null){
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        /**
         * 尾md5；
         * @param file
         * @return
         */
        private String getTailMD5(File file){
            long fileLength = file.length();
            RandomAccessFile access = null;
            try{
                access = new RandomAccessFile(file, "r");
                if(LENGTH * 2 <= fileLength){
                    byte[] byts = new byte[LENGTH];
                    access.seek(fileLength-LENGTH);
                    access.read(byts);
                    return MdUtil.MD5(byts);
                }else{
                    long length = fileLength - (fileLength/2);
                    if(length > 0){
                        byte[] byts = new byte[(int)length];
                        access.seek(fileLength-length);
                        return MdUtil.MD5(byts);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(access != null){
                    try {
                        access.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        /**
         * 头md5
         * @param file
         * @return
         */
        private String getHeaderMD5(File file){
            long fileLength = file.length();
            FileInputStream input = null;
            try{
                if(fileLength < 256){
                    return "";
                }else if(fileLength > 256 && fileLength < 256 * 1024){
                    byte[] byts = new byte[256];
                    input = new FileInputStream(file);
                    input.read(byts);
                    return MdUtil.MD5(byts);
                }else{
                    byte[] byts = new byte[256 * 1024];
                    input = new FileInputStream(file);
                    input.read(byts);
                    return MdUtil.MD5(byts);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(input != null){
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        public String getHeader() {
            return header;
        }
        public void setHeader(String header) {
            this.header = header;
        }
        public String getTail() {
            return tail;
        }
        public void setTail(String tail) {
            this.tail = tail;
        }
        public String getMd5() {
            return md5;
        }
        public void setMd5(String md5) {
            this.md5 = md5;
        }
    }
}
