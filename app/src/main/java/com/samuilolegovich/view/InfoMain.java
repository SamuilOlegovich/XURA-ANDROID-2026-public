package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class InfoMain extends BaseActivity {
    public static final String INFO_MAIN_CLASS = ".InfoMain";

    private TextView infoMainTextView;

    private TextView itIsImportantToKnowView;
    private TextView itIsImportantToKnowView1;
    private TextView itIsImportantToKnowView2;
    private TextView itIsImportantToKnowView3;
    private TextView itIsImportantToKnowView4;

    private TextView aboutTheApplicationView;
    private TextView aboutTheApplicationView1;
    private TextView aboutTheApplicationView2;
    private TextView aboutTheApplicationView3;
    private TextView aboutTheApplicationView4;
    private TextView aboutTheApplicationView5;
    private TextView aboutTheApplicationView6;
    private TextView aboutTheApplicationView7;
    private TextView aboutTheApplicationView8;

    private TextView advantagesView;
    private TextView advantagesView1;
    private TextView advantagesView2;
    private TextView advantagesView3;
    private TextView advantagesView4;
    private TextView advantagesView5;
    private TextView advantagesView6;
    private TextView advantagesView7;

    private TextView targetView;
    private TextView targetView1;
    private TextView targetView2;
    private TextView targetView3;

    private TextView adviceView;
    private TextView adviceView1;

    private TextView thankYou;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_main_page);
        setButtons();
        setLanguage();
    }



    private void setButtons() {
        infoMainTextView = (TextView) findViewById(R.id.info_main_text_view);

        itIsImportantToKnowView = (TextView) findViewById(R.id.it_is_important_to_know_view);
        itIsImportantToKnowView1 = (TextView) findViewById(R.id.it_is_important_to_know_view_1);
        itIsImportantToKnowView2 = (TextView) findViewById(R.id.it_is_important_to_know_view_2);
        itIsImportantToKnowView3 = (TextView) findViewById(R.id.it_is_important_to_know_view_3);
        itIsImportantToKnowView4 = (TextView) findViewById(R.id.it_is_important_to_know_view_4);

        aboutTheApplicationView = (TextView) findViewById(R.id.about_the_application_view);
        aboutTheApplicationView1 = (TextView) findViewById(R.id.about_the_application_view_1);
        aboutTheApplicationView2 = (TextView) findViewById(R.id.about_the_application_view_2);
        aboutTheApplicationView3 = (TextView) findViewById(R.id.about_the_application_view_3);
        aboutTheApplicationView4 = (TextView) findViewById(R.id.about_the_application_view_4);
        aboutTheApplicationView5 = (TextView) findViewById(R.id.about_the_application_view_5);
        aboutTheApplicationView6 = (TextView) findViewById(R.id.about_the_application_view_6);
        aboutTheApplicationView7 = (TextView) findViewById(R.id.about_the_application_view_7);
        aboutTheApplicationView8 = (TextView) findViewById(R.id.about_the_application_view_8);

        advantagesView = (TextView) findViewById(R.id.advantages_view);
        advantagesView1 = (TextView) findViewById(R.id.advantages_view_1);
        advantagesView2 = (TextView) findViewById(R.id.advantages_view_2);
        advantagesView3 = (TextView) findViewById(R.id.advantages_view_3);
        advantagesView4 = (TextView) findViewById(R.id.advantages_view_4);
        advantagesView5 = (TextView) findViewById(R.id.advantages_view_5);
        advantagesView6 = (TextView) findViewById(R.id.advantages_view_6);
        advantagesView7 = (TextView) findViewById(R.id.advantages_view_7);

        targetView = (TextView) findViewById(R.id.target_view);
        targetView1 = (TextView) findViewById(R.id.target_view_1);
        targetView2 = (TextView) findViewById(R.id.target_view_2);
        targetView3 = (TextView) findViewById(R.id.target_view_3);

        adviceView = (TextView) findViewById(R.id.advice_view);
        adviceView1 = (TextView) findViewById(R.id.advice_view_1);

        thankYou = (TextView) findViewById(R.id.good_luck_and_thank_you_for_choosing_our_app_view);
    }


    private void setLanguage() {
        infoMainTextView.setText(R.string.info_main_text);

        itIsImportantToKnowView.setText(R.string.it_is_important_to_know);
        itIsImportantToKnowView1.setText(R.string.it_is_important_to_know_1);
        itIsImportantToKnowView2.setText(R.string.it_is_important_to_know_2);
        itIsImportantToKnowView3.setText(R.string.it_is_important_to_know_3);
        itIsImportantToKnowView4.setText(R.string.it_is_important_to_know_4);

        aboutTheApplicationView.setText(R.string.about_the_application);
        aboutTheApplicationView1.setText(R.string.about_the_application_1);
        aboutTheApplicationView2.setText(R.string.about_the_application_2);
        aboutTheApplicationView3.setText(R.string.about_the_application_3);
        aboutTheApplicationView4.setText(R.string.about_the_application_4);
        aboutTheApplicationView5.setText(R.string.about_the_application_5);
        aboutTheApplicationView6.setText(R.string.about_the_application_6);
        aboutTheApplicationView7.setText(R.string.about_the_application_7);
        aboutTheApplicationView8.setText(R.string.about_the_application_8);

        advantagesView.setText(R.string.advantages);
        advantagesView1.setText(R.string.advantages_1);
        advantagesView2.setText(R.string.advantages_2);
        advantagesView3.setText(R.string.advantages_3);
        advantagesView4.setText(R.string.advantages_4);
        advantagesView5.setText(R.string.advantages_5);
        advantagesView6.setText(R.string.advantages_6);
        advantagesView7.setText(R.string.advantages_7);

        targetView.setText(R.string.target);
        targetView1.setText(R.string.target_1);
        targetView2.setText(R.string.target_2);
        targetView3.setText(R.string.target_3);

        adviceView.setText(R.string.advice);
        adviceView1.setText(R.string.advice_1);

        thankYou.setText(R.string.good_luck_and_thank_you_for_choosing_our_app);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
