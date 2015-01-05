package com.mzinck.blockjump.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.mzinck.blockjump.BlockJump;
import com.mzinck.blockjump.androidcontroller.AndroidRequestHandler;

public class AndroidLauncher extends AndroidApplication implements AndroidRequestHandler {
	
	private static final String AD_UNIT_ID = "ca-app-pub-1859904243523672/6072384277";
	private AdView adView;
	private RelativeLayout.LayoutParams layoutParams;
	private final int SHOW_ADS = 1;
    private final int HIDE_ADS = 0;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		RelativeLayout layout = new RelativeLayout(this);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
       
		View gameView = initializeForView(new BlockJump(this));
		
		AdView adMobView = createAdView();// Put in your secret key here this, AdSize.BANNER, "xxxxxxxx"
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
		//adRequestBuilder.addTestDevice("93F07773A581EBFA7B9832E76C04F75B");

	    adMobView.loadAd(adRequestBuilder.build());	
	    adMobView.setVisibility(View.VISIBLE);
	    layout.addView(gameView);
	    RelativeLayout.LayoutParams adParams = layoutParams();
        layout.addView(adMobView, adParams);
        
        setContentView(layout);
	}
	
	public AdView createAdView() {
		adView = new AdView(this);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId(AD_UNIT_ID);
		
		return adView;
	}
	
	public RelativeLayout.LayoutParams layoutParams() {
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		return layoutParams;		
	}
	
    @SuppressLint("HandlerLeak")
	protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SHOW_ADS:
                    adView.setVisibility(View.VISIBLE);
                    break;
                case HIDE_ADS:
                    adView.setVisibility(View.GONE);
                    break;
            }
        }
    };
    
	@Override
	public void onResume() {
		super.onResume();
		if(adView != null) {
			adView.resume();
		}
	}
	
	@Override
	public void onPause() {
		if(adView != null) {
			adView.pause();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if(adView != null) {
			adView.destroy();
		}
		super.onDestroy();		
	}
	
	@Override
	public void showAds() {
		handler.sendEmptyMessage(SHOW_ADS);
	}
	
	@Override
	public void hideAds() {
		handler.sendEmptyMessage(HIDE_ADS);
	}
	
	@Override
	public void destroyAds() {
		adView.destroy();
	}
	
}
