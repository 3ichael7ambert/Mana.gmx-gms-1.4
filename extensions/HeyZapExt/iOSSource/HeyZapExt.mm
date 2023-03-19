//
//  HeyZap Extension for GameMaker: Studio
//  ver 1.0
//
//  Released by Vitaliy Sidorov on 14/01/2016
//  Copyright SilenGames, 2016.
//
//  For support please e-mail at contact@silengames.com
//

#import "HeyZapExt.h"
#import <HeyzapAds/HeyzapAds.h>
#import <UIKit/UIKit.h>

const int EVENT_OTHER_SOCIAL = 70;
extern int CreateDsMap( int _num, ... );
extern void CreateAsynEventWithDSMap(int dsmapindex, int event_index);
extern UIViewController *g_controller;
extern UIView *g_glView;
extern int g_DeviceWidth;
extern int g_DeviceHeight;
double g_BannerScale = 1;
UIView *Adbanner;
NSString *g_AppId;
double g_BannerWidth = 0;
double g_BannerHeight = 0;
double preBannerPos = 2;
double isOfferWallAutoClose = 0;

@implementation HeyZapExt

- (void) HeyZap_Init:(char *)_app_id Arg2:(double)_istest
{
	g_AppId  = [NSString stringWithCString:_app_id encoding:NSUTF8StringEncoding];
	if (_istest == 2) {
		[HeyzapAds startWithPublisherID: g_AppId andOptions: HZAdOptionsDisableAutoPrefetching];
	} else if (_istest == 1) {
		[HeyzapAds startWithPublisherID: g_AppId];
		[HeyzapAds presentMediationDebugViewController];
	} else {
		[HeyzapAds startWithPublisherID: g_AppId];
	}
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didReceiveAdNotificationStatic:) name:HZMediationDidReceiveAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didReceiveAdNotificationReward:) name:HZMediationDidReceiveAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToReceiveAdNotificationStatic:) name:HZMediationDidFailToReceiveAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToReceiveAdNotificationReward:) name:HZMediationDidFailToReceiveAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didShowAdNotificationStatic:) name:HZMediationDidShowAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didShowAdNotificationReward:) name:HZMediationDidShowAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToShowAdNotificationStatic:) name:HZMediationDidFailToShowAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToShowAdNotificationReward:) name:HZMediationDidFailToShowAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didClickAdNotificationStatic:) name:HZMediationDidClickAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didClickAdNotificationReward:) name:HZMediationDidClickAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didHideAdNotificationStatic:) name:HZMediationDidHideAdNotification object:[HZInterstitialAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didHideAdNotificationReward:) name:HZMediationDidHideAdNotification object:[HZIncentivizedAd class]];
	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didCompleteIncentivizedAdNotification:) name:HZMediationDidCompleteIncentivizedAdNotification object:[HZIncentivizedAd class]];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didFailToCompleteIncentivizedAdNotification:) name:HZMediationDidFailToCompleteIncentivizedAdNotification object:[HZIncentivizedAd class]];

}

- (void) HeyZap_AddBanner:(double)_pos
{
	HZBannerPosition BannerVPos;
	if (_pos == 1) {
		BannerVPos = HZBannerPositionTop;
	} else {
		BannerVPos = HZBannerPositionBottom;
	}
	if (preBannerPos != _pos) {
		[self HeyZap_RemoveBanner];
	}
	if (Adbanner != nil) {
		[[HZBannerAdController sharedInstance].bannerView setHidden: NO];
	} else {
		HZBannerAdOptions *bannerOpts = [[HZBannerAdOptions alloc] init];
		[[HZBannerAdController sharedInstance] placeBannerAtPosition:BannerVPos options:bannerOpts success:^(UIView *banner) {
			Adbanner = banner;
			[self sendCallbacks:(char *)"heyzap_banner_loaded" Arg2:1];
			NSLog(@"Banner was Shown");
			g_BannerScale = g_DeviceWidth/g_glView.bounds.size.width;
			g_BannerWidth = Adbanner.bounds.size.width*g_BannerScale;
			g_BannerHeight = Adbanner.bounds.size.height*g_BannerScale;
		} failure:^(NSError *error) {
			[self sendCallbacks:(char *)"heyzap_banner_loaded" Arg2:0];
			NSLog(@"Banner Error = %@",error);
		}];
	}
	preBannerPos = _pos;
}

- (void) HeyZap_RemoveBanner
{
	if (Adbanner != nil) {
		[Adbanner removeFromSuperview];
		Adbanner = nil;
		[[HZBannerAdController sharedInstance] destroyBanner];
	}
}

- (void) HeyZap_HideBanner
{
	if (Adbanner != nil) {
		[[HZBannerAdController sharedInstance].bannerView setHidden: YES];
	}
}

