package com.mobinators.ads.managers.applications;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public class AppRater {
    private final static String APP_TITLE = "your_app_name";
    private static String PACKAGE_NAME = "your_package_name";
    private static int DAYS_UNTIL_PROMPT = 5;
    private static int LAUNCHES_UNTIL_PROMPT = 10;
    private static long EXTRA_DAYS;
    private static long EXTRA_LAUCHES;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static Activity activity;

    public static void app_launched(Activity activity1) {
        activity = activity1;
//        Configs.sendScreenView("Avaliando App", activity);

        PACKAGE_NAME = activity.getPackageName();

        prefs = activity.getSharedPreferences("apprater", Context.MODE_PRIVATE);
        if (prefs.getBoolean("dontshowagain", false))
            return;

        editor = prefs.edit();

        EXTRA_DAYS = prefs.getLong("extra_days", 0);
        EXTRA_LAUCHES = prefs.getLong("extra_launches", 0);

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= (LAUNCHES_UNTIL_PROMPT + EXTRA_LAUCHES))
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) + EXTRA_DAYS)
                showRateDialog();

        editor.commit();
    }

    public static void showRateDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.setTitle("Deseja avaliar o aplicativo " + APP_TITLE + "?");

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(5, 5, 5, 5);

        TextView tv = new TextView(activity);
//        tv.setTextColor(activity.getResources().getColor(R.color.default_text));
        tv.setText("Ajude-nos a melhorar o aplicativo com sua avaliação no Google Play!");
        tv.setWidth(240);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(5, 5, 5, 5);
        ll.addView(tv);

        Button b1 = new Button(activity);
//        b1.setTextColor(activity.getResources().getColor(R.color.default_text));
//        b1.setBackground(activity.getResources().getDrawable(R.drawable.rounded_blue_box));
        b1.setTextColor(Color.WHITE);
        b1.setText("Avaliar aplicativo " + APP_TITLE + "!");
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Configs.sendHitEvents(Configs.APP_RATER, Configs.CATEGORIA_ANALYTICS, "Clique", "Avaliar", activity);
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME)));
                delayDays(60);
                delayLaunches(30);
                dialog.dismiss();
            }
        });
        ll.addView(b1);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) b1.getLayoutParams();
        params.setMargins(5, 3, 5, 3);
        b1.setLayoutParams(params);

        Button b2 = new Button(activity);
//        b2.setTextColor(activity.getResources().getColor(R.color.default_text));
//        b2.setBackground(activity.getResources().getDrawable(R.drawable.rounded_blue_box));
        b2.setTextColor(Color.WHITE);
        b2.setText("Lembre-me mais tarde!");
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Configs.sendHitEvents(Configs.APP_RATER, Configs.CATEGORIA_ANALYTICS, "Clique", "Avaliar Mais Tarde", activity);
                delayDays(3);
                delayLaunches(10);
                dialog.dismiss();
            }
        });
        ll.addView(b2);
        params = (LinearLayout.LayoutParams) b2.getLayoutParams();
        params.setMargins(5, 3, 5, 3);
        b2.setLayoutParams(params);

        Button b3 = new Button(activity);
//        b3.setTextColor(activity.getResources().getColor(R.color.default_text));
//        b3.setBackground(activity.getResources().getDrawable(R.drawable.rounded_blue_box));
        b3.setTextColor(Color.WHITE);
        b3.setText("Não, obrigado!");
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Configs.sendHitEvents(Configs.APP_RATER, Configs.CATEGORIA_ANALYTICS, "Clique", "Não Avaliar", activity);
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);
        params = (LinearLayout.LayoutParams) b3.getLayoutParams();
        params.setMargins(5, 3, 5, 0);
        b3.setLayoutParams(params);
        dialog.setContentView(ll);
        dialog.show();
    }

    private static void delayLaunches(int numberOfLaunches) {
        long extra_launches = prefs.getLong("extra_launches", 0) + numberOfLaunches;
        editor.putLong("extra_launches", extra_launches);
        editor.commit();
    }

    private static void delayDays(int numberOfDays) {
        Long extra_days = prefs.getLong("extra_days", 0) + (numberOfDays * 1000 * 60 * 60 * 24);
        editor.putLong("extra_days", extra_days);
        editor.commit();
    }
}
