package com.prasthaan.dusterai;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class ReviewHelper {

    private static final String PREF_HAS_REVIEWED = "has_reviewed";
    private static final String PREF_LAUNCH_COUNT = "launch_count";

    public static void launchReviewIfEligible(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean hasReviewed = prefs.getBoolean(PREF_HAS_REVIEWED, false);
        int launchCount = prefs.getInt(PREF_LAUNCH_COUNT, 0);

        launchCount++;
        prefs.edit().putInt(PREF_LAUNCH_COUNT, launchCount).apply();


        if (!hasReviewed && (launchCount == 3 || launchCount == 8 || launchCount == 13 || launchCount == 15 || launchCount == 18)) {
            ReviewManager manager = ReviewManagerFactory.create(activity);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                    flow.addOnCompleteListener(flowTask -> {
                        prefs.edit().putBoolean(PREF_HAS_REVIEWED, true).apply();
                    });
                }
            });
        }
    }
}
