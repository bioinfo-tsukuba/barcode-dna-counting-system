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
 * 確認用ダイアログを表示する.
 */
public class ExecutionDialogue {
    //========================================================================//
    // fx:id
    //========================================================================//
    @FXML
    private AnchorPane executionDialogueID;     // Execution dialogue
    @FXML
    private Label executionLabelID;             // Message label

    //========================================================================//
    // Local data
    //========================================================================//
    /**
     * 確認内容（true:OK、false:CANCEL）.
     */
    private boolean isConfirm;

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * 確認用ダイアログを表示するクラスのコンストラクター.
     */
    public ExecutionDialogue() {
    }

    /**
     * 確認用ダイアログを表示する.
     *
     * @param message 確認用メッセージ
     * @param color red:エラー、green:ワーニング、blue:メッセージ
     * @param node ダイアログ表示のための基準画面
     * @return 確認内容（true:OK、false:CANCEL）
     * @throws IOException 例外処理
     */
    public boolean open(String message, String color, Node... node) throws IOException {
        // Set message
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ExecutionDialogue.fxml"));
        Parent root = loader.load();
        ExecutionDialogue executionDialogue = loader.getController();
        executionDialogue.executionLabelID.setText(message);
        executionDialogue.executionLabelID.setStyle("-fx-text-fill: " + color);

        // Open dialogue
        Stage stage = new Stage( StageStyle.UTILITY);
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

        stage.initModality( Modality.APPLICATION_MODAL);

        // Check base screen
        if (node.length > 0) {
            stage.initOwner(node[0].getScene().getWindow());
        }

        stage.showAndWait();

        // Dialogue click information
        return executionDialogue.isConfirm;
    }

    //========================================================================//
    // On Action
    //========================================================================//
    /**
     * OKボタンがクリックされたときの処理.
     *
     * @param event アクションイベント
     */
    @FXML
    private void okAction(ActionEvent event) {      // OK button
        this.isConfirm = true;
        this.executionDialogueID.getScene().getWindow().hide();
    }

    /**
     * CANCELボタンがクリックされたときの処理.
     *
     * @param event アクションイベント
     */
    @FXML
    private void cancelAction(ActionEvent event) {  // CANCEL button
        this.isConfirm = false;
        this.executionDialogueID.getScene().getWindow().hide();
    }
}
