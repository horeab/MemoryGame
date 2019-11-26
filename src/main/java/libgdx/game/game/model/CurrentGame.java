package libgdx.game.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.MainActivity;
import util.ActivityUtil;
import util.GameLogic;

public class CurrentGame implements Serializable {

	private static final long serialVersionUID = 9146722923300219753L;

	private MatrixElement[][] levelMatrix;
	private MatrixChoice firstChoice;

	private List<Item> allItems = new ArrayList<Item>();
	private List<Level> allLevels = new ArrayList<Level>();

	private Level currentLevel;

	private int totalScoreFor;
	private int totalScoreAgainst;

	private int stageScoreFor;
	private int stageScoreAgainst;

	private int selectedDifficulty;

	public CurrentGame(int levelNr, int selectedDifficulty, MainActivity context) {
		ActivityUtil activityUtil = new ActivityUtil(context);
		this.allItems = activityUtil.getItemsFromResources();
		this.allLevels = activityUtil.getLevelsFromResources(allItems);

		this.setCurrentLevel(allLevels.get(levelNr));

		GameLogic gameUtil = new GameLogic();
		this.levelMatrix = gameUtil.generateMatrix(currentLevel.getRows(), currentLevel.getCols(), currentLevel.getAvailableItems().size());

		this.firstChoice = null;

		this.selectedDifficulty = selectedDifficulty;
	}

	public int getTotalScoreFor() {
		return totalScoreFor;
	}

	public void setTotalScoreFor(int totalScoreFor) {
		this.totalScoreFor = totalScoreFor;
	}

	public int getTotalScoreAgainst() {
		return totalScoreAgainst;
	}

	public void setTotalScoreAgainst(int totalScoreAgainst) {
		this.totalScoreAgainst = totalScoreAgainst;
	}

	public int getStageScoreFor() {
		return stageScoreFor;
	}

	public void setStageScoreFor(int stageScoreFor) {
		this.stageScoreFor = stageScoreFor;
	}

	public int getStageScoreAgainst() {
		return stageScoreAgainst;
	}

	public void setStageScoreAgainst(int stageScoreAgainst) {
		this.stageScoreAgainst = stageScoreAgainst;
	}

	public MatrixElement[][] getLevelMatrix() {
		return levelMatrix;
	}

	public void setLevelMatrix(MatrixElement[][] levelMatrix) {
		this.levelMatrix = levelMatrix;
	}

	public MatrixChoice getFirstChoice() {
		return firstChoice;
	}

	public void setFirstChoice(MatrixChoice firstChoice) {
		this.firstChoice = firstChoice;
	}

	public List<Item> getAllItems() {
		return allItems;
	}

	public void setAllItems(List<Item> allItems) {
		this.allItems = allItems;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(Level currentLevel) {
		this.currentLevel = currentLevel;
	}

	public List<Level> getAllLevels() {
		return allLevels;
	}

	public void setAllLevels(List<Level> allLevels) {
		this.allLevels = allLevels;
	}

	public int getSelectedDifficulty() {
		return selectedDifficulty;
	}

	public void setSelectedDifficulty(int selectedDifficulty) {
		this.selectedDifficulty = selectedDifficulty;
	}
}
