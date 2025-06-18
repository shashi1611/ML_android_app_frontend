package com.prasthaan.dusterai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ContactUsPage extends BaseMenuActivity {

//    @Override
//    public void onBackPressed() {
//        boolean fromBottomNav = getIntent().getBooleanExtra("fromBottomNav", false);
//
//        if (fromBottomNav) {
//            String caller = getIntent().getStringExtra("caller");
//
//            if (caller != null) {
//                try {
//                    // Reconstruct full class name based on your package
//                    String packageName = getApplicationContext().getPackageName();
//                    Class<?> callerClass = Class.forName(packageName + "." + caller);
//
//                    Intent intent = new Intent(this, callerClass);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                    finish();
//                    return;
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace(); // Fallback if class not found
//                }
//            }
//        }
//
//        super.onBackPressed(); // Default behavior if nothing matches
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_contact_us_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation(bottomNavigationView, R.id.nav_contact);
        findViewById(R.id.email_section).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"shashikantkuma69@gmail.com"});
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        findViewById(R.id.website_section).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dusterai.blogspot.com/"));
            startActivity(browserIntent);
        });

//        findViewById(R.id.whatsapp_section).setOnClickListener(v -> {
//            String phone = "+919876543210"; // your support number
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse("https://wa.me/" + phone));
//            startActivity(i);
//        });


        findViewById(R.id.rate_us_section).setOnClickListener(v -> {
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        findViewById(R.id.privacy_policy_section).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.termsfeed.com/live/78532296-2325-4114-b392-2287fb1f1c75"));
            startActivity(browserIntent);
        });

        findViewById(R.id.whatsapp_section).setOnClickListener(v -> {
            String instagramUrl = "https://www.instagram.com/dusterai01?igsh=MWxyNGJjNmpqc29oaQ=="; // replace with your actual handle
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(instagramUrl));
            intent.setPackage("com.instagram.android");

            // If Instagram app is not installed, open in browser
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)));
            }
        });

    }
}