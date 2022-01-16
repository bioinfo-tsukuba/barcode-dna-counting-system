package barcode;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * メインスクリーンの処理を行う.
 */
public class MainScreen {
    //========================================================================//
    // fx:id
    //========================================================================//
    @FXML
    private AnchorPane mainPaneID;                  // Main window
    @FXML
    private TextField barcodeDnaFileID;             // Barcode DNA file
    @FXML
    private TextArea read1FilesID;                  // Read1 FASTQ files
    @FXML
    private TextArea read2FilesID;                  // Read2 FASTQ files
    @FXML
    private CheckBox checkSelectFolderID;           // Select folder
    @FXML
    private Spinner<Integer> spinnerRemoveFirstID;  // Remove first bases
    @FXML
    private Spinner<Integer> spinnerRemoveLastID;   // Remove last bases
    @FXML
    private Spinner<Integer> spinnerMaxFlankID;     // Max flank mismatches
    @FXML
    private Spinner<Integer> spinnerMaxMidID;       // Max mid mismatches
    @FXML
    private Spinner<Integer> spinnerMinimumReadLengthID;    // Minimum read length
    @FXML
    private TextField evalueID;                     // E-value
    @FXML
    private Spinner<Integer> spinnerCollectFlankID; // Collect flank mismatches
    @FXML
    private Spinner<Integer> spinnerCollectMidID;   // Collect mid mismatches
    @FXML
    private ChoiceBox<String> choiceStrandID;               // Strand
    @FXML
    private TextField outputFolderID;               // Output folder
    @FXML
    private TextField outputPrefixID;               // Output prefix
    @FXML
    private Spinner<Integer> spinnerThreadsID;      // Number of threads
    @FXML
    private Button stopCountBarcodeID;              // Stop
    @FXML
    private Button executeCountDnaBarcodeID;        // Execute
    @FXML
    private ListView<String> messagesID;            // Messages

    //========================================================================//
    // Local class
    //========================================================================//
    /**
     * 解析環境を設定する.
     */
    private final AdminConfiguration adminConfiguration;

    /**
     * 計算オプションを管理する.
     */
    private Options options;

    /**
     * ユーザーの計算条件を保存して再現する.
     */
    private final UserConfiguration userConfiguration;

    /**
     * エラーダイアログを表示する.
     */
    private final ErrorDialogue errorDialogue;

    /**
     * バーコードDNA配列をカウントする.
     */
    private final CountBarcodeDna countBarcodeDna;

    /**
     * バーコードDNA配列のカウント数を集計する.
     */
    private final CollectMF collectMF;

    //========================================================================//
    // Local parameter
    //========================================================================//
    /**
     * 検索フォルダー.
     */
    String stockSearchFolder = System.getProperty("user.home");

    /**
     * メッセージ出力欄のスクロール行数.
     */
    private final int MESSAGE_SCROLL = 5;   // for this case

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * メインスクリーンの処理を行うクラスのコンストラクター.
     */
    public MainScreen() {
        // Local class
        this.adminConfiguration = new AdminConfiguration();
        this.options = new Options();
        this.userConfiguration = new UserConfiguration();
        this.errorDialogue = new ErrorDialogue();
        this.countBarcodeDna = new CountBarcodeDna();
        this.collectMF = new CollectMF();
    }

    /**
     * 開始時にメッセージを出力する.
     */
    public void programVersion() {
        for (int i = 0; i < this.MESSAGE_SCROLL; i++) {
            this.messagesID.getItems().add("");
        }
        this.messagesID.getItems().add("Wellcome to " + ProgramVersion.PROGRAM + " "
                + ProgramVersion.VERSION);
    }

    //========================================================================//
    // [On Action]
    //========================================================================//
    /**
     * バーコードDNAファイルを選択する.
     *
     * @param event アクションイベント
     */
    @FXML
    private void selectBarcodeDnaAction(ActionEvent event) {
        this.stockSearchFolder = CommonTools.selectFastaFile(this.barcodeDnaFileID,
                this.stockSearchFolder, this.mainPaneID);
    }

    /**
     * リードファイルを選択する.
     *
     * @param event アクションイベント
     */
    @FXML
    private void selectRead1Action(ActionEvent event) {
        if (this.checkSelectFolderID.isSelected()) {
            this.stockSearchFolder = CommonTools.selectFastqFolder(this.read1FilesID,
                    this.read2FilesID, this.stockSearchFolder, this.mainPaneID);
        } else {
            this.stockSearchFolder = CommonTools.selectFastqFiles(this.read1FilesID,
                    this.read2FilesID, this.stockSearchFolder, this.mainPaneID);
        }
    }

