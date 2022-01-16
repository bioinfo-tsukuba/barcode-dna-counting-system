package barcode;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * エラーダイアログを表示する.
 */
public class ErrorDialogue {
    //========================================================================//
    // fx:id
    //========================================================================//
    @FXML
    private AnchorPane errorDialogueID;     // Error dialogue
    @FXML
    private Label errorLabelID;             // Error message label

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * エラーダイアログを表示するクラスのコンストラクター.
     */
    public ErrorDialogue() {
    }

    /**
     * エラーダイアログを表示する.
     *
     * @param message メッセージ
     * @param color red:エラー、green:ワーニング、blue:メッセージ
     * @param node ダイアログ表示のための基準画面
     * @throws IOException 例外処理
     */
    public void open(String message, String color, Node... node) throws IOException {
        // Set message
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ErrorDialogue.fxml"));
        Parent root = loader.load();
        ErrorDialogue errorDialogue = loader.getController();
        errorDialogue.errorLabelID.setText(message);
        errorDialogue.errorLabelID.setStyle("-fx-text-fill: " + color);

        // Open dialogue
        Stage stage = new Stage(StageStyle.UTILITY);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add(this.getClass().getResource("barcode.css").toExternalForm());

        // Check dialogue type
        if (color.equals("blue")) {
            stage.setTitle("Message");
        } else if (color.equals("red")) {
            stage.setTitle("Error");
        } else {
            stage.setTitle("Warning");
        }

        stage.initModality(Modality.APPLICATION_MODAL);

        // Check base screen
        if (node.length > 0) {
            stage.initOwner(node[0].getScene().getWindow());
        }

        stage.showAndWait();
    }

    //========================================================================//
    // On Action
    //========================================================================//
    /**
     * OKボタンがクリックされたときにダイアログを閉じる.
     *
     * @param event アクションイベント
     */
    @FXML
    private void okAction(ActionEvent event) {       // OK button
        this.errorDialogueID.getScene().getWindow().hide();
    }
}
