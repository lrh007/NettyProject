package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.client.bean.ImageData;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 工具类
 *
 * @Author lrh 2020/9/23 17:27
 */
public class Util {


    /**
     * 使用gzip压缩字符串
     * @param str 要压缩的字符串
     * @return
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * 使用gzip解压缩
     * @param compressedStr 压缩字符串
     * @return
     */
    public static String uncompress(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = Base64.getDecoder().decode(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }
    /**
     * 编码和压缩字符串
     * @Author lrh 2020/9/25 13:50
     */
    public static String encodeAndCompress(byte[] src){
        String s = Base64.getEncoder().encodeToString(src);
        return compress(s);
    }
    /**
     * 解码和解压缩字符串
     * @Author lrh 2020/9/25 13:50
     */
    public static byte[] decodeUnCompress(String data){
        String s = uncompress(data);
        return Base64.getDecoder().decode(s);
    }
    /**
     * 分割图片并给图片进行编号
     * @Author lrh 2020/9/28 12:15
     */
    public static Map<Integer,ImageData> splitImageAndNum(int screenWidth, int screenHeight, int marginLeft, BufferedImage sourceBufferedImage){
        Map<Integer,ImageData> dataMap = new HashMap<>();
        int width_interval = 10; //宽度分成10份
        int height_interval = 2; //高度分成2份
        int width = screenWidth / width_interval;  //图片宽度
        int height = screenHeight / height_interval; //图片高度
        int x = marginLeft; //图片x坐标
        int y = 0; //图片y坐标
        //分割图片
        BufferedImage newBufferedImage = null;
        int index = 0; //图片编号
        for (int i = 0; i < height_interval; i++) {
            for (int j = 0; j < width_interval; j++) {
                dataMap.put(index,new ImageData(null,false,x,y,height,width,sourceBufferedImage.getSubimage(x, y, width, height),index));
                System.out.println("分割图片位置[number="+index+",x="+x+",y="+y+",width="+width+",height="+height+"]");
                x += width;
                if(j+1 >= width_interval){
                    x = marginLeft;
                }
                index ++;
            }
            y+= height;
        }
        return dataMap;
    }
    /**
     * 比较前后两张图片之前是否相同，如果不相同则将之前的图片更新，否则不动
     * @Author lrh 2020/10/9 11:23
     */
    public static boolean compareImageData(int imageNumber,BufferedImage newImageData,Map<Integer, ImageData> beforeData){
        int height = newImageData.getHeight();
        int width = newImageData.getWidth();
        ImageData imageData = beforeData.get(imageNumber);
        BufferedImage beforeBufferedImage = imageData.getBufferedImage();
        for (int x=0;x<width;x++){
            for (int y = 0;y<height;y++){
                int newRGB = newImageData.getRGB(x, y);
                int oldRGB = beforeBufferedImage.getRGB(x, y);
                if(newRGB != oldRGB){
                    System.out.println("图片前后不相同，进行替换，图片编号= "+imageNumber);
                    imageData.setBufferedImage(newImageData);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取图像异或操作后的结果
     * @Author lrh 2020/10/9 14:44
     */
    public static BufferedImage getXorImageData(int imageNumber,BufferedImage newImageData,Map<Integer, ImageData> beforeData){
        BufferedImage beforeBufferedImage = beforeData.get(imageNumber).getBufferedImage();
        boolean b = compareImageData(imageNumber, newImageData, beforeData);
        if(!b){ //图像不相同，对像素进行异或操作
            ColorModel cm = newImageData.getColorModel();
            BufferedImage image = new BufferedImage(cm,
                    cm.createCompatibleWritableRaster(newImageData.getWidth(), newImageData.getHeight()),
                    cm.isAlphaPremultiplied(),
                    null);
            int height = newImageData.getHeight();
            int width = newImageData.getWidth();
            for (int x=0;x<width;x++){
                for (int y = 0;y<height;y++){
                    int newRGB = newImageData.getRGB(x, y);
                    int oldRGB = beforeBufferedImage.getRGB(x, y);
                    image.setRGB(x,y,newRGB ^ oldRGB);
                }
            }
            return image;
        }
        return null;
    }
    /**   
     * 异或还原图片数据
     * @Author lrh 2020/10/9 15:00
     */
    public static BufferedImage restoreXorImageData(BufferedImage oldBufferedImage,BufferedImage xorBufferedImage){
        ColorModel cm = oldBufferedImage.getColorModel();
        BufferedImage image = new BufferedImage(cm,
                cm.createCompatibleWritableRaster(oldBufferedImage.getWidth(), oldBufferedImage.getHeight()),
                cm.isAlphaPremultiplied(),
                null);
        int height = oldBufferedImage.getHeight();
        int width = oldBufferedImage.getWidth();
        for (int x=0;x<width;x++){
            for (int y = 0;y<height;y++){
                int xorRGB = xorBufferedImage.getRGB(x, y);
                int oldRGB = oldBufferedImage.getRGB(x, y);
                image.setRGB(x,y,oldRGB ^ xorRGB);
            }
        }
        return image;
    }

    public static void main(String[] args) {
        int a = 20,b=20;
        int c = a ^ b;
        System.out.println(c);
        System.out.println(b^c);
    }
}
