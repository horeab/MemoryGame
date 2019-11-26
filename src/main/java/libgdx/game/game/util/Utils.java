package libgdx.game.game.util;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class Utils {

	public static final String WHITE = "#FFFFFF";
	public static final String RED = "#FF0000";
	public static final String GREEN = "#006600";
	public static final String VERY_LIGHT_GREEN = "#EBF5EB";

	public static void keepScreenOn(Activity activity) {
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public static String getScoreColor(int score) {
		String color = "#000000";
		if (score > 0) {
			color = GREEN;
		} else if (score < 0) {
			color = RED;

		}
		return color;
	}

	public static String fontString(String text, boolean small, String color) {
		String colorVal = "";
		if (color != null) {
			colorVal = "color=" + color;
		}
		String value = "<font " + colorVal + " >" + text + "</font>";
		if (small) {
			value = "<small>" + value + "</small>";
		}
		return value;
	}

	public static void initViewWithFont(Activity context, Integer... viewsIds) {
		int[] ids = convertToArray(viewsIds);
		initViewWithFont(context, ids);
	}

	private static int[] convertToArray(Integer... viewsIds) {
		int[] ids = new int[viewsIds.length];
		int i = 0;
		for (int viewId : viewsIds) {
			ids[i] = viewId;
			i++;
		}
		return ids;
	}

	private static void initViewWithFont(Activity context, int[] viewsIds) {
		Typeface font = getTypeFace(context);
		for (Integer viewId : viewsIds) {
			View view = context.findViewById(viewId);
			if (view instanceof Button) {
				((Button) view).setTypeface(font);
			} else if (view instanceof TextView) {
				((TextView) view).setTypeface(font);
			}
		}
	}

	public static Typeface getTypeFace(Activity context) {
		Typeface font = Typeface.createFromAsset(context.getAssets(), "customfont.ttf");
		return font;
	}

	public static void showPopupAd(boolean showAds, final InterstitialAd interstitialAd) {
		if (showAds && interstitialAd.isLoaded()) {
			interstitialAd.show();
			interstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdClosed() {
					AdRequest adRequest = new AdRequest.Builder().build();
					interstitialAd.loadAd(adRequest);
				}
			});
		}
	}
}
