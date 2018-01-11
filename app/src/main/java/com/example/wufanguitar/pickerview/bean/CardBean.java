package com.example.wufanguitar.pickerview.bean;

import com.wufanguitar.pickerview.callback.IPickerViewData;

/**
 * Created by KyuYi on 2017/3/2.
 * E-Mail:kyu_yi@sina.com
 * 功能：
 */

public class CardBean implements IPickerViewData {
    int id;
    String cardNo;

    public CardBean(int id, String cardNo) {
        this.id = id;
        this.cardNo = cardNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    @Override
    public Object getPickerViewData() {
        return cardNo;
    }
}

