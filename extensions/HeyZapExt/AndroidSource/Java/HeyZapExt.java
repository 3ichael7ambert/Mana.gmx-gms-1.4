//
//  HeyZap Extension for GameMaker: Studio
//  ver 1.0
//
//  Released by Vitaliy Sidorov on 14/01/2016
//  Copyright SilenGames, 2016.
//
//  For support please e-mail at contact@silengames.com
//

package ${YYAndroidPackageName};

import android.util.Log;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.String;
import java.lang.Object;

import com.heyzap.sdk.ads.HeyzapAds;
import com.heyzap.sdk.ads.InterstitialAd;
import com.heyzap.sdk.ads.IncentivizedAd;

import com.heyzap.sdk.ads.BannerAd;
import com.heyzap.sdk.ads.BannerAdView;
import com.heyzap.sdk.ads.HeyzapAds.BannerListener;
import com.heyzap.sdk.ads.HeyzapAds.BannerOptions;
import com.heyzap.sdk.ads.HeyzapAds.CreativeSize;

import com.heyzap.sdk.ads.HeyzapAds.StaticBannerListener;
import com.heyzap.sdk.ads.HeyzapAds.BannerError;
import com.heyzap.sdk.ads.HeyzapAds.OnStatusListener;
import com.heyzap.sdk.ads.HeyzapAds.OnIncentiveResultListener;

import com.fyber.Fyber;
import com.fyber.utils.FyberLogger;

import com.fyber.ads.ofw.OfferWallActivity;

import com.fyber.currency.VirtualCurrencyErrorResponse;
import com.fyber.currency.VirtualCurrencyResponse;

import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.requesters.OfferWallRequester;
import com.fyber.requesters.VirtualCurrencyCallback;
import com.fyber.requesters.VirtualCurrencyRequester;

import com.fyber.ads.AdFormat;

import android.widget.TextView;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Gravity;

import ${YYAndroidPackageName}.RunnerActivity;
import ${YYAndroidPackageName}.R;
import com.yoyogames.runner.RunnerJNILib;


public class HeyZapExt {

	private static final int EVENT_OTHER_SOCIAL = 70;
	private String app_id;
	private int BannerVPos;
	private int BannerNew;
	private static final int OFFERWALL_REQUEST_CODE = 9999;
	private OfferWallRequester offerWallObj = null;
	private VirtualCurrencyRequester virtualCurrencyObj = null;
	private FrameLayout bannerRootView = null;
	private FrameLayout bannerWrapper = null;
	private BannerAdView bannerAdView = null;
	private double isOfferWallAutoClose = 0;
	
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	*/

	public void HeyZap_Init(String _app_id, double istest) {
		app_id = _app_id;
		Log.i("yoyo","Calling HeyZap with "+_app_id);
		if (istest == 1) {
			HeyzapAds.start(app_id, RunnerActivity.CurrentActivity);
			HeyzapAds.startTestActivity(RunnerActivity.CurrentActivity);
			Log.i("yoyo","HeyZap Test Mode Enabled");
		} else if (istest == 2) {
			HeyzapAds.start(app_id, RunnerActivity.CurrentActivity, HeyzapAds.DISABLE_AUTOMATIC_FETCH);
			Log.i("yoyo","HeyZap Auto Fetch Disabled");
		} else {
			HeyzapAds.start(app_id, RunnerActivity.CurrentActivity);
		}						
		setupCallbacks();
	}
	
