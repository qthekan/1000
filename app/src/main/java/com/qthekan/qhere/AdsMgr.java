package com.qthekan.qhere;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.qthekan.util.qlog;

public class AdsMgr
{
    private Context mContext;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    public AdsMgr(Context context, AdView adView)
    {
        mContext = context;
        mAdView = adView;
    }

    public void initAds()
    {
        //===========================================================
        // banner ads
        //===========================================================
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //===========================================================
        // interstitial ads
        //===========================================================
        // app id
        MobileAds.initialize(mContext, "ca-app-pub-6591940578118358~8770626226");

        // interstitial id
        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdUnitId("ca-app-pub-6591940578118358/2170532620");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // 계속 실패할 경우 무한루프로 돌아서 주석처리함.
                //loadInterAds();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                loadInterAds();
            }
        });
    }

    public void showInterAds()
    {
        if(mInterstitialAd.isLoaded())
        {
            mInterstitialAd.show();
        }
        else
        {
            Log.d("", "showInterAds() The interstitial wasn't loaded yet.");
        }
    }


    private void loadInterAds()
    {
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

}
