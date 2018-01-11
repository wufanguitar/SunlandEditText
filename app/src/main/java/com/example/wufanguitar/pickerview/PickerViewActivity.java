package com.example.wufanguitar.pickerview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wufanguitar.R;
import com.example.wufanguitar.pickerview.bean.CardBean;
import com.example.wufanguitar.pickerview.bean.ProvinceBean;
import com.wufanguitar.pickerview.OptionsPickerView;
import com.wufanguitar.pickerview.TimePickerView;
import com.wufanguitar.pickerview.callback.ICustomLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: Frank Wu
 * @Time: 2018/01/01 on 22:56
 * @Email: wu.fanguitar@163.com
 * @Description:
 */

public class PickerViewActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<ProvinceBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();

    /* private ArrayList<ArrayList<ArrayList<IPickerViewData>>> options3Items = new ArrayList<>(); */
    private Button btn_Time, btn_Options, btn_CustomOptions, btn_CustomTime, btn_no_linkage, btn_to_Fragment;

    private TimePickerView pvTime, pvCustomTime, pvCustomLunar;
    private OptionsPickerView pvOptions, pvCustomOptions, pvNoLinkOptions;
    private ArrayList<CardBean> cardItem = new ArrayList<>();

    private ArrayList<String> food = new ArrayList<>();
    private ArrayList<String> clothes = new ArrayList<>();
    private ArrayList<String> computer = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_view);

        // 等数据加载完毕再初始化并显示Picker，以免还未加载完数据就显示，造成APP崩溃。
        getOptionData();

        initLunarPicker();
        initTimePicker();
        initOptionPicker();
        initCustomTimePicker();
        initCustomOptionPicker();
        initNoLinkOptionsPicker();

        btn_Time = (Button) findViewById(R.id.btn_Time);
        btn_Options = (Button) findViewById(R.id.btn_Options);
        btn_CustomOptions = (Button) findViewById(R.id.btn_CustomOptions);
        btn_CustomTime = (Button) findViewById(R.id.btn_CustomTime);
        btn_no_linkage = (Button) findViewById(R.id.btn_no_linkage);
        btn_to_Fragment = (Button) findViewById(R.id.btn_fragment);

        btn_Time.setOnClickListener(this);
        btn_Options.setOnClickListener(this);
        btn_CustomOptions.setOnClickListener(this);
        btn_CustomTime.setOnClickListener(this);
        btn_no_linkage.setOnClickListener(this);
        btn_to_Fragment.setOnClickListener(this);

        findViewById(R.id.btn_GotoJsonData).setOnClickListener(this);
        findViewById(R.id.btn_lunar).setOnClickListener(this);
    }

    private void initLunarPicker() {
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, 1, 23);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2027, 2, 28);
        //时间选择器 ，自定义布局
        pvCustomLunar = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) { // 选中事件回调
                Toast.makeText(PickerViewActivity.this, getTime(date), Toast.LENGTH_SHORT).show();
            }
        }).setSelectDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_lunar, new ICustomLayout() {

                    @Override
                    public void customLayout(final View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomLunar.returnData();
                                pvCustomLunar.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomLunar.dismiss();
                            }
                        });
                        //公农历切换
                        CheckBox cb_lunar = (CheckBox) v.findViewById(R.id.cb_lunar);
                        cb_lunar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                pvCustomLunar.setLunarCalendar(!pvCustomLunar.isLunarCalendar());
                                //自适应宽
                                setTimePickerChildWeight(v, 0.8f, isChecked ? 1f : 1.1f);
                            }
                        });
                    }

                    /**
                     * 公农历切换后调整宽
                     */
                    private void setTimePickerChildWeight(View v, float yearWeight, float weight) {
                        ViewGroup timepicker = (ViewGroup) v.findViewById(R.id.time_picker);
                        View year = timepicker.getChildAt(0);
                        LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) year.getLayoutParams());
                        lp.weight = yearWeight;
                        year.setLayoutParams(lp);
                        for (int i = 1; i < timepicker.getChildCount(); i++) {
                            View childAt = timepicker.getChildAt(i);
                            LinearLayout.LayoutParams childLp = ((LinearLayout.LayoutParams) childAt.getLayoutParams());
                            childLp.weight = weight;
                            childAt.setLayoutParams(childLp);
                        }
                    }
                })
                .setTimeType(new boolean[]{true, true, true, false, false, false})
                .setCenterLabel(false) // 是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(Color.RED)
                .build();
    }

    private void initTimePicker() {
        //控制时间范围(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
        //因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2013, 0, 23);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2019, 11, 28);
        //时间选择器
        pvTime = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                // 这里回调过来的v,就是show()方法里面所添加的 View 参数，如果show的时候没有添加参数，v则为null
                /*btn_Time.setText(getTime(date));*/
                Button btn = (Button) v;
                btn.setText(getTime(date));
            }
        })
                //年月日时分秒 的显示与否，不设置则默认全部显示
                .setTimeType(new boolean[]{true, true, true, false, false, false})
                .setLabel("", "", "", "", "", "")
                .setCenterLabel(false)
                .setDividerColor(Color.DKGRAY)
                .setContentTextSize(21)
                .setSelectDate(selectedDate)
                .setRangDate(startDate, endDate)
