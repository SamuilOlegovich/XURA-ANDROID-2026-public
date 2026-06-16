package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Статический информационный экран о реферальной программе: условия становления
 * рефералом, восстановление статуса, выгоды для рефералов и их партнёров,
 * ограничения для не-рефералов и планы на будущее. Без интерактивной логики.
 */
@AndroidEntryPoint
public class InfoReferral extends BaseActivity {
    public static final String INFO_REFERRAL_CLASS = ".InfoReferral";

    private TextView becomeReferralMainTextView;

    private TextView becomeReferralTextView;
    private TextView becomeReferralTextView1;
    private TextView becomeReferralTextView2;
    private TextView becomeReferralTextView3;

    private TextView referralRecoveryTextView;
    private TextView referralRecoveryTextView1;

    private TextView benefitsForReferralsAndTheirPartnersView;
    private TextView benefitsForReferralsAndTheirPartnersView1;
    private TextView benefitsForReferralsAndTheirPartnersView2;
    private TextView benefitsForReferralsAndTheirPartnersView3;

    private TextView restrictionsForNonReferralUsersView;
    private TextView restrictionsForNonReferralUsersView1;

    private TextView futurePlansView;
    private TextView futurePlansView1;




    /** Инициализирует экран: разметка, привязка всех TextView, локализация текстов. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_referral_page);
        setButtons();
        setLanguage();
    }



    /** Находит и сохраняет ссылки на все текстовые блоки разметки экрана. */
    private void setButtons() {
        becomeReferralMainTextView = (TextView) findViewById(R.id.become_referral_main_text_view);

        becomeReferralTextView = (TextView) findViewById(R.id.become_referral_text_view_0);
        becomeReferralTextView1 = (TextView) findViewById(R.id.become_referral_text_view_1);
        becomeReferralTextView2 = (TextView) findViewById(R.id.become_referral_text_view_2);
        becomeReferralTextView3 = (TextView) findViewById(R.id.become_referral_text_view_3);

        referralRecoveryTextView = (TextView) findViewById(R.id.referral_recovery_text_view);
        referralRecoveryTextView1 = (TextView) findViewById(R.id.referral_recovery_text_view_1);

        benefitsForReferralsAndTheirPartnersView = (TextView) findViewById(R.id.benefits_for_referrals_and_their_partners_view);
        benefitsForReferralsAndTheirPartnersView1 = (TextView) findViewById(R.id.benefits_for_referrals_and_their_partners_view_1);
        benefitsForReferralsAndTheirPartnersView2 = (TextView) findViewById(R.id.benefits_for_referrals_and_their_partners_view_2);
        benefitsForReferralsAndTheirPartnersView3 = (TextView) findViewById(R.id.benefits_for_referrals_and_their_partners_view_3);

        restrictionsForNonReferralUsersView = (TextView) findViewById(R.id.restrictions_for_non_referral_users_view);
        restrictionsForNonReferralUsersView1 = (TextView) findViewById(R.id.restrictions_for_non_referral_users_view_1);

        futurePlansView = (TextView) findViewById(R.id.future_plans_view);
        futurePlansView1 = (TextView) findViewById(R.id.future_plans_view_1);
    }


    /** Устанавливает локализованный текст во все текстовые блоки экрана. */
    private void setLanguage() {
        becomeReferralMainTextView.setText(R.string.become_referral_main_text);

        becomeReferralTextView.setText(R.string.become_referral_text);
        becomeReferralTextView1.setText(R.string.become_referral_text_1);
        becomeReferralTextView2.setText(R.string.become_referral_text_2);
        becomeReferralTextView3.setText(R.string.become_referral_text_3);

        referralRecoveryTextView.setText(R.string.referral_recovery_text);
        referralRecoveryTextView1.setText(R.string.referral_recovery_text_1);

        benefitsForReferralsAndTheirPartnersView.setText(R.string.benefits_for_referrals_and_their_partners);
        benefitsForReferralsAndTheirPartnersView1.setText(R.string.benefits_for_referrals_and_their_partners_1);
        benefitsForReferralsAndTheirPartnersView2.setText(R.string.benefits_for_referrals_and_their_partners_2);
        benefitsForReferralsAndTheirPartnersView3.setText(R.string.benefits_for_referrals_and_their_partners_3);

        restrictionsForNonReferralUsersView.setText(R.string.restrictions_for_non_referral_users);
        restrictionsForNonReferralUsersView1.setText(R.string.restrictions_for_non_referral_users_1);

        futurePlansView.setText(R.string.future_plans);
        futurePlansView1.setText(R.string.future_plans_1);
    }




    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
