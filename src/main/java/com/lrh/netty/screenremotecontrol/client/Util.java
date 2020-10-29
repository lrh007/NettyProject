package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.client.bean.ImageData;
import com.luciad.imageio.webp.WebPReadParam;
import com.luciad.imageio.webp.WebPWriteParam;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.zip.*;

import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;

/**
 * 工具类
 *
 * @Author lrh 2020/9/23 17:27
 */
public class Util {

    /**
     * 分割图片并给图片进行编号
     * @Author lrh 2020/9/28 12:15
     */
    public static Map<Integer,ImageData> splitImageAndNum(int screenWidth, int screenHeight, int marginLeft, BufferedImage sourceBufferedImage){
        Map<Integer,ImageData> dataMap = new HashMap<>();
        int width_interval = 10; //宽度分成10份
        int height_interval = 4; //高度分成2份
        int width = screenWidth / width_interval;  //图片宽度
        int height = screenHeight / height_interval; //图片高度
        int x = marginLeft; //图片x坐标
        int y = 0; //图片y坐标
        //分割图片
        BufferedImage newBufferedImage = null;
        int index = 0; //图片编号
        for (int i = 0; i < height_interval; i++) {
            for (int j = 0; j < width_interval; j++) {
                dataMap.put(index,new ImageData(null,x,y,height,width,sourceBufferedImage.getSubimage(x, y, width, height),index,screenWidth,screenHeight));
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
     * 分割图片并给图片进行编号
     * @Author lrh 2020/9/28 12:15
     */
    public static Map<Integer,ImageData> splitImageAndNum(int screenWidth, int screenHeight, int marginLeft){
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
                dataMap.put(index,new ImageData(null,x,y,height,width,null,index,screenWidth,screenHeight));
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
    public synchronized static boolean compareImageData(int imageNumber,BufferedImage newImageData,BufferedImage beforeBufferedImage){
        int height = newImageData.getHeight();
        int width = newImageData.getWidth();
//        ImageData imageData = beforeData.get(imageNumber);
//        BufferedImage beforeBufferedImage = imageData.getBufferedImage();
        int intervalX = 20; //隔行扫描
        int intervalY = 20;
        for (int x=0;x<width;x += intervalX){
            for (int y = 0;y<height;y += intervalY){
                int newRGB = newImageData.getRGB(x, y);
                int oldRGB = beforeBufferedImage.getRGB(x, y);
                if(newRGB != oldRGB){
                    System.out.println("图片前后不相同，进行替换，图片编号= "+imageNumber);
//                    imageData.setBufferedImage(newImageData);
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * 比较前后两张图片之前是否相同，如果不相同则将之前的图片更新，否则不动,获取裁剪后的大小
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
    public static BufferedImage getXorImageData(int imageNumber,BufferedImage newImageData,BufferedImage beforeBufferedImage){
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

    public static void main(String[] args) throws Exception {


        /*File in_file = new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\1.jpg");
        BufferedImage bufferedImage = ImageIO.read(in_file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //压缩
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufferedImage);
        param.setQuality(0.35f, false);
        encoder.setJPEGEncodeParam(param);
        encoder.encode(bufferedImage);
        System.out.println("压缩之后大小="+out.toByteArray().length/1024);

        //解压缩
        byte[] buf = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
        BufferedImage image = decoder.decodeAsBufferedImage();
        ImageIO.write(image,"jpg",new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\2.jpg"));
*/
        //先把原图片缩小成原来的90%大小，质量为60%
//        Robot robot = new Robot();
//        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, 192, 540));
//        ImageIO.write(screenCapture,"jpg",new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\1.jpg"));
////        BufferedImage bufferedImage1 = Thumbnails.of(screenCapture).scale(0.9f).outputQuality(1f).outputFormat("gif").asBufferedImage();
//        byte[] bytes = bufferedImageTobytes(screenCapture, 0.3f);
//        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
//        ImageIO.write(image,"jpg",new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\2.jpg"));


            // 字符串超过一定的长度
        String str = "OimKBp+DNM9N9Ce1A6yFM2VcySnScSBlCWy0FwXapH3jmL2FpjzXYvuFIvJOsdjKW/9RSAeP/q5u\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "6qyArAfXbnccj+cvYL8vMW8FqFZIMwRpVht3hmJuMdDjNubFhL9VBfapTpkIGpTbiXDvNQ2MBjPI\n" +
                "aLUIGOM1TCW6ZogcEZEc52DuocUEID9+LJHFdT8wN8A2IKEuZZv1vFiNgmvG5vbo4xoulzh1c5kJ\n" +
                "ZxVOg+q5yPb6eCth6viCoeuGWp2gmktKww/zVmi1ts5EvmD5TGo3qOpmPudzFC7sR9fTZUa3HEEy\n" +
                "bSVFoh3sWxZS+KwV";
        System.out.println("原始字符串大小="+str.length());
//        byte[] bytes = zipCompress(str);
//        System.out.printf("压缩后字符串大小="+bytes.length);

//        String s = zipString(str);
//        System.out.println(s.length());
//        System.out.println("\n原始的字符串为------->" + str);
//        float len0=str.length();
//        System.out.println("原始的字符串长度为------->"+len0);
//        List<Integer> list = LZWcompress(str);
//        float len1=list.size();
//        System.out.println("压缩后的字符串长度为----->" + len1);
//        String jy = LZWdecompress(list);
//        System.out.println("\n解压缩后的字符串为--->" + jy);
//        System.out.println("解压缩后的字符串长度为--->"+jy.length());
//        //判断
//        if(str.equals(jy)){
//            System.out.println("先压缩再解压以后字符串和原来的是一模一样的");
//        }
//        String s = Arrays.asList(list).toString();
//        System.out.println(s.replaceAll("\\[\\[|\\]\\]",""));

//        System.load("C:\\Users\\MACHENIKE\\AppData\\Local\\Temp\\libwebp-imageio.so");
//        System.out.println(System.getProperty("user.dir"));
//        File outfile = File.createTempFile("imageio", ".dll",new File(System.getProperty("user.dir")));
//        System.out.println(outfile.getAbsolutePath());

//        this.getClass().getClassLoader().getResource("META-INF/lib/windows_64/" + "webp-imageio.dll");

        //编码
//        String inputJpgPath = "C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\1.jpg";
//        String outputWebpPath = "C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\2.webp";
//        Robot robot = new Robot();
//        BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(0, 0, 192, 540));
//        ImageIO.write(screenCapture,"jpg",new File(inputJpgPath));
//        byte[] bytes = webpEncode(screenCapture,0.3f);
//        System.out.println(bytes.length);

        //解码
//        String inputWebpPath = "C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\2.webp";
//        String outputJpgPath = "C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\3.jpg";
//        FileInputStream in = new FileInputStream(new File(inputWebpPath));
//        ByteArrayOutputStream arry = new ByteArrayOutputStream();
//        BufferedImage image = ImageIO.read(in);
//        ImageIO.write(image,"webp",arry);
//
//        BufferedImage image2 = webpDecode(arry.toByteArray());
//        ImageIO.write(image2, "jpg", new File(outputJpgPath));

    }
    /**
     * 压缩
     */
    public static String zipString(byte[] data) {
        /**
         *     https://www.yiibai.com/javazip/javazip_deflater.html#article-start
         *     0 ~ 9 压缩等级 低到高
         *     public static final int BEST_COMPRESSION = 9;            最佳压缩的压缩级别。
         *     public static final int BEST_SPEED = 1;                  压缩级别最快的压缩。
         *     public static final int DEFAULT_COMPRESSION = -1;        默认压缩级别。
         *     public static final int DEFAULT_STRATEGY = 0;            默认压缩策略。
         *     public static final int DEFLATED = 8;                    压缩算法的压缩方法(目前唯一支持的压缩方法)。
         *     public static final int FILTERED = 1;                    压缩策略最适用于大部分数值较小且数据分布随机分布的数据。
         *     public static final int FULL_FLUSH = 3;                  压缩刷新模式，用于清除所有待处理的输出并重置拆卸器。
         *     public static final int HUFFMAN_ONLY = 2;                仅用于霍夫曼编码的压缩策略。
         *     public static final int NO_COMPRESSION = 0;              不压缩的压缩级别。
         *     public static final int NO_FLUSH = 0;                    用于实现最佳压缩结果的压缩刷新模式。
         *     public static final int SYNC_FLUSH = 2;                  用于清除所有未决输出的压缩刷新模式; 可能会降低某些压缩算法的压缩率。
         */

        //使用指定的压缩级别创建一个新的压缩器。
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        //设置压缩输入数据。
        deflater.setInput(data);
        //当被调用时，表示压缩应该以输入缓冲区的当前内容结束。
        deflater.finish();
        final byte[] bytes = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        while (!deflater.finished()) {
            //压缩输入数据并用压缩数据填充指定的缓冲区。
            int length = deflater.deflate(bytes);
            outputStream.write(bytes, 0, length);
        }
        //关闭压缩器并丢弃任何未处理的输入。
        deflater.end();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
    /**
     * 解压缩
     */
    public static byte[] unzipString(String zipString) {
        byte[] decode = Base64.getDecoder().decode(zipString);
        //创建一个新的解压缩器  https://www.yiibai.com/javazip/javazip_inflater.html
        Inflater inflater = new Inflater();
        //设置解压缩的输入数据。
        inflater.setInput(decode);
        final byte[] bytes = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try {
            //finished() 如果已到达压缩数据流的末尾，则返回true。
            while (!inflater.finished()) {
                //将字节解压缩到指定的缓冲区中。
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            //关闭解压缩器并丢弃任何未处理的输入。
            inflater.end();
        }
        return outputStream.toByteArray();
    }
    /**
     * 将图片转换成webp格式
     * @param qualit 0-1 之间，表示图片质量，默认0.3
     * @Author lrh 2020/10/22 15:40
     */
    public static byte[] webpEncode2(BufferedImage bufferedImage,float qualit) throws IOException {
        ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByMIMEType("image/webp").next();
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream m = new MemoryCacheImageOutputStream(out);
//        writer.setOutput(m);
        writer.setOutput(new FileImageOutputStream(new File("C:\\Users\\MACHENIKE\\Desktop\\新建文件夹\\"+new Random().nextInt(Integer.MAX_VALUE)+".webp")));
        writer.write((IIOMetadata)null, new IIOImage(bufferedImage, (List)null, (IIOMetadata)null), writeParam);
        m.flush();//需要调用这个方法，将内存中的数据刷新到输出流中，否则输出流没有数据
        return out.toByteArray();
    }
    /**   
     * 将图片转换成webp格式
     * @param qualit 0-1 之间，表示图片质量，默认0.3
     * @Author lrh 2020/10/22 15:40
     */
    public static byte[] webpEncode(BufferedImage bufferedImage,float qualit){
        ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByMIMEType("image/webp").next();
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(MODE_EXPLICIT); //压缩模式，不能变
        writeParam.setCompressionType("Lossy"); //Lossless 无损压缩，Lossy 有损压缩
        writeParam.setCompressionQuality(qualit); //压缩质量
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream m = new MemoryCacheImageOutputStream(outputStream);
        writer.setOutput(m);
        try {
           writer.write((IIOMetadata)null, new IIOImage(bufferedImage, (List)null, (IIOMetadata)null), writeParam);
           m.flush();//需要调用这个方法，将内存中的数据刷新到输出流中，否则输出流没有数据
           return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**   
     * 将webp字节数组解码
     * @Author lrh 2020/10/27 13:51
     */
    public static BufferedImage webpDecode(byte[] bytes){
        ImageReader reader = (ImageReader)ImageIO.getImageReadersByMIMEType("image/webp").next();
        WebPReadParam readParam = new WebPReadParam();
        readParam.setBypassFiltering(true);
        MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));
        reader.setInput(in);
        BufferedImage image = null;
        try {
            image = reader.read(0, readParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    
    

    /** Compress a string to a list of output symbols. */
    public static List<Integer> LZWcompress(String uncompressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<String,Integer> dictionary = new HashMap<String,Integer>();
        for (int i = 0; i < 256; i++)
            dictionary.put("" + (char)i, i);

        String w = "";
        List<Integer> result = new ArrayList<Integer>();
        for (char c : uncompressed.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;
            else {
                result.add(dictionary.get(w));
                // Add wc to the dictionary.
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }

        // Output the code for w.
        if (!w.equals(""))
            result.add(dictionary.get(w));
        return result;
    }

    /** Decompress a list of output ks to a string. */
    public static String LZWdecompress(List<Integer> compressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<Integer,String> dictionary = new HashMap<Integer,String>();
        for (int i = 0; i < 256; i++)
            dictionary.put(i, "" + (char)i);

        String w = "" + (char)(int)compressed.remove(0);
        StringBuffer result = new StringBuffer(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k))
                entry = dictionary.get(k);
            else if (k == dictSize)
                entry = w + w.charAt(0);
            else
                throw new IllegalArgumentException("Bad compressed k: " + k);

            result.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.put(dictSize++, w + entry.charAt(0));

            w = entry;
        }
        return result.toString();
    }

    /**
     * 对图像进行压缩
     * @param image 图像
     * @return 图像数据包装类
     * @throws ImageFormatException
     * @throws IOException
     * @throws IOException
     */
    public static byte[] encodeImage(BufferedImage image){

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(0.75f, false);
        encoder.setJPEGEncodeParam(param);
        try {
            encoder.encode(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();

    }

    /**对图像进行解压缩
     * @param ito 图像数据包装类
     * @return BufferedImage 图像
     * @throws ImageFormatException
     * @throws IOException
     * @throws IOException
     */

    public static BufferedImage decodeImage(byte[] buf){
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
        BufferedImage image = null;
        try {
            image = decoder.decodeAsBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}
