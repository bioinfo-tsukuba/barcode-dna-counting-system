package barcode;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * メインクラス.
 */
public class Main extends Application {
    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * プログラムをスタートする.
     *
     * @param primaryStage プライマリステージ
     * @throws Exception 例外処理
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        // Set locale for English
        Locale.setDefault(Locale.ENGLISH);

        // Open screen
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("MainScreen.fxml"));
        Parent root = loader.load();

        // Scroll message bar
        MainScreen mainScreen = loader.getController();
        mainScreen.programVersion();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(this.getClass().getResource("barcode.css").toExternalForm());
        primaryStage.setTitle(ProgramVersion.PROGRAM);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * メインクラスのコンストラクター.
     *
     * @param args メイン関数の引数（未使用）
     */
    public static void main(String[] args) {
        launch(args);
    }
}
