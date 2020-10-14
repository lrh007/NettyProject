package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.client.bean.ImageData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
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
                dataMap.put(index,new ImageData(null,false,x,y,height,width,sourceBufferedImage.getSubimage(x, y, width, height),index,screenWidth,screenHeight));
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
        int intervalX = 10; //隔行扫描
        int intervalY = 10;
        for (int x=0;x<width;x += intervalX){
            for (int y = 0;y<height;y += intervalY){
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
     * 比较前后两张图片之前是否相同，如果不相同则将之前的图片更新，否则不动
     * @Author lrh 2020/10/9 11:23
     */
    public static boolean compareImageData2(int imageNumber,BufferedImage newBufferedImage,ImageData newImageData,Map<Integer, ImageData> beforeData){
        int height = newBufferedImage.getHeight();
        int width = newBufferedImage.getWidth();
        ImageData imageData = beforeData.get(imageNumber);
        BufferedImage beforeBufferedImage = imageData.getBufferedImage();
        int intervalX = 10; //隔行扫描
        int intervalY = 10;
        //在隔行扫描的基础上判断大概图片发生变化的位置，这样发送的数据大小会减小很多，速度会快很多
        List<Integer> list = new ArrayList<>(); //保存所有变化的行号,最后只挑选最小和最大的行号,两者相减得出变化图片的高度
        for (int x=0;x<width;x += intervalX){
            for (int y = 0;y<height;y += intervalY){
                int newRGB = newBufferedImage.getRGB(x, y);
                int oldRGB = beforeBufferedImage.getRGB(x, y);
                if(newRGB != oldRGB){
                    list.add(x); //记录开始变化的行数,并继续向下对比其它行
                    break;
                }
            }
        }
        //图片一样直接返回
        if(list.size() == 0){
            return true;
        }
        System.out.println("图片前后不相同，进行替换，图片编号= "+imageNumber);
        //对比结果进行从小到大排序
        Collections.sort(list);
        int minY = list.get(0); //最小X值
        int maxY = list.get(list.size()-1); //最大X值
        //计算高度的时候,最小值-10,最大值+10
        minY = minY - intervalX < 0 ? 0 : minY - intervalX;
        maxY = maxY + intervalX > imageData.getY()+height ? imageData.getY()+height : maxY + intervalX;
        int newHeight = maxY - minY;
        //设置裁剪后的大小
        newImageData.setMiniX(0);
        newImageData.setMiniY(minY);
        newImageData.setMiniWidth(width);
        newImageData.setMiniHeight(newHeight);
        newImageData.setBufferedImage(newBufferedImage.getSubimage(0,minY,width,newHeight));
//        替换原来的图片
        imageData.setBufferedImage(newBufferedImage);
        return false;
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

    /**   
     * 通过map的key进行排序
     * @Author lrh 2020/10/12 16:59
     */
    public static Map<Integer, byte[]> sortMapByKey(Map<Integer, byte[]> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<Integer, byte[]> sortMap = new TreeMap<Integer, byte[]>(
                new Comparator<Integer>() {

                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1.compareTo(o2);
                    }});
        sortMap.putAll(map);

        return sortMap;
    }
    /**   
     * 将map中的value值转换成字节数据
     * @Author lrh 2020/10/12 17:08
     * @return
     */
    public static byte[] convertMapToBytes(Map<Integer, byte[]> map){
        Set<Map.Entry<Integer, byte[]>> entries = map.entrySet();
        Collection<byte[]> values = map.values();
        int length = 0; //数组总长度
        for (Map.Entry<Integer, byte[]> entry : entries){
            byte[] data = entry.getValue();
            length += data.length;
        }
        //通过总长度然后将数组合并到一起
        byte[] allBytes = new byte[length];
        int destPos = 0; //索引位置
        for (Map.Entry<Integer, byte[]> entry : entries){
            byte[] src = entry.getValue();
            System.arraycopy(src,0,allBytes,destPos,src.length);
            destPos += src.length;
        }
        
//        int i = new Random().nextInt(Integer.MAX_VALUE);
//        try {
//            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(allBytes));
//            ImageIO.write(bufferedImage,"jpg",new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\"+i+".jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("数据总子节数="+allBytes.length);


        return allBytes;
    }
    /**   
     * 合并图片
     * @Author lrh 2020/10/13 15:50
     */
    public static void mergeImage(BufferedImage globelBufferedImage,ImageData imageData){
        Graphics g = globelBufferedImage.getGraphics(); //获取画笔

        ////获取局部图片的缓冲区
        BufferedImage subImage = null;
        try {
            subImage = ImageIO.read(new ByteArrayInputStream(decodeUnCompress(imageData.getData())));
            //使用画笔将局部图片缓冲区画到全局缓冲区上，替换原来的图片
            g.clearRect(imageData.getX(),imageData.getY(),imageData.getWidth(),imageData.getHeight());
            g.drawImage(subImage,imageData.getX(),imageData.getY(),imageData.getWidth(),imageData.getHeight(),null);
            g.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 待合并的两张图必须满足这样的前提，如果水平方向合并，则高度必须相等；如果是垂直方向合并，宽度必须相等。
     * mergeImage方法不做判断，自己判断。
     *
     * @param img1
     *            待合并的第一张图
     * @param img2
     *            带合并的第二张图
     * @param isHorizontal
     *            为true时表示水平方向合并，为false时表示垂直方向合并
     * @return 返回合并后的BufferedImage对象
     * @throws IOException
     */
    public static BufferedImage mergeImage(BufferedImage img1, BufferedImage img2, boolean isHorizontal,int startX, int startY)throws IOException {
        int w1 = img1.getWidth();
        int h1 = img1.getHeight();
        int w2 = img2.getWidth();
        int h2 = img2.getHeight();

        // 从图片中读取RGB
        int[] ImageArrayOne = new int[w1 * h1];
        ImageArrayOne = img1.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
        int[] ImageArrayTwo = new int[w2 * h2];
        ImageArrayTwo = img2.getRGB(0, 0, w2, h2, ImageArrayTwo, 0, w2);

        // 生成新图片
        BufferedImage DestImage = null;
        if (isHorizontal) { // 水平方向合并
            DestImage = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
            DestImage.setRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            DestImage.setRGB(startX,startY, w2, h2, ImageArrayTwo, 0, w2); // 设置下半部分的RGB
        } else { // 垂直方向合并
            DestImage = new BufferedImage(w1, h1 + h2,BufferedImage.TYPE_INT_RGB);
            DestImage.setRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            DestImage.setRGB(0, h1, w2, h2, ImageArrayTwo, 0, w2); // 设置下半部分的RGB
        }
        return DestImage;
    }

    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        for(int i=0;i<10;i++){
            list.add(new Random().nextInt(10));
        }
        System.out.println("排序前: "+list);
        Collections.sort(list);
        System.out.println("排序后: "+list);
        System.out.println("最小值: "+list.get(0)+",最大值: "+list.get(list.size()-1));

    }
}
