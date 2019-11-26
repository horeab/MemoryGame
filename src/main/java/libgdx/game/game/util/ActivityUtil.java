package libgdx.game.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import libgdx.game.game.model.Item;
import libgdx.game.game.model.Level;
import libgdx.game.game.model.MatrixChoice;
import libgdx.game.game.model.MatrixElement;
import main.MainActivity;
import model.CurrentGame;
import model.Item;
import model.Level;
import model.MatrixChoice;
import model.MatrixElement;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.simapps.memorygame.R;

import db.StoreManagement;

public class ActivityUtil {

    public static final int BUTTON_ID_STARTING_INT_VALUE = 0;
    public final static int ROW_ID_STARTING_INT_VALUE = 200;

    private MainActivity context;
    private GameLogic gameUtil;
    private DifficultyUtil difficultyUtil;
    private StoreManagement storeManagement;

    public ActivityUtil(MainActivity context) {
        this.context = context;
        this.gameUtil = new GameLogic();
        this.difficultyUtil = new DifficultyUtil(context);
        this.storeManagement = new StoreManagement(context);
    }

    ;

    public void addImagesToRows(int rows, int col) {
        int position = BUTTON_ID_STARTING_INT_VALUE;
        for (TableRow row : getCreatedRows(rows)) {
            for (int nr = 0; nr < col; nr++) {
                ImageView image = createImage(position);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                int margin = 0;
                lp.setMargins(margin, margin, margin, margin);
                row.addView(image, lp);
                position++;
            }
        }
    }

    private ImageView createImage(int position) {
        ImageView image = new ImageView(context);
        image.setId(position);
        return image;

    }

    private List<TableRow> getCreatedRows(int nrRows) {
        List<TableRow> rows = new ArrayList<TableRow>();
        for (int i = 0; i < nrRows; i++) {
            TableRow row = (TableRow) context.findViewById(ROW_ID_STARTING_INT_VALUE + i);
            rows.add(row);
        }
        return rows;
    }

