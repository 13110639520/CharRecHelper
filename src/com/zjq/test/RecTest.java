package com.zjq.test;

import com.zjq.rec.BankCardNumberRec;
import com.zjq.utils.BankCardUtils;
import org.junit.Test;

/**
 * Created by 张俊强~ on 2017/6/25.
 */
public class RecTest {

    @Test
    public void cardRecTest(){
        System.out.println("进入Test");
        BankCardNumberRec rec=new BankCardNumberRec();
        rec.recCardNumber(BankCardUtils.InputCardPath1);
    }
}