    /**
     * リードファイルのリストをクリアする.
     *
     * @param event アクションイベント
     */
    @FXML
    private void clearReadAction(ActionEvent event) {
        this.read1FilesID.clear();
        this.read2FilesID.clear();
    }

    /**
     * 出力フォルダーを選択する.
     *
     * @param event アクションイベント
     */
    @FXML
    private void selectOutputFolderAction(ActionEvent event) {
        this.stockSearchFolder = CommonTools.selectFolder(this.outputFolderID,
                this.stockSearchFolder, this.mainPaneID);
    }

    /**
     * バーコードDNA配列のカウント数を集計をする.
     *
     * @param event アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void collectAction(ActionEvent event) throws IOException {
        if (this.checkCollect()) {      // check input files
            this.messagesID.scrollTo(this.messagesID.getItems().size());
            this.setConfiguration();    // 計算条件を管理用に設定する.
            // Set count DNA barcode parameters.
            this.collectMF.setParameters(this.adminConfiguration, this.options);

             // Execute count DNA barcode
            this.collectMF.execution(this.messagesID, this.mainPaneID);
        }
    }

    /**
     * HTMLファイルを作成して表示する.
     *
     * @param event アクションイベント
     */
    @FXML
    private void drawFiguresAction(ActionEvent event) throws IOException {
        if (this.checkDraw()) {
            this.messagesID.scrollTo(this.messagesID.getItems().size());
            this.setConfiguration();        // 計算条件を管理用に設定する.
            CreateHtml createHtml = new CreateHtml();
            String html = createHtml.create(this.options, this.messagesID);
            if (html != null) {
                Application app = new Application() {
                    @Override
                    public void start(Stage primaryStage) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
                app.getHostServices().showDocument(html);
            } else {
                // open folder
                Runtime rt = Runtime.getRuntime();
                rt.exec("explorer " + this.outputFolderID.getText());
            }
        }
    }

    /**
     * バーコードDNA配列をカウントする.
     *
     * @param event アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void executeCountDnaBarcodeAction(ActionEvent event) throws IOException {
        if (this.checkCountDNAbarcode()) {      // check input files
            this.messagesID.scrollTo(this.messagesID.getItems().size());
            this.setConfiguration();            // 計算条件を管理用に設定する.
            // Set count DNA barcode parameters.
            this.countBarcodeDna.setParameters(this.adminConfiguration, this.options);

            this.executeCountDnaBarcodeID.setDisable(true);
            this.stopCountBarcodeID.setDisable(false);

            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(new countDNAbarcode_thread(event));
            service.shutdown();
        }
    }

    /**
     * バーコードDNA配列のカウントを中止する.
     *
     * @param event イベント
     */
    @FXML
    private void stopCountBarcodeAction(ActionEvent event)  {
        boolean flag = this.countBarcodeDna.getService() != null && (!this.countBarcodeDna.getService().isShutdown());

        if (flag) { // running
            // Open dialogue
            ExecutionDialogue executionDialogue = new ExecutionDialogue();
            String executionMessage = "Stop count barcode DNA execution.";
            try {
                if (executionDialogue.open(executionMessage, "blue", this.mainPaneID)) {
                    this.countBarcodeDna.cancelFutures();
                    this.executeCountDnaBarcodeID.setDisable(false);
                    this.stopCountBarcodeID.setDisable(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.executeCountDnaBarcodeID.setDisable(false);
                this.stopCountBarcodeID.setDisable(true);
            }
        } else {
            try {
                String warningMessage = "Warning. " + ProgramVersion.PROGRAM + " is not running.";
                // Open warning message dialogue
                this.errorDialogue.open(warningMessage, "green", this.mainPaneID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.executeCountDnaBarcodeID.setDisable(false);
            this.stopCountBarcodeID.setDisable(true);
        }
    }

    /**
     * メッセージ出力欄をクリアする.
     *
     * @param even アクションイベント
     */
    @FXML
    private void clearMessagesAction(ActionEvent even) {
        this.messagesID.getItems().clear();
        for (int i = 0; i < this.MESSAGE_SCROLL; i++) {
            this.messagesID.getItems().add("");
        }
    }

    /**
     * メッセージ出力欄をファイルに保存する.
     *
     * @param even アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void saveMessagesAction(ActionEvent even) throws IOException {
        File folder = new File(this.outputFolderID.getText());
        if (!folder.exists()) {
            folder = new File(System.getProperty("user.home"));
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save messages");
        fc.setInitialDirectory(folder);
        String tstamp = CommonTools.SDFFILE.format(new Date());
        String fileName = CommonTools.serialFileName(folder.getPath() + CommonTools.SP + "Messages-" + tstamp + ".txt");
        fc.setInitialFileName(new File(fileName).getName());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Messages file", "*.txt"));

        File file = fc.showSaveDialog(this.mainPaneID.getScene().getWindow());
        if (file != null) {
            BufferedWriter bw = Files.newBufferedWriter(file.toPath());
            try (PrintWriter pw = new PrintWriter(bw)) {
                for (int i = 0; i < this.messagesID.getItems().size(); i++) {
                    pw.println(this.messagesID.getItems().get(i));
                }
            }
        }
    }

    //========================================================================//
    // [On Drag]
    //========================================================================//
    /**
     * Read1ファイルをドラッグする.
     *
     * @param event イベント
     */
    public void read1DragOver(DragEvent event) {
        if (!this.checkSelectFolderID.isSelected()) {
            List<String> exts = Arrays.asList(".*_R1_.*.fastq.gz");
            CommonTools.filesDragOver(event, exts);
        }
    }

    /**
     * Read1ファイルをドロップする.
     *
     * @param event アクションイベント
     */
    public void read1DragDropped(DragEvent event) {
        List<String> exts = Arrays.asList(".*_R1_.*.fastq.gz");
        CommonTools.filesDragDropped(event, exts, this.read1FilesID, this.read2FilesID);
    }

    //========================================================================//
    // MenuBar in Files [On Action]
    //========================================================================//
    /**
     * 計算条件設定ファイルをインポートする.
     *
     * @param even アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void importConfigAction(ActionEvent even) throws IOException {
        File file = CommonTools.selectConfigurationFile(this.stockSearchFolder, this.mainPaneID);
        if (file == null) {
            return;
        }

        int ret = this.userConfiguration.readConfigurationFile(file, this.mainPaneID);
        if (ret == 1) {
            this.getConfiguration();
        } else if (ret == 0) {
            String errorMessage = "Error. Configuration file read error.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
        } else {
            this.messagesID.getItems().add("Stop reading configuration file.");
        }
    }

    /**
     * 計算条件を設定ファイルにエクスポートする.
     *
     * @param even アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void exportConfigAction(ActionEvent even) throws IOException {
        File file = CommonTools.saveConfigurationFile(this.stockSearchFolder, this.mainPaneID);
        if (file == null) {
            return;
        }

        this.setConfiguration();        // 計算条件を管理用に設定する.
        boolean ret = this.userConfiguration.writeConfigurationFile(file);
        if (!ret) {
            String errorMessage = "Error. Configuration file write error.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
        }
    }

    /**
     * メインスクリーンを閉じてGUIを終了する.
     * @param even アクションイベント
     */
    @FXML
    private void closeAction(ActionEvent even) {
        Platform.exit();
    }

    //========================================================================//
    // MenuBar in Help [On Action]
    //========================================================================//
    /**
     * User guideを表示する.
     *
     * @param even アクションイベント
     */
    @FXML
    private void userGuideAction(ActionEvent even) {
        File file = new File(System.getProperty("user.dir"));
        String pdfPath = file.getPath() + CommonTools.SP + "barcodeDNA_UserGuide.pdf";
        Application app = new Application() {
            @Override
            public void start(Stage primaryStage) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        app.getHostServices().showDocument(pdfPath);
    }

    /**
     * プログラムの情報をダイアログで表示する.
     *
     * @param even アクションイベント
     * @throws IOException 例外処理
     */
    @FXML
    private void aboutAction(ActionEvent even) throws IOException {
        String information = ProgramVersion.PROGRAM + " " + ProgramVersion.VERSION;
        AboutDialogue aboutDialogue = new AboutDialogue();
        aboutDialogue.open(information, this.mainPaneID);
    }

    //========================================================================//
    // Private function
    //========================================================================//
    /**
     * バーコードDNA配列のカウント数を集計する前に設定を確認する.
     *
     * @return true:問題なし、false:問題あり
     * @throws IOException 例外処理
     */
    private boolean checkCollect() throws IOException {
        if (!CommonTools.checkInputText(this.barcodeDnaFileID)) {
            String errorMessage = "Barcode DNA file was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.read1FilesID)) {
            String errorMessage = "Read files were not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputFolderID)) {
            String errorMessage = "Output folder was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputPrefixID)) {
            String errorMessage = "Output prefix was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (this.spinnerMaxFlankID.getValue() < this.spinnerCollectFlankID.getValue()) {
            String errorMessage = "Max mismatches in [frank] must be greater than or equal to collect mismatches.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (this.spinnerMaxMidID.getValue() < this.spinnerCollectMidID.getValue()) {
            String errorMessage = "Max mismatches in [mid] must be greater than or equal to collect mismatches.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        // check MLIST csv file
        String basename = new File(this.barcodeDnaFileID.getText()).getName();
        String dbname = basename.substring(0,basename.lastIndexOf(".fa"));
        boolean check = false;
        for (String s : CommonTools.getTextArea(this.read1FilesID)) {
            String name = CommonTools.getFastqName(s);
            String csv = this.outputFolderID.getText() + CommonTools.SP
                    + name + CommonTools.SP + name + '.' + dbname + ".csv";
            File file = new File(csv);
            if (file.isFile() && file.canRead()) {
                check = true;
                break;
            }
        }

        if (!check) {
            String errorMessage = "Error. Barcode DNA has not been counted yet.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
        }
        return check;
    }

    /**
     * HTMLファイルを作成して表示する前に設定を確認する.
     *
     * @return true:問題なし、false:問題あり
     * @throws IOException 例外処理
     */
    private boolean checkDraw() throws IOException {
        if (!CommonTools.checkInputText(this.read1FilesID)) {
            String errorMessage = "Read files were not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputFolderID)) {
            String errorMessage = "Output folder was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputPrefixID)) {
            String errorMessage = "Output prefix was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        // check png file
        List<String> Pair = Arrays.asList("R1", "R2");
        List<String> Strand = Arrays.asList("forward", "reverse");
        List<String> reads1 = CommonTools.getTextArea(this.read1FilesID);
        boolean check = false;
        for (String s : reads1) {
            String name = CommonTools.getFastqName(s);
            for (int j = 0; j < Pair.size(); j++) {
                for (int k = 0; k < Strand.size(); k++) {
                    String img = CommonTools.getImgName(name, j, k);
                    String png = this.outputFolderID.getText() + CommonTools.SP + img;
                    File pngFile = new File(png);
                    if (pngFile.isFile() && pngFile.canRead()) {
                        check = true;
                        break;
                    }
                }
                if (check) {
                    break;
                }
            }
            if (check) {
                break;
            }
        }

        if (!check) {
            String errorMessage = "Error. Barcode DNA has not been counted yet.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
        }
        return check;
    }

    /**
     * バーコードDNA配列をカウントする前に設定を確認する.
     *
     * @return true:問題なし、false:問題あり
     * @throws IOException 例外処理
     */
    private boolean checkCountDNAbarcode() throws IOException {
        if (!CommonTools.checkInputText(this.barcodeDnaFileID)) {
            String errorMessage = "Barcode DNA file was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        } else {
            String barcodeDna = this.barcodeDnaFileID.getText();
            File fasta = new File(barcodeDna);
            if (!CommonTools.checkBlastdb(fasta)) {
                if (!CommonTools.makeblastdb(this.adminConfiguration, barcodeDna,
                        this.messagesID, this.mainPaneID)) {
                    return false;
                }
            }
        }

        if (!CommonTools.checkInputText(this.read1FilesID)) {
            String errorMessage = "Read files were not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputValue(this.evalueID)) {
            String errorMessage = "E-value was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputFolderID)) {
            String errorMessage = "Output folder was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (!CommonTools.checkInputText(this.outputPrefixID)) {
            String errorMessage = "Output prefix was not specified.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (this.spinnerMaxFlankID.getValue() < this.spinnerCollectFlankID.getValue()) {
            String errorMessage = "Max mismatches in [frank] must be greater than or equal to collect mismatches.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        if (this.spinnerMaxMidID.getValue() < this.spinnerCollectMidID.getValue()) {
            String errorMessage = "Max mismatches in [mid] must be greater than or equal to collect mismatches.";
            this.errorDialogue.open(errorMessage, "red", this.mainPaneID);
            return false;
        }

        return true;
    }

    /**
     * 計算条件を管理用に設定する.
     */
    private void setConfiguration() {
        // Search folder
        this.userConfiguration.setSearchFolder(this.stockSearchFolder);

        // fx:id
        this.options.setBarcodeDnaFile(this.barcodeDnaFileID);
        if (CommonTools.checkInputText(this.read1FilesID)) {
            this.options.setRead1Files(this.read1FilesID);
        }
        if (CommonTools.checkInputText(this.read2FilesID)) {
            this.options.setRead2Files(this.read2FilesID);
        }
        this.options.setCheckSelectFolder(this.checkSelectFolderID);
        this.options.setRemoveFirst(this.spinnerRemoveFirstID);
        this.options.setRemoveLast(this.spinnerRemoveLastID);
        this.options.setMaxFlank(this.spinnerMaxFlankID);
        this.options.setMaxMid(this.spinnerMaxMidID);
        this.options.setMinimumReadLength(this.spinnerMinimumReadLengthID);
        this.options.setEvalue(this.evalueID);
        this.options.setCollectFlank(this.spinnerCollectFlankID);
        this.options.setCollectMid(this.spinnerCollectMidID);
        this.options.setChoiceStrand(this.choiceStrandID);
        this.options.setOutputFolder(this.outputFolderID);
        this.options.setOutputPrefix(this.outputPrefixID);
        this.options.setNumberOfThreads(this.spinnerThreadsID);
        this.userConfiguration.setOptions(this.options);
    }

    /**
     * 計算条件を保存データから取得する.
     */
    private void getConfiguration() {
        // Search folder
        this.stockSearchFolder = this.searchFolder(this.userConfiguration.getSearchFolder());

        // fx:id
        this.options = this.userConfiguration.getOptions();
        this.setTextField(this.barcodeDnaFileID, this.options.getBarcodeDnaFile());
        CommonTools.setTextArea(this.read1FilesID, this.options.getRead1Files());
        CommonTools.setTextArea(this.read2FilesID, this.options.getRead2Files());
        this.checkSelectFolderID.setSelected(this.options.getCheckSelectFolder());
        this.setSpinner(this.spinnerRemoveFirstID, this.options.getRemoveFirst());
        this.setSpinner(this.spinnerRemoveLastID, this.options.getRemoveLast());
        this.setSpinner(this.spinnerMaxFlankID, this.options.getMaxFlank());
        this.setSpinner(this.spinnerMaxMidID, this.options.getMaxMid());
        this.setSpinner(this.spinnerMinimumReadLengthID, this.options.getMinimumReadLength());
        this.setTextField(this.evalueID, this.options.getEvalue());
        this.setSpinner(this.spinnerCollectFlankID, this.options.getCollectFlank());
        this.setSpinner(this.spinnerCollectMidID, this.options.getCollectMid());
        this.choiceStrandID.setValue(this.options.getChoiceStrand());
        this.setTextField(this.outputFolderID, this.options.getOutputFolder());
        this.setTextField(this.outputPrefixID, this.options.getOutputPrefix());
        this.setSpinner(this.spinnerThreadsID, this.options.getNumberOfThreads());
    }

    /**
     * 文字列をテキストフィールドに設定する.
     *
     * @param tf テキストフィールド
     * @param s 文字列
     */
    private void setTextField(TextField tf, String s) {
        if (s != null && s.length() > 0) {
            tf.setText(s);
        }
    }

    /**
     * 実数をテキストフィールドに設定する.
     *
     * @param tf テキストフィールド
     * @param d 実数
     */
    private void setTextField(TextField tf, double d) {
        if (d >= 0.0) {
            tf.setText(String.valueOf(d));
        }
    }

    /**
     * スピナーに整数を設定する.
     *
     * @param sp スピナー
     * @param i 設定値
     */
    private void setSpinner(Spinner<Integer> sp, int i) {
        if (i > 0) {
            sp.getValueFactory().setValue(i);
        }
    }

    /**
     * 検索フォルダーを取得する.
     *
     * @param s 検索フォルダーの候補
     * @return 検索フォルダー
     */
    private String searchFolder(String s) {
        if (s != null && s.length() > 0) {
            return s;
        } else {
            return System.getProperty("user.home");
        }
    }

    /**
     * バーコードDNAのカウント処理をスレッドに投入する.
     */
    private class countDNAbarcode_thread implements Callable {
        private ActionEvent event;
        countDNAbarcode_thread(ActionEvent event) {
            this.event = event;
        }
        @Override
        public Void call() {
            // Execute count barcode DNA
            String collectPath = countBarcodeDna.execution(messagesID, mainPaneID);
            executeCountDnaBarcodeID.setDisable(false);
            stopCountBarcodeID.setDisable(true);
            if (collectPath != null) {
                Platform.runLater( () -> {
                    try {
                        drawFiguresAction(event);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } );
            }
            return null;
        }
    }
}