    public void createRows(int rows) {
        TableLayout table = (TableLayout) context.findViewById(R.id.buttonsTable);
        TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < rows; i++) {
            TableRow row = new TableRow(context);
            row.setId(ROW_ID_STARTING_INT_VALUE + i);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            table.addView(row, rowLp);
        }
    }

    public void onClickImageViews(final CurrentGame currentGame) {
        int position = 0;
        int i = 0;
        final int rows = currentGame.getCurrentLevel().getRows();
        final int col = currentGame.getCurrentLevel().getCols();
        for (int row = 0; row < rows; row++) {
            for (int nr = 0; nr < col; nr++) {
                ImageView image = (ImageView) context.findViewById(BUTTON_ID_STARTING_INT_VALUE + position);
                final int finalI = i;
                final int finalNr = nr;
                image.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MatrixElement[][] items = currentGame.getLevelMatrix();
                        MatrixElement currentItem = items[finalI][finalNr];
                        MatrixChoice clickedItem = new MatrixChoice(finalNr, finalI, currentItem.getItem());
                        if (context.isEnableImageClick() && !clickedItem.equals(currentGame.getFirstChoice()) && !currentItem.isFound()
                                && !currentItem.isShowed()) {
                            currentItem.setShowed(true);
                            refreshImageViews(rows, col, items, false);

                            if (currentGame.getFirstChoice() != null) {
                                processScore(currentGame, clickedItem);
                                if (isLevelFinished(items) && context.isScoreGame()) {
                                    processLevelFinishedPopup(currentGame.getTotalScoreFor(), currentGame.getTotalScoreAgainst(),
                                            currentGame.getStageScoreFor(), currentGame.getStageScoreAgainst());
                                }
                                if (context.isChancesGame()) {
                                    if (isLevelFinished(items)
                                            || !isLevelSucces(currentGame.getStageScoreFor(), currentGame.getStageScoreAgainst(), currentGame
                                            .getCurrentLevel().getLevelNr())) {
                                        currentGame.setTotalScoreFor(currentGame.getTotalScoreFor() + currentGame.getStageScoreFor());
                                        processLevelFinishedPopup(currentGame.getTotalScoreFor(), currentGame.getTotalScoreAgainst(),
                                                currentGame.getStageScoreFor(), currentGame.getStageScoreAgainst());
                                    }
                                }
                            }

                            MatrixChoice firstItemClicked = currentGame.getFirstChoice() == null ? clickedItem : null;
                            currentGame.setFirstChoice(firstItemClicked);

                            secondChoiceMadeProcesses(currentGame);
                        }
                    }

                    private void processLevelFinishedPopup(int totalScoreFor, int totalScoreAgainst, int stageScoreFor, int stageScoreAgainst) {
                        boolean levelSucces = isLevelSucces(stageScoreFor, stageScoreAgainst, currentGame.getCurrentLevel().getLevelNr());
                        int stageScore = getScore(stageScoreFor, stageScoreAgainst);
                        int totalScore = getScore(totalScoreFor, totalScoreAgainst);
                        if (context.isChancesGame()) {
                            totalScore = totalScoreFor;
                            stageScore = stageScoreFor;
                        }
                        Dialog levelFinishedPopup = context.levelFinishedPopup(levelSucces, !levelSucces, stageScore, totalScore);
                        levelFinishedPopup.show();
                    }
                });
                position++;
            }
            i++;
        }
    }

    private void processScore(CurrentGame currentGame, MatrixChoice clickedItem) {
        int scoreForToIncrement = 0;
        int scoreAgainstToIncrement = 0;
        String itemValue = null;

        if (gameUtil.doButtonsMatchAndProcess(clickedItem, currentGame.getFirstChoice(), currentGame.getLevelMatrix())) {
            insertDiscoveredItem(currentGame, clickedItem);
            playSound(R.raw.itemfound);
            scoreForToIncrement = difficultyUtil.getScoreForToIncrement();
            int rows = currentGame.getCurrentLevel().getRows();
            int col = currentGame.getCurrentLevel().getCols();
            refreshImageViews(rows, col, currentGame.getLevelMatrix(), false);
            if (context.isScoreGame()) {
                itemValue = getRandomItemValue(clickedItem.getItem(), currentGame.getAllItems());
                itemValue = getRandomCommentaryValue(itemValue);
            } else {
                scoreForToIncrement = 10;
            }
        } else {
            scoreAgainstToIncrement = difficultyUtil.getScoreAgainstToIncrement();
            if (context.isChancesGame()) {
                scoreForToIncrement = currentGame.getStageScoreFor() > 0 || currentGame.getTotalScoreFor() > 0 ? -1 : 0;
            }
        }
        currentGame.setStageScoreFor(currentGame.getStageScoreFor() + scoreForToIncrement);
        currentGame.setStageScoreAgainst(currentGame.getStageScoreAgainst() + scoreAgainstToIncrement);
        if (!context.isChancesGame()) {
            currentGame.setTotalScoreFor(currentGame.getTotalScoreFor() + scoreForToIncrement);
            currentGame.setTotalScoreAgainst(currentGame.getTotalScoreAgainst() + scoreAgainstToIncrement);
        }

        context.refreshTextViews(itemValue);
        context.refreshChances();
    }

    private String getRandomCommentaryValue(String itemValue) {
        String[] comms = context.getResources().getStringArray(R.array.commentary_values);
        String commentary = comms[new Random().nextInt(comms.length)];
        commentary = Utils.fontString(itemValue, false, "#003399") + " " + Utils.fontString(commentary, false, null);
        return commentary;
    }

    private void insertDiscoveredItem(CurrentGame currentGame, MatrixChoice clickedItem) {
        List<String> discoveredItems = storeManagement.retrieveDiscoveredElements();
        Item item = getItemForIndex(clickedItem.getItem(), currentGame.getAllItems());
        if (!discoveredItems.contains(item.getItemName())) {
            storeManagement.insertDiscoveredElemName(item.getItemName());
        }
    }

    private void secondChoiceMadeProcesses(final CurrentGame currentGame) {
        if (currentGame.getFirstChoice() == null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int rows = currentGame.getCurrentLevel().getRows();
                    int col = currentGame.getCurrentLevel().getCols();
                    refreshImageViews(rows, col, currentGame.getLevelMatrix(), false);
                }
            }, 500);
        }
    }

    @SuppressWarnings("deprecation")
    public void refreshImageViews(int rows, int col, MatrixElement[][] items, boolean ignoreBackGround) {
        int i = 0;
        int position = 0;
        for (int row = 0; row < rows; row++) {
            for (int nr = 0; nr < col; nr++) {
                MatrixElement currentItem = items[i][nr];
                ImageView image = (ImageView) context.findViewById(BUTTON_ID_STARTING_INT_VALUE + position);
                String imageIdentifier = currentItem.isShowed() ? "item" + currentItem.getItem() : "unknown";
                int resID = context.getResources().getIdentifier(imageIdentifier, "drawable", context.getPackageName());
                image.setImageResource(resID);
                image.setBackgroundDrawable(backgr(currentItem.isShowed(), currentItem.isFound(), ignoreBackGround));
                position++;
            }
            i++;
        }
    }

    public GradientDrawable backgr(boolean isShowed, boolean isFound, boolean ignoreBackGround) {
        if (isShowed && !isFound && !ignoreBackGround) {
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(8);
            shape.setAlpha(150);
            shape.setColor(context.getResources().getColor(R.color.light_gray_blue_level2));
            return shape;
        } else {
            return null;
        }
    }

    public List<Level> getLevelsFromResources(List<Item> allItems) {
        List<Level> levels = new ArrayList<Level>();
        for (GameLevel gameLevel : GameLevel.values()) {
            List<Item> levelItems = getItemsForRange(0, Integer.parseInt(levelValues[3]), allItems);
            Level level = new Level(gameLevel.ordinal(), gameLevel.getRows(), gameLevel.getCols(), levelItems);
            levels.add(level);
        }
        return levels;
    }

    public static List<Item> getItemsFromResources(Activity context) {
        List<Item> items = new ArrayList<Item>();
        String[] itemNames = context.getResources().getStringArray(R.array.item_names);
        for (int i = 0; i < itemNames.length; i++) {
            Item item = new Item();
            item.setItemIndex(i);
            item.setItemName(itemNames[i]);
            if (isScoreGame(context)) {
                String itemValuesArrayId = "item_" + i + "_values";
                int resID = context.getResources().getIdentifier(itemValuesArrayId, "array", context.getPackageName());
                String[] itemValues = context.getResources().getStringArray(resID);
                for (int j = 0; j < itemValues.length; j++) {
                    item.getItemValues().add(itemValues[j]);
                }
            }
            items.add(item);
        }
        return items;
    }

    public static boolean isScoreGame(Activity context) {
        return context.getResources().getString(R.string.game_type).equals("score");
    }

    public List<Item> getItemsFromResources() {
        return getItemsFromResources(context);
    }

    private List<Item> getItemsForRange(int start, int end, List<Item> allItems) {
        List<Item> items = new ArrayList<Item>();
        for (int i = 0; i < end; i++) {
            items.add(allItems.get(i));
        }
        return items;
    }

    private String getRandomItemValue(int itemIndex, List<Item> items) {
        Item item = getItemForIndex(itemIndex, items);
        int randomNr = new Random().nextInt(item.getItemValues().size());
        return item.getItemValues().get(randomNr);
    }

    private Item getItemForIndex(int index, List<Item> items) {
        for (Item item : items) {
            if (item.getItemIndex() == index) {
                return item;
            }
        }
        return null;
    }

    private boolean isLevelSucces(int scoreFor, int scoreAgainst, int levelNr) {
        if (context.isScoreGame()) {
            if (scoreFor >= scoreAgainst) {
                return true;
            }
            return false;
        }
        if (context.isChancesGame()) {
            if (scoreAgainst < difficultyUtil.getChancesNr(levelNr)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isLevelFinished(MatrixElement[][] matrix) {
        boolean isGameOver = true;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (!matrix[i][j].isFound()) {
                    return false;
                }
            }
        }
        return isGameOver;
    }

    public int getScore(int scoreFor, int scoreAgainst) {
        return scoreFor - scoreAgainst;
    }

    public static void playSound(int soundId, Activity context) {
        StoreManagement storeManagement = new StoreManagement(context);
        try {
            if (storeManagement.isSoundOn()) {
                MediaPlayer mp = MediaPlayer.create(context, soundId);
                mp.start();
            }
        } catch (Exception e) {
        }
    }

    public static void playBtnSound(Activity context) {
        playSound(R.raw.buttonclicked, context);
    }

    public void playSound(int soundId) {
        playSound(soundId, context);
    }
}
