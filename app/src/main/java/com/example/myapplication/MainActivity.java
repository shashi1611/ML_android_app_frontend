package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.myapplication.Adapters.FeatListModelAdapter;
import com.example.myapplication.Adapters.FeatListModelAdapter2;
import com.example.myapplication.Models.FeatListModel;
import com.example.myapplication.Models.FeatListModel2;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


//        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_main);


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        ImageSlider imageSlider = findViewById(R.id.image_slider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.pexelspixabay161097, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.cor_s2w, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.pexelspixabay459225, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.cor_w2s, ScaleTypes.FIT));

        imageSlider.setImageList(slideModels);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_feat_list);
        ArrayList<FeatListModel> list = new ArrayList<>();
        list.add(new FeatListModel(R.drawable.feat1c, "Ukiyo-e style"));
        list.add(new FeatListModel(R.drawable.feat2c, "Monet style"));
        list.add(new FeatListModel(R.drawable.feat3c, "Van Gogh style"));
        list.add(new FeatListModel(R.drawable.feat4c, "Cezanne style"));

        FeatListModelAdapter featListModelAdapter = new FeatListModelAdapter(list, this);
        recyclerView.setAdapter(featListModelAdapter);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);

//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
//        recyclerView.setLayoutManager(gridLayoutManager);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        RecyclerView recyclerView2 = findViewById(R.id.recyclerView_feat_list2);
        ArrayList<FeatListModel2> list2 = new ArrayList<>();
        list2.add(new FeatListModel2(R.drawable.s2w, "To winter"));
        list2.add(new FeatListModel2(R.drawable.w2s, "To Summer"));

        FeatListModelAdapter2 featListModelAdapter2 = new FeatListModelAdapter2(list2, this);
        recyclerView2.setAdapter(featListModelAdapter2);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);

//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
//        recyclerView.setLayoutManager(gridLayoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);


        recyclerView2.setNestedScrollingEnabled(false);
        recyclerView2.setOverScrollMode(View.OVER_SCROLL_NEVER);


    }
}