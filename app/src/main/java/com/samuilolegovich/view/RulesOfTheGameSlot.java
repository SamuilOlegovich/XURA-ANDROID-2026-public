package com.samuilolegovich.view;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;

import dagger.hilt.android.AndroidEntryPoint;



/** Экран правил слот-машины. */
@AndroidEntryPoint
public class RulesOfTheGameSlot extends BaseActivity {
    public static final String RULES_SLOT_CLASS = ".RulesOfTheGameSlot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_slot_page);
        TextView tv = findViewById(R.id.tv_rules_content);
        tv.setText(Html.fromHtml(buildRules(), Html.FROM_HTML_MODE_LEGACY));
    }

    private String buildRules() {
        return getString(R.string.slot_rules_content);
    }
}
