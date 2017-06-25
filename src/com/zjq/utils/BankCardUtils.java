package com.zjq.utils;

/**
 * Created by 张俊强~ on 2017/6/25.
 */
public class BankCardUtils {
    //传过来的原图片存放地址
    public static final String InputCardPath = "tmp/input.png";
    public static final String InputCardPath1 = "tmp/card.png";
    //灰度化后的图片存放地址
    public static final String GrayCardPath = "tmp/gray.png";
    //通过遍历修改像素值后的图片存放地
    public static final String ModifyCardPath = "tmp/modify.png";
    //二值化后图片的存放地
    public static final String BinaryCardPath = "tmp/binary.png";
    //腐蚀后的图片存放的地点
    public static final String ErodeCardPath = "tmp/erode.png";
    //经过过滤和切割后的输出待识别图片存放地点
    public static final String OutputCardPath = "tmp/output.png";
}
