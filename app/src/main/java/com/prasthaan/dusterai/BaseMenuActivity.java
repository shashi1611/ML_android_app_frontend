package com.prasthaan.dusterai;

//public class BaseMenuActivity {
//
//}

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseMenuActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "home page activated", Toast.LENGTH_SHORT).show();
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                return true;
            } else if (id == R.id.nav_history) {
                Toast.makeText(this, "history page activated", Toast.LENGTH_SHORT).show();
                if (!(this instanceof HistoryPage)) {
                    startActivity(new Intent(this, HistoryPage.class));
                    finish();
                }
                return true;
            } else if (id == R.id.nav_contact) {
                Toast.makeText(this, "contactUS page activated", Toast.LENGTH_SHORT).show();
                if (!(this instanceof ContactUsPage)) {
                    startActivity(new Intent(this, ContactUsPage.class));
                    finish();
                }
                return true;
            }
            return false;
        });
    }
}

