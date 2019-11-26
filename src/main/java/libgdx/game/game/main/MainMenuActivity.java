package libgdx.game.game.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.simapps.memorygame.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import db.StoreManagement;
import util.ActivityUtil;
import util.AppRater;
import util.DifficultyUtil;
import util.Utils;

public class MainMenuActivity extends Activity {

	private static MainMenuActivity instance;

	private StoreManagement storeManagement;

	public MainMenuActivity() {
		instance = this;
	}

	public static Activity getContext() {
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainmenu);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		storeManagement = new StoreManagement(this);
		setButtonListener();
		initWithFont();
		initRateApp();
		initDiscoveredItems();
		initDifficultiesDropDown();
		initSoundImgView();
		initHighScores();

		initMainTitleAndColors();
	}

	private void initWithFont() {
		Utils.initViewWithFont(this, R.id.newGameButton, R.id.titleString, R.id.difficultyBtn, R.id.subTitleString);
	}

	private void initHighScores() {
		final Dialog popup = new Dialog(this);
		popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setCancelable(true);
		popup.setContentView(R.layout.high_scores_popup);

		ImageView highScoreBtn = (ImageView) findViewById(R.id.highScoresImg);
		highScoreBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ActivityUtil.playBtnSound(getContext());
				refreshHighScores(popup);
				TextView title = (TextView) popup.findViewById(R.id.highScoresTitle);
				title.setTypeface(Utils.getTypeFace(getContext()));
				popup.show();
			}
		});
	}

	private void refreshHighScores(final Dialog popup) {
		LinearLayout statsLayout = (LinearLayout) popup.findViewById(R.id.highScoresContainer);
		statsLayout.removeAllViews();
		Map<String, Integer> highScores = DifficultyUtil.getDifficultyNameAndScores(this);
		Typeface typeFace = Utils.getTypeFace(getContext());
		for (Entry<String, Integer> elem : highScores.entrySet()) {
			Button highScoreButton = (Button) getLayoutInflater().inflate(R.layout.btntemplate, null);
			highScoreButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimension(R.dimen.mainMenuBtnTextSize));
			highScoreButton.setBackgroundResource(R.layout.mainmenubtn);
			highScoreButton.setTypeface(typeFace);
			Integer score = elem.getValue();
			String scoreString = score < 0 ? "-" : score + "";
			scoreString = Utils.fontString(scoreString, false, Utils.VERY_LIGHT_GREEN);
			String scoreLabel = Utils.fontString(elem.getKey() + ": ", true, Utils.WHITE);
			highScoreButton.setText(Html.fromHtml(scoreLabel + scoreString));
			statsLayout.addView(highScoreButton);
		}
	}

	private void initMainTitleAndColors() {
		Resources resources = getResources();
		String[] titleAndColors = resources.getStringArray(R.array.title_parts_and_colors);
		String title = "";
		for (int i = 0; i < titleAndColors.length / 2; i++) {
			title = title + Utils.fontString(titleAndColors[i * 2], false, titleAndColors[i * 2 + 1]);
		}
		TextView titleTextV = (TextView) findViewById(R.id.titleString);
		titleTextV.setText(Html.fromHtml(title));
	}

	private void initSoundImgView() {
		refreshSoundImgView();

		ImageView soundImgV = (ImageView) findViewById(R.id.soundImg);
		soundImgV.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean soundValueToChange = false;
				if (storeManagement.isSoundOn()) {
					soundValueToChange = false;
				} else {
					soundValueToChange = true;
				}
				storeManagement.updateSoundOn(soundValueToChange);
				refreshSoundImgView();
			}
		});
	}

	private void refreshSoundImgView() {
		ImageView soundImgV = (ImageView) findViewById(R.id.soundImg);
		if (storeManagement.isSoundOn()) {
			soundImgV.setBackgroundResource(R.drawable.soundon);
		} else {
			soundImgV.setBackgroundResource(R.drawable.soundoff);
		}
	}

	private void initRateApp() {
		AppRater.app_launched(this, getResources().getString(R.string.app_name), getPackageName());
	}

	private void initDiscoveredItems() {

		final ImageView discItemsBtn = (ImageView) findViewById(R.id.discoveredItemsImg);
		discItemsBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ActivityUtil.playBtnSound(getContext());
				Intent intent = new Intent(getContext(), ItemsDiscoveredActivity.class);
				startActivity(intent);
			}
		});

	}

	private void initDifficultiesDropDown() {
		final Dialog difficultiesPopup = new Dialog(this);
		difficultiesPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		difficultiesPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		difficultiesPopup.setCancelable(true);
		difficultiesPopup.setContentView(R.layout.difficulty_popup);
		Button difficultyBtn = refreshDifficultyBtnText();
		difficultyBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ActivityUtil.playBtnSound(getContext());
				createRadioButtonPopup(difficultiesPopup);
				difficultiesPopup.show();
			}
		});
	}

	private void createRadioButtonPopup(final Dialog difficultiesPopup) {
		TextView title = (TextView) difficultiesPopup.findViewById(R.id.diffTitle);
		title.setTypeface(Utils.getTypeFace(this));
		List<String> difficultyNames = new ArrayList<String>(DifficultyUtil.getCodeAndDifficultyName(this).values());
		RadioGroup difficulties = (RadioGroup) difficultiesPopup.findViewById(R.id.difficultiesGroup);
		difficulties.removeAllViews();
		final int preffDiff = storeManagement.getPreferredDifficulty();
		for (final String string : difficultyNames) {
			RadioButton radioButton = new RadioButton(getContext());
			radioButton.setText(string);
			radioButton.setTextColor(Color.BLACK);
			radioButton.setTypeface(Utils.getTypeFace(getContext()));
			if (DifficultyUtil.getDifficultyIndexForDifficultyName(string, getContext()) == preffDiff) {
				radioButton.setChecked(true);
			}
			radioButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActivityUtil.playBtnSound(getContext());
					int difficultyCodeForDifficultyName = DifficultyUtil.getDifficultyIndexForDifficultyName(string,
							getContext());
					if (difficultyCodeForDifficultyName != preffDiff) {
						storeManagement.updatePreferredDiff(difficultyCodeForDifficultyName);
						refreshDifficultyBtnText();
					}
					difficultiesPopup.dismiss();
				}
			});
			difficulties.addView(radioButton);
		}
	}

	private Button refreshDifficultyBtnText() {
		int preffDiff = storeManagement.getPreferredDifficulty();
		Button difficultyBtn = (Button) findViewById(R.id.difficultyBtn);
		String diffName = DifficultyUtil.getDifficultyNameForDifficultyIndex(preffDiff, getContext());
		String downArrow = "&#9660";
		diffName = diffName + " <big><font color=#1975D1 >" + downArrow + "</font></big>";
		difficultyBtn.setText(Html.fromHtml(diffName));
		return difficultyBtn;
	}

	private void setButtonListener() {
		final Button newGameButton = (Button) findViewById(R.id.newGameButton);
		newGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ActivityUtil.playBtnSound(getContext());
				Intent intent = new Intent(getContext(), MainActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

}
