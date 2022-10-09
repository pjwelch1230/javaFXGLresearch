import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.FXGL.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerAction;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import javafx.animation.FadeTransition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MemoryPuzzle extends GameApplication {

    // variables used elsewhere, accessable to other methods
    private static final int NUM_OF_PAIRS = 10; // total numbers of pairs, can be increased for harder difficulty
    private static final int NUM_PER_ROW = 5;   // number of cards per row, can be increased for wider screens
    private Tile selected = null;               // tile object, keeps track of the cards the user has clicked on
    private int clickCounter = 2;               // number of clicks at a time, to limit user input
    private int scoreTracker = 0;               // used to keep track of score, could be replaced using FXGL.geti() and someother call if enough time
    private int streakCounter = 0;              // keeps track of the streak (used for calculating score), same as above
    private int maxStreak = 0;                  // keeps track of the highest streak the player gets

    private TimerAction timerAct;

    @Override
    protected void initSettings(GameSettings settings) {    // sets basics up, ie., screen size, window title, etc. 
        settings.setWidth(800);
        settings.setHeight(800);
        settings.setTitle("The Memory Game");
        settings.setVersion("0.0.1");
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new MemoryMainMenu();
            }
            @Override
            public FXGLMenu newGameMenu() {
                return new PauseMenu();
            }
        });
    }

    private void initCards() {      // initializes the cards as objects, prints them out, all the fun stuff
        int numCard = 1;
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < NUM_OF_PAIRS; i++) {
            tiles.add(new Tile(String.valueOf(numCard)));
            tiles.add(new Tile(String.valueOf(numCard)));
            numCard++;
        }

        Collections.shuffle(tiles);

        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            FXGL.entityBuilder()
            .at(100*(i % NUM_PER_ROW)+150, 100*(i / NUM_PER_ROW)+150)
            .view(tile)
            .buildAndAttach();
        }
    }

    // Sets up the tile object and how it looks, also that it can be interacted with via mouse clicks
    private class Tile extends StackPane {
        private Text text = new Text();

        public Tile(String value) {
            Rectangle border = new Rectangle(100,100);
            border.setFill(null);
            border.setStroke(Color.BLACK);
            border.setStrokeWidth(2);

            text.setText(value);
            text.setFont(Font.font(40));
            text.setFill(Color.BLUE);
            setAlignment(Pos.CENTER);
            getChildren().addAll(border, text);

            setOnMouseClicked(this::handleClick);
            hide();
        }

        // logic for how mouse clicks should be handeled. The matching game logic is here
        public void handleClick(MouseEvent event) {
            if (isFlip() || clickCounter == 0)
                    return;
                
            clickCounter--;
            if (selected == null) {
                    FXGL.play("flip.wav");
                    selected = this;
                    flip(() -> {});
            } else {
                flip(() -> {    // Case for if the cards don't match
                    if (!doesMatch(selected)) {
                        FXGL.play("wilhelm.wav");
                        selected.hide();
                        this.hide();
                        FXGL.set("streak", 0);
                        streakCounter = 0;
                        scoreTracker -= 100;
                        if (FXGL.geti("score") >= 100) {
                            FXGL.inc("score", -100);
                        }  else if (scoreTracker <= 0) {
                            FXGL.set("score", 0);
                        }
                        // Case for if the cards match
                    } else {
                        FXGL.play("match.wav");
                        streakCounter ++;
                        if (maxStreak < streakCounter) {
                            int streakDiff = streakCounter - maxStreak;
                            FXGL.inc("maxStreak", streakDiff);
                            maxStreak = streakCounter;
                        }
                        FXGL.inc("streak", 1);
                        FXGL.inc("matchRemaining", -1);
                        FXGL.inc("score", (streakCounter * 100));
                        FXGL.inc("pairsRem", -1);
                        if (FXGL.geti("pairsRem") == 0) {
                            gameOver("Game Completed: All Matches Found");
                        }
                    }

                    selected = null;
                    clickCounter = 2;
                });
            }
        }

        // Returns the value of the opacity of the card (checks if card has been flipped or not)
        public boolean isFlip() {
            return text.getOpacity() == 1;
        }

        // Changes opacity of the card so that the letter shows up
        public void flip(Runnable action) {
            FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
            ft.setToValue(1);
            ft.setOnFinished(e -> action.run());
            ft.play();
        }
    
        // Changes opacity of the card so the letter is hidden
        public void hide() {
            FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
            ft.setToValue(0);       
            ft.play();
        }

        // Checks if tiles are matching
        public boolean doesMatch(Tile second) {
            return text.getText().equals(second.text.getText());
        }
    }

    // Initializes values used for FXGL functions, useful because of the auto updates
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("streak", 0);
        vars.put("maxStreak", 0);
        vars.put("pairsRem", 10);
        vars.put("timer", 60);
        vars.put("strTimer", "");
        vars.put("matchRemaining", NUM_OF_PAIRS);
    }

    // Sets up the text that appears on screen during the game
    @Override
    protected void initUI() {
        Label scoreLabel = new Label();
        scoreLabel.setTextFill(Color.BLACK);
        scoreLabel.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, null, 26));
        scoreLabel.textProperty().bind(FXGL.getip("score").asString("Score: %d"));
        FXGL.addUINode(scoreLabel, 5, 555);

        Label streakLabel = new Label();
        streakLabel.setTextFill(Color.ORANGERED);
        streakLabel.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, null, 26));
        streakLabel.textProperty().bind(FXGL.getip("streak").asString("Match Streak: %d"));
        FXGL.addUINode(streakLabel, 5, 605);

        Label maxStreakLabel = new Label();
        maxStreakLabel.setTextFill(Color.BLUEVIOLET);
        maxStreakLabel.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, null, 26));
        maxStreakLabel.textProperty().bind(FXGL.getip("maxStreak").asString("Max Streak: %d"));
        FXGL.addUINode(maxStreakLabel, 5, 655);

        Label titleLabel = new Label();
        titleLabel.setTextFill(Color.PURPLE);
        titleLabel.setFont(Font.font("verdana", FontWeight.BOLD, null, 42));
        titleLabel.textProperty().set("The Memory Game");;
        FXGL.addUINode(titleLabel, 180, 25);

        Label timerLabel = new Label();
        timerLabel.setTextFill(Color.RED);
        timerLabel.setFont(Font.font("verdana", FontWeight.EXTRA_BOLD, null, 32));
        timerLabel.textProperty().bind(FXGL.getsp("strTimer"));
        FXGL.addUINode(timerLabel, 195, 85);
    }

    // Method used when the game is finished
    private void gameOver(String overCondition) {
        timerAct.expire();
        FXGL.play("done.wav");
        StringBuilder bob = new StringBuilder();
        bob.append(overCondition+"\n\n")
            .append("Final Score: ")
            .append(FXGL.geti("score"))
            .append("\nMax Streak: ")
            .append(FXGL.geti("maxStreak")+"\n")
            .append("Total "+FXGL.gets("strTimer"));
        if (overCondition.equals("Game Over: Time Limit Reached")) {
            bob.append("\nMatches Remaining: "+String.valueOf(FXGL.geti("matchRemaining")));
        }
        FXGL.getDialogService().showMessageBox(bob.toString(), () -> FXGL.getGameController().gotoMainMenu());
    }

    // Initializes the game, beginning point
    private Entity background;
    @Override
    protected void initGame() {
        // Keeps track of time for the timer to be displayed on screen
        timerAct = FXGL.getGameTimer().runAtInterval(() -> {
            if (FXGL.geti("timer") == 0) {
                gameOver("Game Over: Time Limit Reached");
            }  else if (FXGL.geti("timer") == 21) {
                FXGL.play("speech.wav");
            }
            FXGL.inc("timer", -1);
            FXGL.set("strTimer", secsToString(FXGL.geti("timer")));
        }, Duration.seconds(1));


        // Establishes background
        background = FXGL.entityBuilder()
            .at(0,0)
            .view("gameimage.jpg")
            .buildAndAttach();

        initCards();
    }

    // Converts the time counted by timer into format MM:SS
    private String secsToString (int time) {
        return String.format("Time Remaining: %02d:%02d", time / 60, time % 60);
    }

    public static void main(String[] args) {
        launch(args);
    }
}