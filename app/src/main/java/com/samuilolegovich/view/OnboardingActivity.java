package com.samuilolegovich.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;

public class OnboardingActivity extends AppCompatActivity {

    public static final String ONBOARDING_CLASS = ".OnboardingActivity";

    // Слайды: 0=лого, 1=🚀, 2=🎲, 3=face scan
    private static final String[] EMOJIS = {"", "🚀", "🎲", ""};
    private static final int[]    IMAGES = {
        R.drawable.ic_xura_logo,  // slide 0: XURA logo (без тинта)
        0,                         // slide 1: emoji
        0,                         // slide 2: emoji
        R.drawable.ic_face_scan   // slide 3: face scan (cyan)
    };
    private static final int[] TITLES    = {R.string.onb_title_1, R.string.onb_title_2, R.string.onb_title_3, R.string.onb_title_4};
    private static final int[] SUBTITLES = {R.string.onb_sub_1,   R.string.onb_sub_2,   R.string.onb_sub_3,   R.string.onb_sub_4};
    private static final int[] ICON_COLORS = {
        0,                    // 0: лого — без тинта
        R.color.xura_gold,   // 1: 🚀 золотая
        R.color.xura_pink,   // 2: 🎲 розовая
        R.color.xura_cyan    // 3: face scan cyan
    };

    private ViewPager2   pager;
    private LinearLayout dotsContainer;
    private View         btnNext;
    private TextView     btnTitle;
    private TextView     btnSubtitle;
    private TextView     btnSkip;
    private ImageView[]  dots;
    private Animator     currentIconAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager         = findViewById(R.id.onb_pager);
        dotsContainer = findViewById(R.id.onb_dots);
        btnNext       = findViewById(R.id.onb_btn_next);
        btnTitle      = findViewById(R.id.onb_btn_title);
        btnSubtitle   = findViewById(R.id.onb_btn_subtitle);
        btnSkip       = findViewById(R.id.onb_skip);

        pager.setAdapter(new PagerAdapter());
        pager.setPageTransformer(new DepthTransformer());
        buildDots(0);

        pager.post(() -> animateIconAtPosition(0));

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                boolean isLast = position == EMOJIS.length - 1;
                btnTitle.setText(isLast ? R.string.get_started : R.string.next);
                btnSubtitle.setText(isLast ? R.string.btn_sub_get_started : R.string.btn_sub_next);
                btnSkip.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
                // post() гарантирует, что ViewHolder уже привязан и видим
                pager.post(() -> animateIconAtPosition(position));
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = pager.getCurrentItem();
            if (current < EMOJIS.length - 1) {
                pager.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    // ── Анимации иконок ──────────────────────────────────────────────────────

    private void animateIconAtPosition(int position) {
        RecyclerView rv = (RecyclerView) pager.getChildAt(0);
        if (rv == null) return;
        RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
        if (!(vh instanceof PagerAdapter.PageHolder)) return;
        PagerAdapter.PageHolder holder = (PagerAdapter.PageHolder) vh;

        View icon = IMAGES[position] != 0 ? holder.iconImage : holder.iconText;

        if (currentIconAnim != null) currentIconAnim.cancel();

        // Сброс всех трансформаций
        icon.setScaleX(1f); icon.setScaleY(1f);
        icon.setRotation(0f); icon.setTranslationY(0f); icon.setTranslationX(0f);
        icon.setAlpha(1f);

        // Скрыть луч на всех слайдах кроме 3
        holder.scanBeam.setAlpha(0f);
        holder.scanBeam.setTranslationY(0f);

        currentIconAnim = buildIconAnim(icon, holder.scanBeam, position);
        currentIconAnim.start();
    }

    private Animator buildIconAnim(View icon, View scanBeam, int position) {
        switch (position) {

            case 0: { // XURA logo — плавный пульс
                ObjectAnimator sx = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.06f, 1f);
                ObjectAnimator sy = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.06f, 1f);
                sx.setDuration(2000); sx.setRepeatCount(ValueAnimator.INFINITE);
                sy.setDuration(2000); sy.setRepeatCount(ValueAnimator.INFINITE);
                sx.setInterpolator(new AccelerateDecelerateInterpolator());
                sy.setInterpolator(new AccelerateDecelerateInterpolator());
                AnimatorSet set = new AnimatorSet();
                set.playTogether(sx, sy);
                return set;
            }

            case 1: { // 🚀 — плавает вверх-вниз
                float dp24 = 24 * getResources().getDisplayMetrics().density;
                ObjectAnimator anim = ObjectAnimator.ofFloat(icon, "translationY", 0f, -dp24, 0f);
                anim.setDuration(1900);
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                return anim;
            }

            case 2: { // 🎲 — качается
                ObjectAnimator anim = ObjectAnimator.ofFloat(icon, "rotation", 0f, -18f, 0f, 18f, 0f);
                anim.setDuration(1100);
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                return anim;
            }

            case 3: { // Face scan — луч сканирует + иконка мягко дышит
                float density = getResources().getDisplayMetrics().density;
                float sweepRange = 55 * density; // ±55dp от центра FrameLayout

                // Луч появляется и сканирует вверх-вниз
                scanBeam.setAlpha(0.85f);
                ObjectAnimator sweep = ObjectAnimator.ofFloat(scanBeam, "translationY", -sweepRange, sweepRange);
                sweep.setDuration(1400);
                sweep.setRepeatCount(ValueAnimator.INFINITE);
                sweep.setRepeatMode(ValueAnimator.REVERSE);
                sweep.setInterpolator(new AccelerateDecelerateInterpolator());

                // Иконка слегка пульсирует
                ObjectAnimator sx = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.07f, 1f);
                ObjectAnimator sy = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.07f, 1f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0.7f, 1f);
                sx.setDuration(2800);    sx.setRepeatCount(ValueAnimator.INFINITE);
                sy.setDuration(2800);    sy.setRepeatCount(ValueAnimator.INFINITE);
                alpha.setDuration(2800); alpha.setRepeatCount(ValueAnimator.INFINITE);
                sx.setInterpolator(new AccelerateDecelerateInterpolator());
                sy.setInterpolator(new AccelerateDecelerateInterpolator());
                alpha.setInterpolator(new AccelerateDecelerateInterpolator());

