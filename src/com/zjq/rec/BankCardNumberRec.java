package com.zjq.rec;

import com.zjq.utils.BankCardUtils;
import com.zjq.utils.OperateImage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by 张俊强~ on 2017/6/25.
 *
 * @version 1.0
 * @function 银行卡卡号识别
 * @opreation 传入银行卡图片的BufferedImage, 返回识别结果字符串
 */
public class BankCardNumberRec {
    /**
     * **************************
     * 图像处理分为以下几步
     * (1)灰度化
     * (2)遍历,获取和修改像素值
     * (3)二值化
     * (4)图像腐蚀
     * (5)过滤和切割
     * (6)识别内容
     * 相应的会产生五个中间图片
     * **************************
     */
    //存放五个临时图片的Mat
    private Mat grayLizeMat;        //灰度化
    private Mat modifyLizeMat;      //遍历优化
    private Mat binaryLizeMat;      //二值化
    private Mat erodeLizeMat;       //腐蚀
    private Mat resultMat;          //过滤和切割

    //定义源文件的路径
    private String srcImagePath;
    //过滤为白色的限制值
    private Integer toWhiteLimit=100;

    public BankCardNumberRec(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void recCardNumber(String srcImagePath){
        this.srcImagePath=srcImagePath;

        cardImageGrayLize();
        cardImageModifyLize();
        cardImageBinaryLize();
        cardImageErodeLize();
        cardImageFilterAndCut();
    }

    /**
     * @author zjq
     * @function 图片灰度化
     * @time 2017/06/25
     */
    public void cardImageGrayLize() {
        Mat destMat = new Mat();
        Mat srcMat = Highgui.imread(this.srcImagePath);   //获得源图片文件的Mat
        Imgproc.cvtColor(srcMat, destMat, Imgproc.COLOR_RGB2GRAY);
        grayLizeMat = destMat;              // 设置系统的灰度Mat
    }

    /**
     * @author zjq
     * @function 图片遍历优化像素点
     * @time 2017/06/25
     * @describe 此处如何优化需要设计优化算法, 不能通过简单的比较
     */
    public void cardImageModifyLize() {
        Mat modifyMat = grayLizeMat;
        for (int y = 0; y < modifyMat.height(); y++) {
            for (int x = 0; x < modifyMat.width(); x++) {
                // 得到该行像素点的值
                double[] data = modifyMat.get(y, x);
                for (int i1 = 0; i1 < data.length; i1++) {
                    if (data[i1] > toWhiteLimit) {                    //如果像素点不是特别接近黑色，优化成白色
                        data[i1] = 255;
                    }
                }
                modifyMat.put(y, x, data);
            }
        }
        modifyLizeMat = modifyMat;
    }

    /**
     * @author zjq
     * @function 图片二值化
     * @time 2017/06/25
     */
    public void cardImageBinaryLize() {
        int nY20Thresh = toWhiteLimit;
        int nY20MaxThesh = 255;
        Mat binaryMat = new Mat(modifyLizeMat.height(), modifyLizeMat.width(), CvType.CV_8UC1);
        Imgproc.threshold(modifyLizeMat, binaryMat, nY20Thresh, nY20MaxThesh, Imgproc.THRESH_BINARY);
        binaryLizeMat = binaryMat;
    }

    /**
     * @author zjq
     * @function 图片腐蚀
     * @time 2017/06/25
     */
    public void cardImageErodeLize() {
        Mat destMat = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1.5, 1.5));
        Imgproc.erode(binaryLizeMat, destMat, element);
        erodeLizeMat = destMat;
    }

    /**
     * @author zjq
     * @function 图片过滤和裁剪
     * @time 2017/06/25
     */
    public void cardImageFilterAndCut() {

        Mat srcMat = erodeLizeMat;

        int topLimit = 0;
        int bottomLimit = 0;
        int state = 0;
        for (int y = 0; y < srcMat.height(); y++) {

            int count = 0;
            for (int x = 0; x < srcMat.width(); x++) {
                // 得到该行像素点的值
                double[] data;
                data = srcMat.get(y, x);
                if (data[0] == 0) {                     // 找到黑色
                    count = count + 1;
                }
            }


            if (state == 0)                             // 还未到有效行
            {
                if (count >= 100)                       // 找到了有效行
                {                                       // 有效行允许十个像素点的噪声
                    topLimit = y - 1;
                    state = 1;
                }
            } else if (state == 1) {
                if (count <= 100)                        // 找到了有效行
                {                                        // 有效行允许十个像素点的噪声
                    bottomLimit = y + 1;
                    if (bottomLimit - topLimit > 10) {
                        state = 2;
                    }
                }
            }
        }
        System.out.println("过滤上界" + Integer.toString(topLimit));
        System.out.println("过滤下界" + Integer.toString(bottomLimit));
        BufferedImage bfImage=null;
        try {
             bfImage = (new OperateImage(0, topLimit, srcMat.width(), bottomLimit - topLimit))
                    .cutImage(matToBufferedImage(srcMat));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 保存新图片
        try {
            ImageIO.write(bfImage, "png", new File(BankCardUtils.OutputCardPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // mat转BufferedImage
    public BufferedImage matToBufferedImage(Mat grayMat) {

        byte[] data1 = new byte[grayMat.rows() * grayMat.cols() * (int) (grayMat.elemSize())];
        grayMat.get(0, 0, data1);
        BufferedImage image1 = new BufferedImage(grayMat.cols(), grayMat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        image1.getRaster().setDataElements(0, 0, grayMat.cols(), grayMat.rows(), data1);
        return image1;
    }

}
