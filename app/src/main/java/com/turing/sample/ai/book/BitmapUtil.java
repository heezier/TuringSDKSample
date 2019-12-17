package com.turing.sample.ai.book;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;



public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    public static int WIDTH = 640;
    public static int HEIGHT = 480;

    public static int MIU_WIDTH = 320;
    public static int MIU_HEIGHT = 240;

    public interface OnSaveImgListener{
        void onSuccess(String filePath);
        void onFiled(String meaasge);
    }

    public static void saveYuvTpJpg(String path, byte[] data, boolean ocr, OnSaveImgListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String srcpath = null;
                Bitmap bitmap = yuvToRGB(data, ocr);
                Log.d(TAG, "bitmapWidth: " + bitmap.getWidth() + "    bitmapHeight: " + bitmap.getHeight());
                File imageFile = new File(path);
                if (!Build.BRAND.contains("3Q")) {
                    Bitmap rotate = BitmapUtil.rotateBitmap(bitmap);
                    srcpath = BitmapUtil.saveToInternalStorage(imageFile, rotate);
                } else {
                    srcpath = BitmapUtil.saveToInternalStorage(imageFile, bitmap);
                }
//                srcpath = BitmapUtil.saveToInternalStorage(imageFile, bitmap);
                if(TextUtils.isEmpty(srcpath)){
                    listener.onFiled("Picture save failed ！");
                }else{
                    listener.onSuccess(srcpath);
                }
            }
        }).start();
    }

    /**
     * 将yuv格式的byte数组转化成RGB的bitmap
     */
    public static Bitmap yuvToRGB(byte[] data, boolean ocr) {
        int width = WIDTH;
        int height = HEIGHT;
        int quality = 80;
        if (!ocr) {
            width = 320;
            height = 240;
            quality = 20;
        }
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), quality, baos);
        Bitmap mBitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
        return mBitmap;
    }

    public static Bitmap bitMapScale(Bitmap bitmap,float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        bitmap = null;
        return resizeBmp;
    }

    /**
     * 保存Yuv数据到Jpg文件中
     * 暂时没有清理机制，考虑如果将来图片过大，可能需要清理一下。
     *
     * @param data
     * @param destPath
     */
    public static void saveYuvToJpgFile(byte[] data, int width, int height, String destPath) {
        File file = new File(destPath);
        if (file.exists()) {
            file.delete();
        }
        boolean created = false;
        FileOutputStream fos = null;
        try {
            created = file.createNewFile();
            fos = new FileOutputStream(file);
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
            yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将argb_8888的图像转化为w*h*4的字节数组，算法使用
     */
    public static byte[] bitmap2byte(final Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bytes = byteBuffer.array();
        return bytes;
    }

    /**
     * 将图片按照固定比例进行压缩
     */
    public static Bitmap resizeBitmapWithConstantWHRatio(Bitmap bmp, int mWidth, int mHeight) {
        if (bmp != null) {
            Bitmap bitmap = bmp;
            float width = bitmap.getWidth(); //728
            float height = bitmap.getHeight(); //480
            Log.d(TAG, "----原图片的宽度:" + bmp.getWidth() + ", 高度:" + bmp.getHeight()); //720/480 = 1.5

            float scale = 1.0f;
            float scaleX = (float) mWidth / width;
            float scaleY = (float) mHeight / height;
            if (scaleX < scaleY && (scaleX > 0 || scaleY > 0)) {
                scale = scaleX;
            }
            if (scaleY <= scaleX && (scaleX > 0 || scaleY > 0)) {
                scale = scaleY;
            }

            Log.d(TAG, "-----scaleX:" + scale + " , scaleY:" + scale);
            return resizeBitmapByScale(bmp, scale);
        }
        return null;
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        float width = bitmap.getWidth(); //728
        float height = bitmap.getHeight(); //480
        Bitmap bmpOut = Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, true);
        return bmpOut;
    }

    /***将bitmap写进指定路径*/
    public static String saveToInternalStorage(File imageFile, Bitmap bitmapImage) {
        String path = "";
        if (!imageFile.exists()) {
            try {
                imageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            path = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bitmapImage.recycle();
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path; //得到.jpg的全路径名
    }

    /**
     * bitmap转化为byte[]数组，网络传输使用
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        return baos.toByteArray();
    }

    /**
     * sx, sy -1, 1   左右翻转   1, -1  上下翻转
     */
    public static Bitmap rotaingImageView(int angle, Bitmap srcBitmap, float sx, float sy) {
        Matrix matrix = new Matrix();  //使用矩阵 完成图像变换
        if (sx != 0 || sy != 0) {
            matrix.postScale(sx, sy);  //重点代码，记住就ok
        }

        int w = srcBitmap.getWidth();
        int h = srcBitmap.getHeight();
        Bitmap cacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cacheBitmap);  //使用canvas在bitmap上面画像素

        matrix.postRotate(angle);
        Bitmap retBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, matrix, true);
        canvas.drawBitmap(retBitmap, new Rect(0, 0, w, h), new Rect(0, 0, w, h), null);
        return retBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

}
