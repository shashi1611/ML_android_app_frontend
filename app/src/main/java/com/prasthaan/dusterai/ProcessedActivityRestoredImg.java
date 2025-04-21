package com.prasthaan.dusterai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasthaan.dusterai.Adapters.AdapterResultRestoImg;
import com.prasthaan.dusterai.Models.ModalResultRestoImg;

import java.util.ArrayList;

public class ProcessedActivityRestoredImg extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_processed_restored_img);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageViewRestoImg = findViewById(R.id.download_img_result_resto);
        RecyclerView recyclerViewImageRestorationRes = findViewById(R.id.recyclerView_result_list_image_restoration);
        TextView textView = findViewById(R.id.feature_banner_image_restoratiion);
        ArrayList<ModalResultRestoImg> listImageRestorationRes = new ArrayList<>();

        Intent intent = getIntent();

        String restoredImageUrl = intent.getStringExtra("RESTORED_IMAGE_URL");

        ArrayList<String> faceUrls = intent.getStringArrayListExtra("RESTORED_FACE_URLS");
        if (restoredImageUrl != null) {
//            Log.d("PresignedURL", "Received URL: " + presignedUrl);
            //                JSONObject jsonObject = new JSONObject(presignedUrl);
//                String imageUrl = jsonObject.getString("output"); // Extract the actual URL

            Glide.with(this)
                    .load(restoredImageUrl)
                    .placeholder(R.drawable.loadingimagepleasewait)
                    .error(R.drawable.errorloadingimage)
                    .into(imageViewRestoImg);


        } else {
            Toast.makeText(this, "Image URL not received", Toast.LENGTH_SHORT).show();
        }

        if (!faceUrls.isEmpty()) {
            for (String url : faceUrls) {
                listImageRestorationRes.add(new ModalResultRestoImg(url));
            }
            AdapterResultRestoImg adapterResultRestoImg = new AdapterResultRestoImg(listImageRestorationRes, this);
            recyclerViewImageRestorationRes.setAdapter(adapterResultRestoImg);
            LinearLayoutManager layoutManagerImageRestoration = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerViewImageRestorationRes.setLayoutManager(layoutManagerImageRestoration);
            recyclerViewImageRestorationRes.setNestedScrollingEnabled(false);
            recyclerViewImageRestorationRes.setOverScrollMode(View.OVER_SCROLL_NEVER);


        } else {
            textView.setText("No faces found");
        }


// Now you can use Glide or any image loader to show them
        Log.d("Restored hello Image URL", restoredImageUrl);
        for (String url : faceUrls) {
            Log.d("Restored hello Face URL", url);
        }


//        RecyclerView recyclerViewImageRestorationRes = findViewById(R.id.recyclerView_result_list_image_restoration);
//        ArrayList<ModalResultRestoImg> listImageRestorationRes = new ArrayList<>();
//        listImageRestorationRes.add(new ModalResultRestoImg(listImageRestorationRes.get(0)));
//        FeatListModalAdapterImageRestoration featListModalAdapterImageRestoration = new FeatListModalAdapterImageRestoration(listImageRestoration, this);
//        recyclerViewImageRestoration.setAdapter(featListModalAdapterImageRestoration);
//        LinearLayoutManager layoutManagerImageRestoratiion = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        recyclerViewImageRestoration.setLayoutManager(layoutManagerImageRestoratiion);
//        recyclerViewImageRestoration.setNestedScrollingEnabled(false);
//        recyclerViewImageRestoration.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
}