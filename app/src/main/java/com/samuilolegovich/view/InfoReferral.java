package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_referral_page);
        setButtons();
        setLanguage();
    }



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




    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