//                .setBackgroundId(0x00FFFFFF) //设置外部遮罩颜色
                .setDecorView(null)
                .build();
    }

    private void initOptionPicker() { // 条件选择器初始化
        /**
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         */
        pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String tx = options1Items.get(options1).getPickerViewData()
                        + options2Items.get(options1).get(options2);
                btn_Options.setText(tx);
            }
        })
                .setTitleStr("城市选择")
                .setContentTextSize(20) // 设置滚轮文字大小
                .setDividerColor(Color.LTGRAY) // 设置分割线的颜色
                .setOptionsSelectedPosition(0, 1) // 默认选中项
                .setWheelViewBgColor(Color.BLACK)
                .setTopBarBgColor(Color.DKGRAY)
                .setTitleStrColor(Color.LTGRAY)
                .setLeftBtnStrColor(Color.YELLOW)
                .setRightBtnStrColor(Color.YELLOW)
                .setCenterTextColor(Color.LTGRAY)
                .setCenterLabel(false) // 是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setLabels("省", "市", "区")
                .setBackgroundColor(0x66000000) // 设置外部遮罩颜色
                .build();
        pvOptions.setRelatedPicker(options1Items, options2Items); // 二级选择器
    }

    private void initCustomTimePicker() {

        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, 1, 23);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2027, 2, 28);
        //时间选择器 ，自定义布局
        pvCustomTime = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                btn_CustomTime.setText(getTime(date));
            }
        }).setSelectDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, new ICustomLayout() {

                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.returnData();
                                pvCustomTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.dismiss();
                            }
                        });
                    }
                })
                .setContentTextSize(18)
                .setTimeType(new boolean[]{true, true, true, true, true, true})
                .setLabel("年", "月", "日", "时", "分", "秒")
                .setLineSpacingMultiplier(1.2f)
                .setTimeXOffset(0, 0, 0, 40, 0, -40)
                .setCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(0xFF24AD9D)
                .build();
    }

    private void initCustomOptionPicker() {//条件选择器初始化，自定义布局
        /**
         * @description
         *
         * 注意事项：
         * 自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针。
         * 具体可参考demo 里面的两个自定义layout布局。
         */
        pvCustomOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String tx = (String) cardItem.get(options1).getPickerViewData();
                btn_CustomOptions.setText(tx);
            }
        })
                .setLayoutRes(R.layout.pickerview_custom_options, new ICustomLayout() {
                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        final TextView tvAdd = (TextView) v.findViewById(R.id.tv_add);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions.returnData();
                                pvCustomOptions.dismiss();
                            }
                        });

                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions.dismiss();
                            }
                        });

                        tvAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getCardData();
                                pvCustomOptions.setRelatedPicker(cardItem);
                            }
                        });
                    }
                })
                .setDialog(true)
                .build();

        pvCustomOptions.setRelatedPicker(cardItem);//添加数据
    }

    private void initNoLinkOptionsPicker() {// 不联动的多级选项
        pvNoLinkOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {

                String str = "food:" + food.get(options1)
                        + "\nclothes:" + clothes.get(options2)
                        + "\ncomputer:" + computer.get(options3);

                Toast.makeText(PickerViewActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        }).build();
        pvNoLinkOptions.setNoRelatedPicker(food, clothes, computer);
    }

    private void getOptionData() {

        /**
         * 注意：如果是添加JavaBean实体数据，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */

        getCardData();
        getNoLinkData();

        // 选项1
        options1Items.add(new ProvinceBean(0, "广东", "描述部分", "其他数据"));
        options1Items.add(new ProvinceBean(1, "湖南", "描述部分", "其他数据"));
        options1Items.add(new ProvinceBean(2, "广西", "描述部分", "其他数据"));

        // 选项2
        ArrayList<String> options2Items_01 = new ArrayList<>();
        options2Items_01.add("广州");
        options2Items_01.add("佛山");
        options2Items_01.add("东莞");
        options2Items_01.add("珠海");
        ArrayList<String> options2Items_02 = new ArrayList<>();
        options2Items_02.add("长沙");
        options2Items_02.add("岳阳");
        options2Items_02.add("株洲");
        options2Items_02.add("衡阳");
        ArrayList<String> options2Items_03 = new ArrayList<>();
        options2Items_03.add("桂林");
        options2Items_03.add("玉林");
        options2Items.add(options2Items_01);
        options2Items.add(options2Items_02);
        options2Items.add(options2Items_03);

        /*-------- 数据源添加完毕 ---------*/
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_Time && pvTime != null) {
            // pvTime.setDate(Calendar.getInstance());
           /* pvTime.show(); //show timePicker*/
            pvTime.show(v); // 弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
        } else if (v.getId() == R.id.btn_Options && pvOptions != null) {
            pvOptions.show(); // 弹出条件选择器
        } else if (v.getId() == R.id.btn_CustomOptions && pvCustomOptions != null) {
            pvCustomOptions.show(); // 弹出自定义条件选择器
        } else if (v.getId() == R.id.btn_CustomTime && pvCustomTime != null) {
            pvCustomTime.show(); // 弹出自定义时间选择器
        } else if (v.getId() == R.id.btn_no_linkage && pvNoLinkOptions != null) { // 不联动数据选择器
            pvNoLinkOptions.show();
        } else if (v.getId() == R.id.btn_GotoJsonData) { // 跳转到省市区解析示例页面
            startActivity(new Intent(PickerViewActivity.this, JsonDataActivity.class));
        } else if (v.getId() == R.id.btn_fragment) { // 跳转到 fragment
            startActivity(new Intent(PickerViewActivity.this, FragmentTestActivity.class));
        } else if (v.getId() == R.id.btn_lunar) {
            pvCustomLunar.show();
        }
    }

    private void getCardData() {
        for (int i = 0; i < 5; i++) {
            cardItem.add(new CardBean(i, "No.ABC12345 " + i));
        }

        for (int i = 0; i < cardItem.size(); i++) {
            if (cardItem.get(i).getCardNo().length() > 6) {
                String str_item = cardItem.get(i).getCardNo().substring(0, 6) + "...";
                cardItem.get(i).setCardNo(str_item);
            }
        }
    }

    private void getNoLinkData() {
        food.add("KFC");
        food.add("MacDonald");
        food.add("Pizza hut");

        clothes.add("Nike");
        clothes.add("Adidas");
        clothes.add("Anima");

        computer.add("ASUS");
        computer.add("Lenovo");
        computer.add("Apple");
        computer.add("HP");
    }

    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
}
