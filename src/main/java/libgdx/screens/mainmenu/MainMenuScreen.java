package libgdx.screens.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.List;

import libgdx.game.ScreenManager;
import libgdx.game.game.model.CurrentGame;
import libgdx.game.game.model.MatrixChoice;
import libgdx.game.game.model.MatrixElement;
import libgdx.game.game.model.TableCell;
import libgdx.game.game.util.GameLevel;
import libgdx.game.game.util.GameLogic;
import libgdx.graphics.GraphicUtils;
import libgdx.implementations.skelgame.SkelGameRatingService;
import libgdx.resources.Resource;
import libgdx.screen.AbstractScreen;

public class MainMenuScreen extends AbstractScreen<ScreenManager> {

    private MatrixElement[][] levelMatrix;
    private CurrentGame currentGame;
    private boolean enableImageClick = false;
    private List<TableCell> cells = new ArrayList<>();

    public MainMenuScreen(CurrentGame currentGame) {
        this.currentGame = currentGame;
    }

    @Override
    protected void initFields() {
        levelMatrix = new GameLogic().generateMatrix(GameLevel._0);
    }

    @Override
    public void buildStage() {
        new SkelGameRatingService(this).appLaunched();
        addAllTable();
    }

    private void addAllTable() {
        Table table = new Table();

        final int rows = currentGame.getCurrentLevel().getRows();
        final int columns = currentGame.getCurrentLevel().getCols();


        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                final MatrixElement currentItem = levelMatrix[row][col];
                final MatrixChoice clickedItem = new MatrixChoice(col, row, currentItem.getItem());
                Table cell = new Table();
                Image image = getMatrixElementImage(currentItem);
                cell.add(image);
                cells.add(new TableCell(cell, currentItem));
                image.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (enableImageClick && !clickedItem.equals(currentGame.getFirstChoice()) && !currentItem.isFound()
                                && !currentItem.isShowed()) {
                            currentItem.setShowed(true);
                            refreshImageViews();

                            if (currentGame.getFirstChoice() != null) {
                                processScore(currentGame, clickedItem);
                                if (isLevelFinished(levelMatrix)) {
                                    currentGame.setTotalScoreFor(currentGame.getTotalScoreFor() + currentGame.getStageScoreFor());
                                    processLevelFinishedPopup(currentGame.getTotalScoreFor(), currentGame.getTotalScoreAgainst(),
                                            currentGame.getStageScoreFor(), currentGame.getStageScoreAgainst());
                                }
                            }

                            MatrixChoice firstItemClicked = currentGame.getFirstChoice() == null ? clickedItem : null;
                            currentGame.setFirstChoice(firstItemClicked);

                            if (currentGame.getFirstChoice() == null) {
                                refreshImageViews();
                            }
                        }
                    }
                });
                table.add(cell);
            }
            table.row();
        }

        addActor(table);
    }

    private void processScore(CurrentGame currentGame, MatrixChoice clickedItem) {
        int scoreForToIncrement = 0;
        int scoreAgainstToIncrement = 0;
        String itemValue = null;
    }

    private void processLevelFinishedPopup(int totalScoreFor, int totalScoreAgainst,
                                           int stageScoreFor, int stageScoreAgainst) {
    }

    public void refreshImageViews() {
        for (TableCell tableCell : cells) {
            tableCell.getCell().clearChildren();
            if (tableCell.getMatrixElement().isFound() || tableCell.getMatrixElement().isShowed()) {
                tableCell.getCell().add(getMatrixElementImage(tableCell.getMatrixElement()));
            }
        }
    }

    private Image getMatrixElementImage(MatrixElement currentItem) {
        return GraphicUtils.getImage(Resource.valueOf(currentItem.isShowed() ? "item" + currentItem.getItem() : "unknown"));
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

    @Override
    public void onBackKeyPress() {
        Gdx.app.exit();
    }

}
