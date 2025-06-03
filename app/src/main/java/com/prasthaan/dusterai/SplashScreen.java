package com.prasthaan.dusterai;

import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 8000; // 4 seconds
    private VideoView splashVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Make it full screen (hide status bar & navigation bar)
//        Window window = getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            window.getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
//        }
////        splashVideo = findViewById(R.id.splashVideo);
//        PlayerView playerView = findViewById(R.id.playerView);
//        ExoPlayer player = new ExoPlayer.Builder(this).build();
//        playerView.setPlayer(player);
////        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_intro);
////        splashVideo.setVideoURI(videoUri);
////
////        splashVideo.setOnCompletionListener(mp -> {
////            // Start your main activity
////            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
////            startActivity(intent);
////            finish();
////        });
////
////
////        splashVideo.start();
//
//        // Load video from raw
//        MediaItem mediaItem = MediaItem.fromUri("android.resource://" + getPackageName() + "/" + R.raw.splash_intro_one);
//        player.setMediaItem(mediaItem);
//        player.setRepeatMode(Player.REPEAT_MODE_OFF); // loop
//        player.prepare();
//        player.play();
//
//// Optionally navigate after 7s
//        new Handler().postDelayed(() -> {
//            player.release();
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }, 8000);
    }
}