                AnimatorSet set = new AnimatorSet();
                set.playTogether(sweep, sx, sy, alpha);
                return set;
            }

            default:
                return ObjectAnimator.ofFloat(icon, "alpha", 1f);
        }
    }

    // ── Утилиты ──────────────────────────────────────────────────────────────

    private void finishOnboarding() {
        SharedPreferences prefs = PrefsHelper.get(this);
        prefs.edit().putBoolean(StringEnum.ONBOARDING_SEEN.getValue(), true).apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void buildDots(int selectedPosition) {
        dotsContainer.removeAllViews();
        dots = new ImageView[EMOJIS.length];
        int margin = (int) (6 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < EMOJIS.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(i == selectedPosition ? R.drawable.ic_dot_active : R.drawable.ic_dot_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(margin, 0, margin, 0);
            dotsContainer.addView(dots[i], params);
        }
    }

    private void updateDots(int selectedPosition) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == selectedPosition ? R.drawable.ic_dot_active : R.drawable.ic_dot_inactive);
        }
    }

    @Override
    protected void onDestroy() {
        if (currentIconAnim != null) currentIconAnim.cancel();
        super.onDestroy();
    }

    // ── Трансформер переходов (Google ZoomOut) ────────────────────────────────

    private static class DepthTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            if (position < -1) {
                page.setAlpha(0f);
            } else if (position <= 1) {
                float scale = Math.max(MIN_SCALE, 1f - Math.abs(position));
                float vMargin = page.getHeight() * (1f - scale) / 2f;
                float hMargin = page.getWidth()  * (1f - scale) / 2f;
                page.setTranslationX(position < 0
                        ? hMargin - vMargin / 2f
                        : -hMargin + vMargin / 2f);
                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setAlpha(MIN_ALPHA + (scale - MIN_SCALE) / (1f - MIN_SCALE) * (1f - MIN_ALPHA));
            } else {
                page.setAlpha(0f);
            }
        }
    }

    // ── Адаптер страниц ───────────────────────────────────────────────────────

    private class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.PageHolder> {

        @NonNull
        @Override
        public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new PageHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PageHolder holder, int position) {
            if (IMAGES[position] != 0) {
                holder.iconImage.setVisibility(View.VISIBLE);
                holder.iconText.setVisibility(View.GONE);
                holder.iconImage.setImageResource(IMAGES[position]);
                // face scan чуть меньше логотипа — добавляем отступ
                int pad = (int) ((position == 3 ? 28 : 0) * getResources().getDisplayMetrics().density);
                holder.iconImage.setPadding(pad, pad, pad, pad);
                if (ICON_COLORS[position] != 0) {
                    holder.iconImage.setColorFilter(getColor(ICON_COLORS[position]));
                } else {
                    holder.iconImage.clearColorFilter();
                }
            } else {
                holder.iconImage.setVisibility(View.GONE);
                holder.iconText.setVisibility(View.VISIBLE);
                holder.iconText.setText(EMOJIS[position]);
                holder.iconText.setTextColor(getColor(ICON_COLORS[position]));
            }
            holder.title.setText(TITLES[position]);
            holder.subtitle.setText(SUBTITLES[position]);
        }

        @Override
        public int getItemCount() { return EMOJIS.length; }

        class PageHolder extends RecyclerView.ViewHolder {
            final ImageView iconImage;
            final TextView  iconText;
            final View      scanBeam;
            final TextView  title;
            final TextView  subtitle;

            PageHolder(@NonNull View itemView) {
                super(itemView);
                iconImage = itemView.findViewById(R.id.onb_icon_image);
                iconText  = itemView.findViewById(R.id.onb_icon);
                scanBeam  = itemView.findViewById(R.id.onb_scan_beam);
                title     = itemView.findViewById(R.id.onb_title);
                subtitle  = itemView.findViewById(R.id.onb_subtitle);
            }
        }
    }
}