	public void HeyZap_AddBanner(double _pos) {
		BannerVPos = Gravity.BOTTOM;
		BannerNew = 0;
		if (_pos != 0) {
			BannerVPos = Gravity.TOP;
		}
		if (bannerAdView == null) {
			BannerNew = 1;
			bannerAdView = new BannerAdView(RunnerActivity.CurrentActivity);
			BannerOptions bannerOptions = bannerAdView.getBannerOptions();
			bannerOptions.setFacebookBannerSize(CreativeSize.SMART_BANNER);
			bannerOptions.setAdmobBannerSize(CreativeSize.SMART_BANNER);
			bannerAdView.setBannerListener(new BannerListener() {
				//@Override
				public void onAdClicked(BannerAdView b) {
					sendCallbacks("heyzap_banner_clicked", 1);
					Log.i("yoyo","HeyZap Banner CLICK!");
				}
				//@Override
				public void onAdLoaded(BannerAdView b) {
					sendCallbacks("heyzap_banner_loaded", 1);
					Log.i("yoyo","HeyZap Banner is LOADED!");
				}
				//@Override
				public void onAdError(BannerAdView b, BannerError bannerError) {
					sendCallbacks("heyzap_banner_loaded", 0);
					HeyZap_RemoveBanner();
					Log.i("yoyo","HeyZap Banner is NOT LOADED!");
				}
			});
		}
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				if (bannerWrapper == null) {
					bannerRootView = (FrameLayout) RunnerActivity.CurrentActivity.findViewById(android.R.id.content);
					bannerWrapper = new FrameLayout(RunnerActivity.CurrentActivity);
					bannerWrapper.setLayoutParams(
						new FrameLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT,
								BannerVPos
						)
					);				
					bannerRootView.addView(bannerWrapper);
				} else {
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bannerWrapper.getLayoutParams();
					params.gravity = BannerVPos;
					bannerWrapper.setLayoutParams(params);
					//bannerWrapper.requestLayout();
					if (bannerWrapper.getVisibility() == View.INVISIBLE) {
						bannerWrapper.setVisibility(View.VISIBLE);
						Log.i("yoyo","HeyZap Banner is VISIBLE");
					}
				}
				if (BannerNew == 1) {
					ViewGroup vg = (ViewGroup) bannerWrapper;
					vg.addView(bannerAdView);
					bannerAdView.load();
					//Log.i("yoyo","xxxxxxxx"+bannerWrapper.getChildCount());
				}
			}
		});
	}

	public void HeyZap_HideBanner() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				if (bannerAdView != null && bannerWrapper != null) {
					bannerWrapper.setVisibility(View.INVISIBLE);
					Log.i("yoyo","HeyZap Banner is INVISIBLE");
				} else {
					Log.i("yoyo","HeyZap Banner is not exist!");
				}
			}
		});			
	}
	
	public void HeyZap_RemoveBanner() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				if (bannerAdView != null && bannerWrapper != null) {
					bannerAdView.destroy();
					bannerAdView = null;
					ViewGroup vg = (ViewGroup) bannerWrapper;
					if (vg != null) {
						vg.removeAllViews();
					} else {
						Log.i("yoyo","HeyZap Banner lost view!");
					}
				} else {
					Log.i("yoyo","HeyZap Banner has already removed!");
				}
			}
		});
	}
	
	public double HeyZap_BannerGetWidth() {
		Log.i("yoyo","Banner Width - "+bannerAdView.getMeasuredWidth());
		return bannerAdView.getMeasuredWidth();
	}
	
	public double HeyZap_BannerGetHeight() {
		Log.i("yoyo","Banner Height - "+bannerAdView.getMeasuredHeight());
		return bannerAdView.getMeasuredHeight();
	}
	
	public void HeyZap_LoadInterstitial() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				InterstitialAd.fetch();
			}
		});
    }

    public void HeyZap_ShowInterstitial() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {		
				InterstitialAd.display(RunnerActivity.CurrentActivity);
			}
		});
    }

	public double HeyZap_InterstitialStatus() {
		if (InterstitialAd.isAvailable()) {
			return 1;
		} else {
			return 0;
		}
    }

    public void HeyZap_LoadReward() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				IncentivizedAd.fetch();
			}
		});
    }

    public void HeyZap_ShowReward() {
		RunnerActivity.ViewHandler.post( new Runnable() {
			public void run() {
				IncentivizedAd.display(RunnerActivity.CurrentActivity);
			}
		});
    }
	
	public double HeyZap_RewardStatus() {
		if (IncentivizedAd.isAvailable()) {
			return 1;
		} else {
			return 0;
		}
    }
	
	RequestCallback requestCallback = new RequestCallback() {
		@Override
		public void onRequestError(RequestError requestError) {
			Log.i("yoyo","Offer Wall - Something went wrong with the request: " + requestError.getDescription());
			sendCallbacks("heyzap_offer_loaded", 0);
		}
		@Override
		public void onAdAvailable(Intent intent) {
			Log.i("yoyo","Offer Wall is available");
			RunnerActivity.CurrentActivity.startActivityForResult(intent, OFFERWALL_REQUEST_CODE);
			sendCallbacks("heyzap_offer_loaded", 1);
		}
		@Override
		public void onAdNotAvailable(AdFormat adFormat) {
			Log.i("yoyo","Offer Wall is not available");
			sendCallbacks("heyzap_offer_loaded", 0);
		}
	};
		
	VirtualCurrencyCallback virtualCurrencyCallback = new VirtualCurrencyCallback() {
		@Override
		public void onRequestError(RequestError requestError) {
			Log.i("yoyo","VCS request error: " + requestError.getDescription());
			sendCallbacks("heyzap_offer_reward_error", 0);
		}
		@Override
		public void onSuccess(VirtualCurrencyResponse virtualCurrencyResponse) {
			double deltaOfCoins = virtualCurrencyResponse.getDeltaOfCoins();
			Log.i("yoyo","VCS reward received: " + deltaOfCoins);
			sendCallbacks("heyzap_offer_reward", deltaOfCoins);
		}
		@Override
		public void onError(VirtualCurrencyErrorResponse virtualCurrencyErrorResponse) {
			Log.i("yoyo","VCS error received - " + virtualCurrencyErrorResponse.getErrorMessage());
			sendCallbacks("heyzap_offer_reward_error", 0);
		}
	};
	
	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("yoyo","AAAAAAAAAAAAAAAAAACCCCCCCCCCCCCCCC");
		if (resultCode == RESULT_OK) {
			Log.i("yoyo","CCCCCCOOOOODEEEE OOOKKKKK");
			if (requestCode == OFFERWALL_REQUEST_CODE) {
				Log.i("yoyo","REEEQUESSSTTT OK");
				sendCallbacks("heyzap_offer_closed", 0);
			}
		}
	};
	*/
	
	public void HeyZap_GMBugFix() {
		Log.i("yoyo","Warning: HeyZap_GMBugFix method should be used for iOS only");
	}

	public void HeyZap_OfferWallAutoClose(double _arg) {
		if (_arg == 1) {
			isOfferWallAutoClose = 1;
		} else {
			isOfferWallAutoClose = 0;
		}
    }
	
	public void HeyZap_ShowOfferWall() {
		offerWallObj = OfferWallRequester.create(requestCallback);
		if (isOfferWallAutoClose == 1) {
			offerWallObj.closeOnRedirect(true);
		}
		offerWallObj.request(RunnerActivity.CurrentActivity);
    }
	
	public void HeyZap_OfferWallCheckReward() {
		virtualCurrencyObj = VirtualCurrencyRequester.create(virtualCurrencyCallback);
		virtualCurrencyObj.request(RunnerActivity.CurrentActivity);
		Log.i("yoyo","Checking reward of Offer Wall...");
	}
	protected void sendCallbacks(String _type, double _value) {
		int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
		RunnerJNILib.DsMapAddString( dsMapIndex, "type", _type);
		RunnerJNILib.DsMapAddDouble( dsMapIndex, "value", _value);
		RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex,EVENT_OTHER_SOCIAL);
	}
	
	protected void setupCallbacks() {
					
		InterstitialAd.setOnStatusListener(new HeyzapAds.OnStatusListener() {
		
            @Override
            public void onAvailable(String tag) {
                Log.i("yoyo","HeyZap Ad Loaded!");
				sendCallbacks("heyzap_ad_loaded", 1);
            }
			
            @Override
            public void onShow(String tag) {
                Log.i("yoyo","HeyZap Ad Shown!");
				sendCallbacks("heyzap_ad_shown", 1);
            }
			
            @Override
            public void onClick(String tag) {
                Log.i("yoyo","HeyZap Ad Clicked!");
				sendCallbacks("heyzap_ad_clicked", 1);
            }

            @Override
            public void onHide(String tag) {
                Log.i("yoyo","HeyZap Ad Hidden!");
				sendCallbacks("heyzap_ad_hidden", 1);
            }

            @Override
            public void onFailedToFetch(String tag) {
                Log.i("yoyo","HeyZap Ad Failed To Load!");
				sendCallbacks("heyzap_ad_loaded", 0);
            }

            @Override
            public void onFailedToShow(String tag) {
                Log.i("yoyo","HeyZap Ad Failed To Show!");
				sendCallbacks("heyzap_ad_shown", 0);
            }
			
			@Override
			public void onAudioStarted() {
				Log.i("yoyo","HeyZap Ad Audio Started!");
			}
		 
			@Override
			public void onAudioFinished() {
				Log.i("yoyo","HeyZap Ad Audio Finished!");
			}

        });
		
		IncentivizedAd.setOnStatusListener(new HeyzapAds.OnStatusListener() {
		
            @Override
            public void onAvailable(String tag) {
                Log.i("yoyo","HeyZap Reward Loaded!");
				sendCallbacks("heyzap_reward_loaded", 1);
            }
			
            @Override
            public void onShow(String tag) {
                Log.i("yoyo","HeyZap Reward Shown!");
				sendCallbacks("heyzap_reward_shown", 1);
            }
			
            @Override
            public void onClick(String tag) {
                Log.i("yoyo","HeyZap Reward Clicked!");
				sendCallbacks("heyzap_reward_clicked", 1);
            }

            @Override
            public void onHide(String tag) {
                Log.i("yoyo","HeyZap Reward Hidden!");
				sendCallbacks("heyzap_reward_hidden", 1);
            }

            @Override
            public void onFailedToFetch(String tag) {
                Log.i("yoyo","HeyZap Reward Failed To Load!");
				sendCallbacks("heyzap_reward_loaded", 0);
            }

            @Override
            public void onFailedToShow(String tag) {
                Log.i("yoyo","HeyZap Reward Failed To Show!");
				sendCallbacks("heyzap_reward_shown", 0);
            }

			@Override
			public void onAudioStarted() {
				Log.i("yoyo","HeyZap Reward Audio Started!");
			}
		 
			@Override
			public void onAudioFinished() {
				Log.i("yoyo","HeyZap Reward Audio Finished!");
			}
        });

        HeyzapAds.OnIncentiveResultListener incentiveResultListener = new HeyzapAds.OnIncentiveResultListener() {

            @Override
            public void onComplete(String tag) {
                Log.i("yoyo","HeyZap Give Reward!");
				sendCallbacks("heyzap_reward", 1);
            }

            @Override
            public void onIncomplete(String tag) {
				Log.i("yoyo","HeyZap No Reward!");
				sendCallbacks("heyzap_reward", 0);
            }
        };

        IncentivizedAd.setOnIncentiveResultListener(incentiveResultListener);
    }

}

