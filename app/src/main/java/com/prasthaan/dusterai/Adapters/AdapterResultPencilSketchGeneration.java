package com.prasthaan.dusterai.Adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.prasthaan.dusterai.Models.ModelResultPencilSketchGeneration;
import com.prasthaan.dusterai.ProcessedActivityPencilSketchGeneration;
import com.prasthaan.dusterai.R;

import java.util.ArrayList;

public class AdapterResultPencilSketchGeneration extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FEATURE = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final String Native_AD_UNIT_ID_image_restoration_feat1 = "ca-app-pub-4827086355311757/9541898776";
    String development_test_ad = "ca-app-pub-3940256099942544/2247696110";
    ArrayList<ModelResultPencilSketchGeneration> list;
    Context context;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    public AdapterResultPencilSketchGeneration(ArrayList<ModelResultPencilSketchGeneration> list, Context context, RecyclerView recyclerView, RecyclerView.LayoutManager layoutManager) {
        this.list = list;
        this.context = context;
        this.recyclerView = recyclerView;
        this.layoutManager = layoutManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            View adView = LayoutInflater.from(context).inflate(R.layout.native_ad_layout, parent, false);
            return new AdapterResultPencilSketchGeneration.AdViewHolder(adView);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.single_item_result_pencil_sketch_generation, parent, false);
            return new AdapterResultPencilSketchGeneration.FeatureViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_AD) {
            AdapterResultPencilSketchGeneration.AdViewHolder adHolder = (AdapterResultPencilSketchGeneration.AdViewHolder) holder;

            AdLoader adLoader = new AdLoader.Builder(context, Native_AD_UNIT_ID_image_restoration_feat1) // prod ad
//            AdLoader adLoader = new AdLoader.Builder(context, development_test_ad) // test ad
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
                            // Optionally hide or remove ad container
                            adHolder.adContainer.setVisibility(View.GONE);
                        }
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());

        } else {

            ModelResultPencilSketchGeneration model = list.get(position);
            AdapterResultPencilSketchGeneration.FeatureViewHolder featureHolder = (AdapterResultPencilSketchGeneration.FeatureViewHolder) holder;
            Glide.with(context)
                    .load(model.getResultImg())  // This should be a String URL
                    .placeholder(R.drawable.loadingimagepleasewait)  // optional: show while loading
                    .error(R.drawable.errorloadingimage)        // optional: show on error
                    .into(featureHolder.imageView);


            featureHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.animate()
                            .scaleX(0.85f)
                            .scaleY(0.85f)
                            .alpha(0.6f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .alpha(1f)
                                        .setDuration(100)
                                        .start();
                                if (context instanceof ProcessedActivityPencilSketchGeneration) {
                                    ((ProcessedActivityPencilSketchGeneration) context).downloadImage(model.getResultImg());
                                } else {
                                    Toast.makeText(context, "Unable to start download", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .start();

                }
            });

            featureHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    v.animate()
                            .scaleX(0.85f)
                            .scaleY(0.85f)
                            .alpha(0.6f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .alpha(1f)
                                        .setDuration(100)
                                        .start();
                                if (context instanceof ProcessedActivityPencilSketchGeneration) {
                                    ((ProcessedActivityPencilSketchGeneration) context).shareImageFromPresignedUrl(model.getResultImg());
                                } else {
                                    Toast.makeText(context, "Unable to start download", Toast.LENGTH_SHORT).show();
                                }

                            })
                            .start();

                }
            });

            featureHolder.rightArrowBtn.setOnClickListener(v -> {
                int nextPos = featureHolder.getAdapterPosition() + 1;
                if (nextPos < list.size()) {
                    recyclerView.smoothScrollToPosition(nextPos);
                }
            });

            ((FeatureViewHolder) holder).leftArrowBtn.setOnClickListener(v -> {
                int prevPos = holder.getAdapterPosition() - 1;
                if (prevPos >= 0) {
                    recyclerView.smoothScrollToPosition(prevPos);
                }
            });


        }
    }


    @Override
    public int getItemCount() {

//        return list.size() > 0 ? list.size() + 1 : 0;
        return list.size();
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
        ImageView shareBtn, downloadBtn, leftArrowBtn, rightArrowBtn;


        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.result_edge_pencil_sketch_generation);
            shareBtn = itemView.findViewById(R.id.result_share_btn_pencil_sketch_generation);
            downloadBtn = itemView.findViewById(R.id.result_download_btn_pencil_sketch_generation);
            leftArrowBtn = itemView.findViewById(R.id.arrow_back);
            rightArrowBtn = itemView.findViewById(R.id.arrow_forward);


        }
    }
}