-(double) HeyZap_BannerGetWidth
{
	return g_BannerWidth;
}

-(double) HeyZap_BannerGetHeight
{
	return g_BannerHeight;
}

-(void) HeyZap_OfferWallAutoClose:(double)_arg
{
	if (_arg == 1) {
		isOfferWallAutoClose = 1;
	} else {
		isOfferWallAutoClose = 0;
	}
}

- (void) HeyZap_ShowOfferWall
{
	FYBOfferWallViewController *offerWallViewController = [[FYBOfferWallViewController alloc] init];
	if (isOfferWallAutoClose == 1) {
		offerWallViewController.shouldDismissOnRedirect = YES;
	}
	[offerWallViewController presentFromViewController:g_controller animated:YES completion:^{
		[self sendCallbacks:(char *)"heyzap_offer_loaded" Arg2:1];
	} dismiss:^(NSError *error) {
		if (error != nil) {
			[self sendCallbacks:(char *)"heyzap_offer_loaded" Arg2:0];
			//[self HeyZap_GMBugFix];
		} else {
			[self sendCallbacks:(char *)"heyzap_offer_closed" Arg2:0];
			//[self HeyZap_GMBugFix];
			//[self HeyZap_OfferWallCheckReward];
		}
	}];
}

- (void) HeyZap_OfferWallCheckReward
{
	FYBVirtualCurrencyClient *virtualCurrencyClient = [HeyzapAds virtualCurrencyClient];
	virtualCurrencyClient.delegate = self;
	[virtualCurrencyClient requestDeltaOfCoins];
}

- (void) virtualCurrencyClient:(FYBVirtualCurrencyClient *)client didReceiveResponse:(FYBVirtualCurrencyResponse *)response {
    NSLog(@"Received %.2f %@ (currency id: %@)", response.deltaOfCoins, response.currencyName, response.currencyId);
	[self sendCallbacks:(char *)"heyzap_offer_reward" Arg2:response.deltaOfCoins];
}

- (void) virtualCurrencyClient:(FYBVirtualCurrencyClient *)client didFailWithError:(NSError *)error {
    NSLog(@"FYBVirtualCurrencyClient error: %@", error);
	[self sendCallbacks:(char *)"heyzap_offer_reward_error" Arg2:0];
}

- (void) HeyZap_ShowInterstitial
{
	[HZInterstitialAd show];
}

- (void) HeyZap_LoadInterstitial
{
	[HZInterstitialAd fetch];
}

- (double) HeyZap_InterstitialStatus
{
	if ([HZInterstitialAd isAvailable]) {
		return 1;
	} else {
		return 0;
	}
}

- (void) HeyZap_ShowReward
{
	[HZIncentivizedAd show];
}

- (void) HeyZap_LoadReward
{
	[HZIncentivizedAd  fetch];
}

- (double) HeyZap_RewardStatus
{
	if ([HZIncentivizedAd isAvailable]) {
		return 1;
	} else {
		return 0;
	}
}

- (void) HeyZap_GMBugFix
{
    CGRect oldFrame = g_glView.frame;
    CGRect newFrame = oldFrame;
    newFrame.size.width = 0;
    newFrame.size.height = 0;
    [g_glView setFrame:newFrame];
    [g_glView setFrame:oldFrame];
	NSLog(@"----------- GM PAUSE BUG FIXED ----------");
}

- (void) sendCallbacks:(char *)type Arg2:(double)value
{
	int dsMapIndex = CreateDsMap(2,
		"type", 0.0, type,
		"value", value, (void*)NULL);
	CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
}


- (void) didReceiveAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_loaded" Arg2:1];
}
- (void) didReceiveAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_loaded" Arg2:1];
}


- (void) didFailToReceiveAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_loaded" Arg2:0];
}
- (void) didFailToReceiveAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_loaded" Arg2:0];
}


- (void) didShowAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_shown" Arg2:1];
}
- (void) didShowAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_shown" Arg2:1];
}


- (void) didFailToShowAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_shown" Arg2:0];
}
- (void) didFailToShowAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_shown" Arg2:0];
}


- (void) didClickAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_clicked" Arg2:1];
}
- (void) didClickAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_clicked" Arg2:1];
}


- (void) didHideAdNotificationStatic: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_ad_hidden" Arg2:1];
	//[self HeyZap_GMBugFix];
}
- (void) didHideAdNotificationReward: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward_hidden" Arg2:1];
	//[self HeyZap_GMBugFix];
}


- (void) didCompleteIncentivizedAdNotification: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward" Arg2:1];
}
- (void) didFailToCompleteIncentivizedAdNotification: (NSNotification *)notification {
	[self sendCallbacks:(char *)"heyzap_reward" Arg2:0];
}

- (void) dealloc
{
    [super dealloc];
}

@end
