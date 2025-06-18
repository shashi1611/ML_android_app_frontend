package com.prasthaan.dusterai;

//public class BaseMenuActivity {
//
//}

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseMenuActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            if (this instanceof MainActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            } else if (this instanceof HistoryPage) {
                bottomNavigationView.setSelectedItemId(R.id.nav_history);
            } else if (this instanceof ContactUsPage) {
                bottomNavigationView.setSelectedItemId(R.id.nav_contact);
            } else {
                // For pages like Processing, Result, etc. that are not in bottom nav
                bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                    bottomNavigationView.getMenu().getItem(i).setChecked(false);
                }
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
            }
        }
    }

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView, int selectedItemId) {

        bottomNavigationView.setSelectedItemId(selectedItemId);

        if (selectedItemId != -1 && bottomNavigationView.getSelectedItemId() != selectedItemId) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Context context = bottomNavigationView.getContext();
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
//                    overridePendingTransition(0, 0);
                    finish();
                }
                return true;
            } else if (id == R.id.nav_contact) {
                if (!(this instanceof ContactUsPage)) {
                    startActivity(new Intent(this, ContactUsPage.class));
//                    overridePendingTransition(0, 0);
                    if (this instanceof HistoryPage) {
                        finish();
                    }
                }
                return true;
            } else if (id == R.id.nav_history) {
                if (!(this instanceof HistoryPage)) {
                    startActivity(new Intent(this, HistoryPage.class));
//                    overridePendingTransition(0, 0);
                    if (this instanceof ContactUsPage) {
                        finish();
                    }
                }
                return true;
            }
            return false;
        });
    }


}

