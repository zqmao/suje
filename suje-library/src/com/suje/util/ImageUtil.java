package com.suje.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtil {

	public static int MAX_IMAGE_HEIGHT = 1000;// 图片最大高度
	public static int MAX_IMAGE_WIDTH = 1000;// 图片最大宽度
	public static final int MAX_ARTICLE_SIZE = 1000 * 1000/3; 
	public static final int IMAGE_COMPRESSION_QUALITY = 60;
	public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;
	/**
	 * 压缩次数
	 */
	private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;
	private static int srcWidth;
	private static int srcHeight;
	private static String srcImagePath;
	/**
	 * 是否需要压缩
	 */
	public static boolean isNeedCompress = false;

	/**
	 * 压缩图片
	 * 
	 * @param srcPath
	 *            源图片路径
	 * @return
	 */
	public static Bitmap compress(String srcPath) {
		MAX_IMAGE_HEIGHT = 1000;// 图片最大高度
		MAX_IMAGE_WIDTH = 1000;// 图片最大宽度
		srcImagePath = srcPath;
		isNeedCompress = decodeBoundsInfo();
		if (!isNeedCompress) {
			return BitmapFactory.decodeFile(srcPath);
		}
		byte[] data = getResizedImageData();
		if (data != null) {
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		return null;
	}

	private static byte[] getResizedImageData() {
		
		InputStream input = null;
		try {
			//压缩图片的size
			ByteArrayOutputStream os = null;
			int attempts = 1;
			int sampleSize = 1;
			BitmapFactory.Options options = new BitmapFactory.Options();
			int quality = IMAGE_COMPRESSION_QUALITY;
			Bitmap b = null;
			do {
				options.inSampleSize = sampleSize;
				try {
					b = BitmapFactory.decodeFile(srcImagePath, options);
					if (b == null) {
						return null; 
					}
				} catch (OutOfMemoryError e) {
					//如果图片太大，每次2倍的压缩
					sampleSize *= 2; 
					attempts++;
					continue;
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} while (b == null && attempts < NUMBER_OF_RESIZE_ATTEMPTS);
			if (b == null) {
				return null;
			}
			//控制失真率
			float scaleFactor = 1.F;
			while ((srcWidth * scaleFactor > MAX_IMAGE_WIDTH) || (srcHeight * scaleFactor > MAX_IMAGE_HEIGHT)) {
				scaleFactor *= .75F;
			}
			boolean resultTooBig = true;
			attempts = 1; 
			do {
				try {
					if (options.outWidth > MAX_IMAGE_WIDTH || options.outHeight > MAX_IMAGE_HEIGHT || (os != null && os.size() > MAX_ARTICLE_SIZE)) {
						int scaledWidth = (int) (srcWidth * scaleFactor);
						int scaledHeight = (int) (srcHeight * scaleFactor);
						b = Bitmap.createScaledBitmap(b, scaledWidth, scaledHeight, false);
						if (b == null) {
							return null;
						}
					}
					os = new ByteArrayOutputStream();
					b.compress(CompressFormat.JPEG, quality, os);
					int jpgFileSize = os.size();
					if (jpgFileSize > MAX_ARTICLE_SIZE) {
						quality = (quality * MAX_ARTICLE_SIZE) / jpgFileSize; // watch
						if (quality < MINIMUM_IMAGE_COMPRESSION_QUALITY) {
							quality = MINIMUM_IMAGE_COMPRESSION_QUALITY;
						}
						os = new ByteArrayOutputStream();
						b.compress(CompressFormat.JPEG, quality, os);
					}
				} catch (java.lang.OutOfMemoryError e) {
					e.printStackTrace();
				}
				scaleFactor *= .75F;
				attempts++;
				resultTooBig = os == null || os.size() > MAX_ARTICLE_SIZE;
			} while (resultTooBig && attempts < NUMBER_OF_RESIZE_ATTEMPTS);
			b.recycle(); 
			return resultTooBig ? null : os.toByteArray();
		} catch (java.lang.OutOfMemoryError e) {
			return null;
		}
	}
	
	/**
	 * 解析原图片宽高
	 * 
	 * @param srcPath
	 */
	private static boolean decodeBoundsInfo() {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(srcImagePath, opt);//Bitmap srcMap = 
		srcWidth = opt.outWidth;
		srcHeight = opt.outHeight;
		if(srcWidth == 0){
			return false;
		}
		if(srcWidth < srcHeight){
			MAX_IMAGE_WIDTH = MAX_IMAGE_HEIGHT * srcWidth / srcHeight;
		} else {
			MAX_IMAGE_HEIGHT = MAX_IMAGE_WIDTH * srcHeight / srcWidth;
		}
		
		return srcWidth > MAX_IMAGE_WIDTH;
	}
	
	/** 
     *  
     * @param imgPath 
     * @param bitmap 
     * @return 
     */  
    public static String imgToBase64(Bitmap bitmap) {  
        if(bitmap == null){  
        	
        }  
        ByteArrayOutputStream out = null;  
        try {  
            out = new ByteArrayOutputStream();  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  
            out.flush();  
            out.close();  
            byte[] imgBytes = out.toByteArray();  
            return Base64.encodeToString(imgBytes, Base64.DEFAULT);  
        } catch (Exception e) {  
            return null;  
        } finally {  
            try {  
                out.flush();  
                out.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
    
    /** 
     *  
     * @param base64Data 
     * @param imgName 
     * @param imgFormat 图片格式 
     */  
    public static Bitmap base64ToBitmap(String base64Data,String imgName,String imgFormat) {  
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);  
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);  
        return bitmap;
    }  
}
