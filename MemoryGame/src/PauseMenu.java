import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PauseMenu extends FXGLMenu {

    private static int SIZE = 150;
    private Animation<?> animation;
    public PauseMenu() {
        super(MenuType.GAME_MENU);

        getContentRoot().setTranslateX(FXGL.getAppWidth() / 2.0 - 150);
        getContentRoot().setTranslateY(FXGL.getAppHeight() / 2.0 - 150);

        var resume_button = new MenuButton("Resume Game", this::fireResume);
        resume_button.setTranslateX(FXGL.getAppWidth() / 2- 710 / 2);
        resume_button.setTranslateY(FXGL.getAppHeight() / 2- 820 / 2);

        var exit_button = new MenuButton("Back", this::fireExitToMainMenu);
        exit_button.setTranslateX(FXGL.getAppWidth() / 2- 710 / 2);
        exit_button.setTranslateY(FXGL.getAppHeight() / 2- 720 / 2);

        getContentRoot().getChildren().addAll(exit_button, resume_button);
    }
    private static class MenuButton extends StackPane {
        public MenuButton(String name, Runnable action) {

            var bg = new Rectangle(200, 40);
            bg.setStroke(Color.WHITE);

            var text = FXGL.getUIFactoryService().newText(name, Color.WHITE, 20);

            bg.fillProperty().bind(
                    Bindings.when(hoverProperty()).then(Color.BLACK).otherwise(Color.BLUE)
            );

            text.fillProperty().bind(
                    Bindings.when(hoverProperty()).then(Color.PURPLE).otherwise(Color.WHITE)
            );

            setOnMouseClicked(e -> action.run());

            getChildren().addAll(bg, text);
        }
    }
}