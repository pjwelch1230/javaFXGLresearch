import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static com.almasb.fxgl.dsl.FXGL.*;

public class MemoryMainMenu extends FXGLMenu {
    
    public MemoryMainMenu() {
        super(MenuType.MAIN_MENU);
        
        Image image = new Image("file:MemoryGame/src/assets/textures/menuimage.jpg");
        ImageView background = new ImageView();
        background.setImage(image);

        var title = getUIFactoryService().newText(getSettings().getTitle(), Color.PURPLE, 46.0);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(1.5);
        centerTextBind(title, getAppWidth() / 2, 160);

        var version = getUIFactoryService().newText(getSettings().getVersion(), Color.BLACK, 26.0);
        centerTextBind(version, getAppWidth() / 2.0, 180);
        version.setStroke(Color.BLACK);
        version.setStrokeWidth(1.5);

        var start_button = new MenuButton("Start new game", this::fireNewGame);
        start_button.setTranslateX(FXGL.getAppWidth() / 2- 200 / 2);
        start_button.setTranslateY(FXGL.getAppHeight() / 2- 40 / 2);

        var exit_button = new MenuButton("Quit", this::fireExit);
        exit_button.setTranslateX(FXGL.getAppWidth() / 2- 200 / 2);
        exit_button.setTranslateY(FXGL.getAppHeight() / 2+ 50 / 2);

        getContentRoot().getChildren().addAll(background, start_button, exit_button, title, version);
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
