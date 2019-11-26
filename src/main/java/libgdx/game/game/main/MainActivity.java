package libgdx.game.game.main;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.simapps.memorygame.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import db.StoreManagement;
import model.CurrentGame;
import util.ActivityUtil;
import util.DifficultyUtil;
import util.GameLogic;
import util.Utils;

public class MainActivity extends Activity {

	private static final int SHOW_IMAGES_FOR_X_SECS = 2;

	private static final String CURRENT_GAME = "CURRENT_GAME";

	private static MainActivity instance;
	private InterstitialAd interstitialAd;

	public MainActivity() {
		instance = this;
	}

	public static MainActivity getContext() {
		return instance;
	}

	private boolean enableImageClick = false;

	public boolean isEnableImageClick() {
		return enableImageClick;
	}

	public void setEnableImageClick(boolean enableImageClick) {
		this.enableImageClick = enableImageClick;
	}

	private CurrentGame currentGame;

	public CurrentGame getCurrentGame() {
		return currentGame;
	}

	private ActivityUtil activityUtil;
	private GameLogic gameUtil;

	private boolean alreadyInitialized = false;

	private int selDifficulty;

	public int getSelDifficulty() {
		return selDifficulty;
	}

	private DifficultyUtil difficultyUtil;

	private StoreManagement storeManagement;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUp();
	}

	private void setUp() {

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		storeManagement = new StoreManagement(this);
		selDifficulty = storeManagement.getPreferredDifficulty();

		currentGame = (CurrentGame) getIntent().getSerializableExtra(CURRENT_GAME);
		if (currentGame == null) {
			currentGame = new CurrentGame(0, selDifficulty, this);
		}

		difficultyUtil = new DifficultyUtil(this);
		activityUtil = new ActivityUtil(this);
		gameUtil = new GameLogic();

		activityUtil.createRows(getRows());
		activityUtil.addImagesToRows(getRows(), getCols());
		activityUtil.refreshImageViews(getRows(), getCols(), currentGame.getLevelMatrix(), true);
		activityUtil.onClickImageViews(currentGame);
		processGameTypes();
		processAds();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!alreadyInitialized) {
			super.onWindowFocusChanged(hasFocus);
			gameUtil.showAllImages(currentGame.getLevelMatrix());
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					gameUtil.hideAllImages(currentGame.getLevelMatrix());
					activityUtil.refreshImageViews(getRows(), getCols(), currentGame.getLevelMatrix(), true);
					setEnableImageClick(true);
					alreadyInitialized = true;
				}
			}, SHOW_IMAGES_FOR_X_SECS * 1000);

			initTextViews();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean ignoreBackGround = false;
		if (!alreadyInitialized) {
			ignoreBackGround = true;
		}
		activityUtil.refreshImageViews(getRows(), getCols(), currentGame.getLevelMatrix(), ignoreBackGround);
	}

	private void initTextViews() {
		Utils.initViewWithFont(this, R.id.scoreLabel, R.id.stageScoreLabel, R.id.commentaryLabel);
		refreshTextViews(null);
		refreshChances();
	}

	private void processAds() {
		AdView adView = (AdView) findViewById(R.id.adView);
		if (!getResources().getBoolean(R.bool.show_ads)) {
			adView.setVisibility(View.GONE);
		} else {
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
			interstitialAd = new InterstitialAd(this);
			interstitialAd.setAdUnitId(getResources().getString(R.string.admob_inter_id));
			interstitialAd.loadAd(adRequest);
		}
	}

	public void refreshTextViews(String itemValue) {
		Map<String, String> scores = new HashMap<String, String>();
		scores.put(Utils.GREEN, currentGame.getTotalScoreFor() + "");
		scores.put(Utils.RED, currentGame.getTotalScoreAgainst() + "");

		String scoreFor = Utils.fontString(currentGame.getTotalScoreFor() + "", false, Utils.GREEN);
		String scoreAgainst = Utils.fontString(currentGame.getTotalScoreAgainst() + "", false, Utils.RED);
		String scoreLabel = scoreFor + " - " + scoreAgainst;
		TextView scoreTextView = (TextView) findViewById(R.id.scoreLabel);

		String stageScoreFor = Utils.fontString(currentGame.getStageScoreFor() + "", false, Utils.GREEN);
		String stageScoreAgainst = Utils.fontString(currentGame.getStageScoreAgainst() + "", false, Utils.RED);
		String stageScoreLabel = " (" + stageScoreFor + " - " + stageScoreAgainst + ")";
		TextView stageScoreTextView = (TextView) findViewById(R.id.stageScoreLabel);

		if (isChancesGame()) {
			scoreLabel = "Score: " + Integer.valueOf(currentGame.getTotalScoreFor() + currentGame.getStageScoreFor());
			stageScoreLabel = "Level: " + Integer.valueOf(currentGame.getCurrentLevel().getLevelNr() + 1) + "";
		}
		scoreTextView.setText(Html.fromHtml(scoreLabel));
		stageScoreTextView.setText(Html.fromHtml(stageScoreLabel));

		TextView commentaryTextView = (TextView) findViewById(R.id.commentaryLabel);
		if (itemValue != null) {
			commentaryTextView.setText(Html.fromHtml(itemValue));
		} else {
			commentaryTextView.setText("");
		}
	}

	public Dialog levelFinishedPopup(boolean nextLevel, boolean gameOver, int stageScore, final int totalScore) {

		Typeface typeFace = Utils.getTypeFace(this);

		final Dialog popup = new Dialog(this);
		popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setCancelable(false);
		popup.setContentView(R.layout.next_level_popup);

		String diffTitle = difficultyUtil.getSelectedDifficultyName();

		TextView title = (TextView) popup.findViewById(R.id.diffTitle);
		title.setText(diffTitle);
		title.setTypeface(typeFace);

		TextView stageScoreTextV = (TextView) popup.findViewById(R.id.stageScore);
		stageScoreTextV.setTypeface(typeFace);
		String prefix = stageScore > 0 ? "+" : "";
		String stageScoreText = Utils.fontString(prefix + stageScore, false, Utils.getScoreColor(stageScore));
		String stageLabel = Utils.fontString(getResources().getString(R.string.stage_score), true, null);
		stageScoreTextV.setText(Html.fromHtml(stageLabel + " " + stageScoreText));

		TextView totalScoreTextV = (TextView) popup.findViewById(R.id.totalScore);
		totalScoreTextV.setTypeface(typeFace);
		prefix = totalScore > 0 ? "+" : "";
		String totalScoreText = Utils.fontString(prefix + totalScore, false, Utils.getScoreColor(totalScore));
		String totalLabel = Utils.fontString(getResources().getString(R.string.total_score), true, null);
		totalScoreTextV.setText(Html.fromHtml(totalLabel + " " + totalScoreText));

		String message = getResources().getString(R.string.msg_game_over);
		boolean gameOverSuccess = false;
		if (nextLevel) {
			message = getResources().getString(R.string.msg_next_level);
			gameOverSuccess = nextLevelNr() == -1;
			if (gameOverSuccess) {
				nextLevel = false;
				message = getResources().getString(R.string.msg_game_over);
				int highScore = difficultyUtil.getSelectedDifficultyHighScore(this);
				if (totalScore > highScore) {
					message = getResources().getString(R.string.msg_game_over_success_high_score);
					storeManagement.putHighScore(totalScore, selDifficulty);
				}
			}
		}
		String levelFinishedBtnText = getResources().getString(R.string.btn_next);
		if (!nextLevel) {
			levelFinishedBtnText = getResources().getString(R.string.btn_back);
		}
		TextView messageTextV = (TextView) popup.findViewById(R.id.stageFinishedMessage);
		messageTextV.setTypeface(typeFace);
		messageTextV.setText(message);

		Button levelFinishedBtn = (Button) popup.findViewById(R.id.nextButton);
		levelFinishedBtn.setTypeface(typeFace);
		levelFinishedBtn.setText(levelFinishedBtnText);
		final boolean finalGameOver = gameOver;
		final boolean finalGameOverSuccess = gameOverSuccess;
		final boolean finalNextLevel = nextLevel;
		submitScore(popup, finalGameOverSuccess, totalScore);
		levelFinishedBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ActivityUtil.playBtnSound(getContext());
				if (finalGameOver || finalGameOverSuccess) {
					gameOverOrLevelFinished();
				} else if (finalNextLevel) {
					nextStage();
				}
			}

			public void gameOverOrLevelFinished() {
				finish();
			}

			public void nextStage() {
				int forTotalScore = currentGame.getTotalScoreFor();
				int againstTotalScore = currentGame.getTotalScoreAgainst();
				currentGame = new CurrentGame(nextLevelNr(), selDifficulty, getContext());
				currentGame.setTotalScoreFor(forTotalScore);
				currentGame.setTotalScoreAgainst(againstTotalScore);
				if (getResources().getBoolean(R.bool.show_ads) && interstitialAd.isLoaded()
						&& currentGame.getCurrentLevel().getLevelNr() % 2 == 0) {
					interstitialAd.show();
					interstitialAd.setAdListener(new AdListener() {
						@Override
						public void onAdClosed() {
							AdRequest adRequest = new AdRequest.Builder().build();
							interstitialAd.loadAd(adRequest);
							startNextStage();
						}
					});
				} else {
					startNextStage();
				}
			}
		});
		return popup;
	}

	private void startNextStage() {
		Intent intent = getContext().getIntent();
		intent.putExtra(CURRENT_GAME, currentGame);
		startActivity(intent);
		finish();
	}

	private void submitScore(Dialog popup, boolean finalGameOverSuccess, int score) {
		if (finalGameOverSuccess) {
			Button submitBtn = (Button) popup.findViewById(R.id.submitScoreButton);
			submitBtn.setVisibility(View.VISIBLE);
			submitBtn.setTypeface(Utils.getTypeFace(getContext()));
		}
	}

	private int nextLevelNr() {
		int nrOfLevels = currentGame.getAllLevels().size();
		int currentLevelNr = currentGame.getCurrentLevel().getLevelNr();
		int nextLevelNr = currentLevelNr + 1;
		if (nextLevelNr >= nrOfLevels) {
			return -1;
		}
		return nextLevelNr;
	}

	public int getCols() {
		return currentGame.getCurrentLevel().getCols();
	}

	public int getRows() {
		return currentGame.getCurrentLevel().getRows();
	}

	public boolean isScoreGame() {
		return getResources().getString(R.string.game_type).equals("score");
	}

	public boolean isChancesGame() {
		return getResources().getString(R.string.game_type).equals("chances");
	}

	private void processGameTypes() {
		if (isScoreGame()) {
			findViewById(R.id.chances).setVisibility(View.GONE);

		}
		if (isChancesGame()) {
			findViewById(R.id.commentaryLayout).setVisibility(View.GONE);
		}
	}

	public void refreshChances() {
		LinearLayout row1 = (LinearLayout) findViewById(R.id.chances_row1);
		LinearLayout row2 = (LinearLayout) findViewById(R.id.chances_row2);
		row1.removeAllViews();
		row2.removeAllViews();
		int chancesNr = difficultyUtil.getChancesNr(currentGame.getCurrentLevel().getLevelNr());
		for (int i = 0; i < chancesNr; i++) {
			ImageView heart = new ImageView(this);
			int heartId = R.drawable.heartenabled;
			if (i >= chancesNr - currentGame.getStageScoreAgainst()) {
				heartId = R.drawable.heartdisabled;
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT);
			params.setMargins(1, 1, 1, 1);
			heart.setImageDrawable(getResources().getDrawable(heartId));
			if (i < 14) {
				row1.addView(heart, params);
			} else {
				row2.addView(heart, params);
			}
		}
	}
}
