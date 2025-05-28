package com.prasthaan.dusterai.Adapters;

//public class AdapterResultRestoImg {
//}

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.prasthaan.dusterai.Models.ModalResultRestoImg;
import com.prasthaan.dusterai.ProcessedActivityRestoredImg;
import com.prasthaan.dusterai.R;

import java.util.ArrayList;

public class AdapterResultRestoImg extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FEATURE = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final String Native_AD_UNIT_ID_image_restoration_result_feat1 = "ca-app-pub-4827086355311757/7458435573";
    String development_test_ad = "ca-app-pub-3940256099942544/2247696110";
    ArrayList<ModalResultRestoImg> list;
    Context context;

    public AdapterResultRestoImg(ArrayList<ModalResultRestoImg> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            View adView = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, parent, false);
            return new AdViewHolder(adView);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_resto_img_activity, parent, false);
            return new FeatureViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_AD) {
            AdViewHolder adHolder = (AdViewHolder) holder;

//            AdLoader adLoader = new AdLoader.Builder(context, Native_AD_UNIT_ID_image_restoration_result_feat1) //prod ad
            AdLoader adLoader = new AdLoader.Builder(context, development_test_ad) //test ad
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                    .inflate(R.layout.native_ad_item, null);

                            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                            int screenWidth = displayMetrics.widthPixels;
                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                            adView.setLayoutParams(layoutParams);


                            populateNativeADView(nativeAd, adView);

                            adHolder.adContainer.removeAllViews();
                            adHolder.adContainer.addView(adView);
                        }
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            super.onAdFailedToLoad(adError);
                            Log.d("NativeAd", "Ad failed to load: " + adError.getMessage());

                            // Optionally hide or remove ad container
                            adHolder.adContainer.setVisibility(View.GONE);
                        }
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());

        } else {

            ModalResultRestoImg model = list.get(position);
            FeatureViewHolder featureHolder = (FeatureViewHolder) holder;
            Glide.with(context)
                    .load(model.getImgUrl())  // This should be a String URL
//                    .placeholder(R.drawable.placeholder)  // optional: show while loading
//                    .error(R.drawable.error_image)        // optional: show on error
                    .into(featureHolder.imageView);
//            featureHolder.imageView.setImageResource(model.getImg());
//            featureHolder.textView.setText(model.getFeat_name());

//        expriment doing

            DisplayMetrics displayMetrics = featureHolder.itemView.getContext().getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;

            ViewGroup.LayoutParams layoutParams = featureHolder.itemView.getLayoutParams();
            layoutParams.width = screenWidth / 2; // Set each item to half of screen width
            featureHolder.itemView.setLayoutParams(layoutParams);


//        tilll here

//            featureHolder.itemView.setOnClickListener((view) -> {
////                Intent intent = new Intent(view.getContext(), ProcessingActivity.class);
////                String text = featureHolder.textView.getText().toString();
////                intent.putExtra("text_key", text);
////                view.getContext().startActivity(intent);
//            });
            featureHolder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();

                    // Safely call downloadImage() from the context if it's an instance of your Activity
                    if (context instanceof ProcessedActivityRestoredImg) {
                        ((ProcessedActivityRestoredImg) context).downloadImage(model.getImgUrl());
                    } else {
                        Toast.makeText(context, "Unable to start download", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }


    @Override
    public int getItemCount() {
        return list.size();

//        return list.size() > 0 ? list.size() + 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_FEATURE;


//        if (position == list.size()) {
//            return VIEW_TYPE_AD;
//        } else {
//            return VIEW_TYPE_FEATURE;
//        }

    }

    private void populateNativeADView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
//        adView.setBodyView(adView.findViewById(R.id.ad_body));
//        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
//        adView.setPriceView(adView.findViewById(R.id.ad_price));
//        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
//        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
//        if (nativeAd.getBody() == null) {
//            adView.getBodyView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getBodyView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
//        }

//        if (nativeAd.getCallToAction() == null) {
//            adView.getCallToActionView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getCallToActionView().setVisibility(View.VISIBLE);
//            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
//        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

//        if (nativeAd.getPrice() == null) {
//            adView.getPriceView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getPriceView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
//        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

//        if (nativeAd.getStarRating() == null) {
//            adView.getStarRatingView().setVisibility(View.INVISIBLE);
//        } else {
//            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
//            adView.getStarRatingView().setVisibility(View.VISIBLE);
//        }

//        if (nativeAd.getAdvertiser() == null) {
//            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
//        } else {
//            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
//            adView.getAdvertiserView().setVisibility(View.VISIBLE);
//        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

//    private void downloadImage(String imageUrl) {
//        try {
//            // Generate a unique filename
//            String downloadedImageName = "downloaded_image_" + System.currentTimeMillis() + ".jpg";
//
//            // Create Download Manager Request
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
//
//            // Set Notification details
//            request.setTitle("Downloading Image");
//            request.setDescription("Saving image to Downloads folder...");
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//            // Set the download destination
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadedImageName);
//
//            // Get the system Download Manager
//            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//
//            // Enqueue the request (start downloading)
//            long downloadId = downloadManager.enqueue(request);
//
//            // Show a toast that download started
//            Toast.makeText(context, "Download started, check notification", Toast.LENGTH_SHORT).show();
//
//            // Check Download Status (Optional)
//            new Thread(() -> {
//                boolean downloading = true;
//                while (downloading) {
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(downloadId);
//                    Cursor cursor = downloadManager.query(query);
//                    if (cursor.moveToFirst()) {
//                        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
//                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                            downloading = false;
//                            runOnUiThread(() -> Toast.makeText(context, "Download Complete!", Toast.LENGTH_SHORT).show());
//                        }
//                    }
//                    cursor.close();
//                }
//            }).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        //        ViewGroup adContainer;
        FrameLayout adContainer;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
//            adContainer = itemView.findViewById(R.id.native_ad_container);
            adContainer = itemView.findViewById(R.id.adLayout);
        }
    }

    public static class FeatureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button btn;
//        TextView textView;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.result_face_img_resto);
            btn = itemView.findViewById(R.id.button_download_face_resto_img);
//            textView = itemView.findViewById(R.id.feat_title);
        }
    }


}
