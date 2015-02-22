package com.mzinck.blockjump;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDynamicItem;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIView;
import org.robovm.bindings.admob.GADAdSize;
import org.robovm.bindings.admob.GADBannerView;
import org.robovm.bindings.admob.GADBannerViewDelegateAdapter;
import org.robovm.bindings.admob.GADRequest;
import org.robovm.bindings.admob.GADRequestError;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.utils.Logger;
import com.mzinck.blockjump.mobilecontroller.AdRequestHandler;

public class IOSLauncher extends IOSApplication.Delegate implements AdRequestHandler {
	
	private GADBannerView adview;
	private boolean adsInitialized = false;
	private IOSApplication iosApplication;
	private final String APP_ID = "ca-app-pub-1859904243523672/5452628675";

	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.orientationLandscape = true;
		config.orientationPortrait = false;

		iosApplication = new IOSApplication(new BlockJump(this), config);
		return iosApplication;
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSLauncher.class);
		pool.close();
	}

	@Override
	public void hideAds() {
		initializeAds();

		final CGSize screenSize = UIScreen.getMainScreen().getBounds().size();
		double screenWidth = screenSize.width();

		final CGSize adSize = adview.getBounds().size();
		double adWidth = adSize.width();
		double adHeight = adSize.height();

		float bannerWidth = (float) screenWidth;
		float bannerHeight = (float) (bannerWidth / adWidth * adHeight);

		adview.setFrame(new CGRect(0, -bannerHeight, bannerWidth, bannerHeight));
	}

	@Override
	public void show() {
		initializeAds();

		final CGSize screenSize = UIScreen.getMainScreen().getBounds().size();
		double screenWidth = screenSize.width();

		final CGSize adSize = adview.getBounds().size();
		double adWidth = adSize.width();
		double adHeight = adSize.height();

		float bannerWidth = (float) screenWidth;
		float bannerHeight = (float) (bannerWidth / adWidth * adHeight);

		adview.setFrame(new CGRect((screenWidth / 2) - adWidth / 2, 0,
				bannerWidth, bannerHeight));
	}

	public void initializeAds() {
		if (!adsInitialized) {

			adsInitialized = true;

			adview = new GADBannerView(GADAdSize.banner());
			adview.setAdUnitID(APP_ID); // put your secret key here
			adview.setRootViewController(iosApplication.getUIViewController());

			iosApplication.getUIViewController().getView().addSubview(adview);

			final GADRequest request = GADRequest.create();

			adview.setDelegate(new GADBannerViewDelegateAdapter() {
				@Override
				public void didReceiveAd(GADBannerView view) {
					super.didReceiveAd(view);
				}

				@Override
				public void didFailToReceiveAd(GADBannerView view,
						GADRequestError error) {
					super.didFailToReceiveAd(view, error);
				}
			});

			adview.loadRequest(request);
		}
	}

	@Override
	public void showAds() {
		initializeAds();

		final CGSize screenSize = UIScreen.getMainScreen().getBounds().size();
		double screenWidth = screenSize.width();

		final CGSize adSize = adview.getBounds().size();
		double adWidth = adSize.width();
		double adHeight = adSize.height();

		float bannerWidth = (float) screenWidth;
		float bannerHeight = (float) (bannerWidth / adWidth * adHeight);

		adview.setFrame(new CGRect((screenWidth / 2) - adWidth / 2, 0,
				bannerWidth, bannerHeight));

	}

	@Override
	public void destroyAds() {
		// TODO Auto-generated method stub
		
	}
}