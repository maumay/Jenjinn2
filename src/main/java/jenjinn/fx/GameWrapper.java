/**
 *
 */
package jenjinn.fx;

import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import jenjinn.engine.base.Side;
import jenjinn.engine.pieces.ChessPiece;
import xawd.jflow.utilities.Optionals;

/**
 * @author ThomasB
 */
public final class GameWrapper extends Region
{
	private static final String CSS_STYLE = "-fx-background-color: #aeb5c1;";
	private static final int MIN_WIDTH = 90, MIN_HEIGHT = 100;

	private final Label gameInfoLabel, chooseYourSide;
	private final Button chooseWhite, chooseBlack, playAgain;
	private Optional<ChessGame> chessGame = Optional.empty();

	public GameWrapper()
	{
		setStyle(CSS_STYLE);
		setMinSize(MIN_WIDTH, MIN_HEIGHT);
		setPadding(new Insets(5));
		setSnapToPixel(true);

		gameInfoLabel = new Label(GameStageMessages.WAITING_FOR_GAME_START);
		gameInfoLabel.setAlignment(Pos.CENTER_LEFT);
		gameInfoLabel.setFont(Font.font(12));
		gameInfoLabel.setPadding(new Insets(2));

		chooseYourSide = new Label("Choose your side");
		chooseYourSide.setAlignment(Pos.CENTER);
		chooseYourSide.setFont(Font.font(14));
		chooseYourSide.setPadding(new Insets(2));

		chooseWhite = createSideSelectionButton(Side.WHITE);
		chooseBlack = createSideSelectionButton(Side.BLACK);

		playAgain = new Button("Play again");
		playAgain.setAlignment(Pos.CENTER);
		playAgain.setFont(Font.font(12));
		playAgain.setPadding(new Insets(2, 5, 2, 5));
		playAgain.setVisible(false);
		playAgain.setOnAction(evt -> reset());

		getChildren().addAll(gameInfoLabel, chooseYourSide, chooseWhite, chooseBlack, playAgain);
	}
	
	private Button createSideSelectionButton(Side side)
	{
		ChessPiece toDisplay = side.isWhite()? ChessPiece.WHITE_QUEEN: ChessPiece.BLACK_QUEEN;
		Button button = new Button();
		button.setStyle(CSS_STYLE);
		button.setGraphic(new ImageView(ImageCache.INSTANCE.getImageOf(toDisplay)));
		button.setAlignment(Pos.CENTER);
		button.setFont(Font.font(14));
		button.setPadding(new Insets(5));
		button.setOnAction(evt -> initGame(side));
		return button;
	}

	private void reset()
	{
		final ChessGame toRemove = Optionals.getOrError(chessGame);
		chessGame = Optional.empty();
		getChildren().remove(toRemove.getFxComponent());
		gameInfoLabel.setText(GameStageMessages.WAITING_FOR_GAME_START);
		setSideSelectorVisibility(true);
		playAgain.setVisible(false);
	}

	private void initGame(Side humanSide)
	{
		final ChessGame newGame = new ChessGame(humanSide, ColorScheme.getDefault());
		getChildren().add(newGame.getFxComponent());
		gameInfoLabel.setText(GameStageMessages.WHITE_TO_MOVE);
		chessGame = Optional.of(newGame);
		setSideSelectorVisibility(false);
		addPropertyListeners(newGame);
		Platform.runLater(this::layoutChildren);
	}

	private void setSideSelectorVisibility(boolean visible)
	{
		chooseYourSide.setVisible(visible);
		chooseWhite.setVisible(visible);
		chooseBlack.setVisible(visible);
	}

	private void addPropertyListeners(ChessGame game)
	{
		game.getSideToMoveProperty().addListener((x, oldSide, newSide) -> {
			Platform.runLater(() -> {
				final String message = newSide.isWhite() ? GameStageMessages.WHITE_TO_MOVE : GameStageMessages.BLACK_TO_MOVE;
				gameInfoLabel.setText(message);
			});
		});

		game.getTerminationStateProperty().addListener((x, y, termState) -> {
			Platform.runLater(() -> {
				switch (termState) {
				case DRAW:
					gameInfoLabel.setText(GameStageMessages.DRAW);
					break;
				case WHITE_WIN:
					gameInfoLabel.setText(GameStageMessages.WHITE_WIN);
					break;
				case BLACK_WIN:
					gameInfoLabel.setText(GameStageMessages.BLACK_WIN);
					break;
				default:
					gameInfoLabel.setText("Text error");
				}
				playAgain.setVisible(true);
			});
		});
	}

	@Override
	protected void layoutChildren()
	{
		getChildren().stream().forEach(x -> x.autosize());
		final Insets pad = getPadding();
		final double w = getWidth(), h = getHeight();
		gameInfoLabel.relocate(pad.getLeft(), pad.getTop());
		playAgain.relocate(w - pad.getRight() - playAgain.getWidth(), pad.getTop());
		chooseYourSide.relocate((w - chooseYourSide.getWidth()) / 2, h / 3);
		final double buttonY = chooseYourSide.getLayoutY() + chooseYourSide.getHeight() + 5;
		chooseWhite.relocate(w / 2 - 5 - chooseWhite.getWidth(), buttonY);
		chooseBlack.relocate(w / 2 + 5, buttonY);

		if (chessGame.isPresent()) {
			final double y1 = gameInfoLabel.getLayoutBounds().getMaxY();
			final double y2 = playAgain.getLayoutBounds().getMaxY();
			final double gameY = snapSize(Math.max(y1, y2) + 5);
			final double gameX = snapSize(pad.getLeft());
			final double gameWidth = snapSize(w - pad.getLeft() - pad.getRight());
			final double gameHeight = snapSize(h - pad.getTop() - gameY);
			chessGame.get().getFxComponent().resizeRelocate(gameX, gameY, gameWidth, gameHeight);
		}
	}

	public void interpolateTimeLimit(double fraction)
	{
		if (chessGame.isPresent()) {
			chessGame.get().interpolateMoveTime(fraction);
		}
	}
}
