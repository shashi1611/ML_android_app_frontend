package com.prasthaan.dusterai.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.prasthaan.dusterai.Models.FeatListModel;
import com.prasthaan.dusterai.ProcessingActivity;
import com.prasthaan.dusterai.R;

import java.util.ArrayList;

public class FeatListModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FEATURE = 0;
    private static final int VIEW_TYPE_AD = 1;
    ArrayList<FeatListModel> list;
    Context context;

    public FeatListModelAdapter(ArrayList<FeatListModel> list, Context context) {
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
            View view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
            return new FeatureViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_AD) {
            AdViewHolder adHolder = (AdViewHolder) holder;

            AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                    .inflate(R.layout.native_ad_item, null);
                            populateNativeADView(nativeAd, adView);

                            adHolder.adContainer.removeAllViews();
                            adHolder.adContainer.addView(adView);
                        }
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            super.onAdFailedToLoad(adError);
                            // You can log it or show a fallback
                            Log.d("NativeAd", "Ad failed to load: " + adError.getMessage());

                            // Optionally hide or remove ad container
                            adHolder.adContainer.setVisibility(View.GONE);
                        }
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());


        } else {
            int pos = position - Math.round(position / 4);


            FeatListModel model = list.get(pos);
            FeatureViewHolder featureHolder = (FeatureViewHolder) holder;
            featureHolder.imageView.setImageResource(model.getImg());
            featureHolder.textView.setText(model.getFeat_name());

//        expriment doing

            DisplayMetrics displayMetrics = featureHolder.itemView.getContext().getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;

            ViewGroup.LayoutParams layoutParams = featureHolder.itemView.getLayoutParams();
            layoutParams.width = screenWidth / 2; // Set each item to half of screen width
            featureHolder.itemView.setLayoutParams(layoutParams);


//        tilll here

            featureHolder.itemView.setOnClickListener((view) -> {
                Intent intent = new Intent(view.getContext(), ProcessingActivity.class);
                String text = featureHolder.textView.getText().toString();
                intent.putExtra("text_key", text);
                view.getContext().startActivity(intent);
            });
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
//
//        if (holder instanceof AdViewHolder) {
//            AdViewHolder adHolder = (AdViewHolder) holder;
//
//            AdLoader.Builder builder = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
//                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
//                        @Override
//                        public void onNativeAdLoaded(NativeAd nativeAd) {
//                            NativeAdView nativeAdView =
//                            // Assumes you have a placeholder FrameLayout in your View layout
//                            // (with ID fl_adplaceholder) where the ad is to be placed.
////                            FrameLayout frameLayout =
////                                    findViewById(R.id.ad_view_container);
//                            // Assumes that your ad layout is in a file call native_ad_layout.xml
//                            // in the res/layout folder
////                            NativeAdView adView = (NativeAdView) getLayoutInflater()
////                                    .inflate(R.layout.native_ad_layout, null);
//                            // This method sets the assets into the ad view.
////                            populateNativeAdView(nativeAd, adView);
////                            frameLayout.removeAllViews();
////                            frameLayout.addView(adView);
//                        }
//                    });
//        } else {
//
//
//            FeatListModel model = list.get(position);
//            holder.imageView.setImageResource(model.getImg());
//            holder.textView.setText(model.getFeat_name());
//
////        expriment doing
//
//            DisplayMetrics displayMetrics = holder.itemView.getContext().getResources().getDisplayMetrics();
//            int screenWidth = displayMetrics.widthPixels;
//
//            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
//            layoutParams.width = screenWidth / 2; // Set each item to half of screen width
//            holder.itemView.setLayoutParams(layoutParams);
//
//
////        tilll here
//
//            holder.itemView.setOnClickListener((view) -> {
//                Intent intent = new Intent(view.getContext(), ProcessingActivity.class);
//                String text = holder.textView.getText().toString();
//                intent.putExtra("text_key", text);
//                view.getContext().startActivity(intent);
//            });
//        }
//
//    }

    @Override
    public int getItemCount() {

        if (list.size() > 0) {
            return list.size() + Math.round(list.size() / 4);
        }
        return list.size();
//        return list.size() + 1; // +1 for the ad
    }

    @Override
    public int getItemViewType(int position) {

        if ((position + 1) % 4 == 0) {
            return VIEW_TYPE_AD;
        } else {
            return VIEW_TYPE_FEATURE;
        }
//        return super.getItemViewType(position);
    }

    private void populateNativeADView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        ViewGroup adContainer;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            adContainer = itemView.findViewById(R.id.native_ad_container);
        }
    }

    public static class FeatureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.feat_img);
            textView = itemView.findViewById(R.id.feat_title);
        }
    }


}
