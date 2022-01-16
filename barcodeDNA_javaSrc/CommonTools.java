package barcode;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ユーティリティ関数を集めたクラス.
 */
public class CommonTools {
    //========================================================================//
    // Private constructor
    //========================================================================//
    /**
     * ユーティリティ関数を集めたクラスのコンストラクター.
     */
    private CommonTools() {     // prohibit the creation of an instance
    }

    //========================================================================//
    // Public data
    //========================================================================//
    /**
     * 改行コード.
     */
    public static final String BR = System.getProperty("line.separator");

    /**
     * Split関数用リターンコード.
     */
    public static final String BR2 = "(\\r\\n|\\r|\\n)";

    /**
     * カウント比用CSVファイルの拡張子.
     */
    public static final String PERCENT_CSV = ".percent.csv";

    /**
     * カウント数用CSVファイルの拡張子.
     */
    public static final String COUNTS_CSV = ".counts.csv";

    /**
     * セパレータ.
     */
    public static final String SP = File.separator;

    /**
     * メッセージ出力欄用タイムスタンプのフォーマット.
     */
    public static final SimpleDateFormat SDF = new SimpleDateFormat("(EEE, d MMM yyyy HH:mm:ss)", Locale.ENGLISH);

    /**
     * 一時ファイルのファイル名用タイムスタンプのフォーマット.
     */
    public static final SimpleDateFormat SDFVAL = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

    /**
     * 日付のフォーマット.
     */
    public static final SimpleDateFormat SDFFILE = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    /**
     * 排他制御用インスタンス.
     */
    public static final Object LOCK = new Object();

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * パスのwindows形式からlinux形式への変換。 入力がnullの場合はnullを返す。
     *
     * @param s Windows形式のパス
     * @return Linux形式のパス
     */
    public static String fromWindows2Linux(String s) {
        if (s == null) {
            return null;
        } else {
            return "/" + s.replaceAll("\\:", "").replaceAll("\\\\", "/");
        }
    }

    /**
     * Fastaファイルを選択する.
     *
     * @param target Fastaファイル用テキストフィールド
     * @param stockSearchFolder 検索ディレクトリ
     * @param node ファイル選択ダイアログの基準画面
     * @return 選択ファイルのディレクトリ
     */
    public static String selectFastaFile(TextField target, String stockSearchFolder, Node node) {
        FileChooser fc = new FileChooser();
        File searchFolder = new File(stockSearchFolder);
        fc.setTitle("Select barcode DNA file");
        fc.setInitialDirectory(searchFolder);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("FASTA file", "*.fa"));

        File file = fc.showOpenDialog(node.getScene().getWindow());
        if (file != null) {
            String filePath = file.getPath();
            target.setText(filePath);
            stockSearchFolder = file.getParent();
        }
        return stockSearchFolder;
    }

    /**
     * Fastqファイルのあるフォルダーを選択する
     *
     * @param read1 Read1ファイル用テキストエリア
     * @param read2 Read2ファイル用テキストエリア
     * @param stockSearchFolder 検索ディレクトリ
     * @param node ファイル選択ダイアログの基準画面
     * @return 選択ファイルのディレクトリ
     */
    public static String selectFastqFolder(TextArea read1, TextArea read2, String stockSearchFolder, Node node) {
        DirectoryChooser dc = new DirectoryChooser();
        File searchFolder = new File(stockSearchFolder);
        dc.setTitle("Select folder");
        dc.setInitialDirectory(searchFolder);

        File folder = dc.showDialog(node.getScene().getWindow());
        if (folder != null) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    if (file1.exists() && file1.isFile()) {
                        String file1Path = file1.getPath();
                        if (file1Path.endsWith(".fastq.gz") && file1Path.contains("_R1_")) {
                            if (file1.getName().startsWith("Undetermined")) {
                                continue;
                            }
                            CommonTools.setTextArea(read1, file1Path);
                            stockSearchFolder = file1.getParent();
                            String file2Path = file1Path.replace("_R1_", "_R2_");
                            File file2 = new File(file2Path);
                            if (file2.exists()) {
                                CommonTools.setTextArea(read2, file2Path);
                            }
                        }
                    }
                }
            }
        }
        return stockSearchFolder;
    }

    /**
     * Fastqファイルを選択する
     *
     * @param read1 Read1ファイル用テキストエリア
     * @param read2 Read2ファイル用テキストエリア
     * @param stockSearchFolder 検索ディレクトリ
     * @param node ファイル選択ダイアログの基準画面
     * @return 選択ファイルのディレクトリ
     */
    public static String selectFastqFiles(TextArea read1, TextArea read2, String stockSearchFolder, Node node) {
        FileChooser fc = new FileChooser();
        File searchFolder = new File(stockSearchFolder);
        fc.setInitialDirectory(searchFolder);
        fc.setTitle("Select read1 FASTQ files");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Read1 FASTQ files", "*_R1_*.fastq.gz"));

        List<File> files = fc.showOpenMultipleDialog(node.getScene().getWindow());
        if (files != null) {
            for (File file1 : files) {
                String file1Path = file1.getPath();
                CommonTools.setTextArea(read1, file1Path);
                stockSearchFolder = file1.getParent();
                String file2Path = file1Path.replace("_R1_", "_R2_");
                File file2 = new File(file2Path);
                if (file2.exists()) {
                    CommonTools.setTextArea(read2, file2Path);
                }
            }
        }
        return stockSearchFolder;
    }

    /**
     * テキストエリアにファイルを1つ格納する.
     *
     * @param target ファイル用テキストエリア
     * @param name ファイル名
     */
    public static void setTextArea(TextArea target, String name) {
        if (checkInputText(target)) {
            target.setText(target.getText() + BR + name);
        } else {
            target.setText(name);
        }
    }

    /**
     * テキストエリアにファイルリストを格納する.
     *
     * @param target ファイル用テキストエリア
     * @param names ファイルリスト
     */
    public static void setTextArea(TextArea target, List<String> names) {
        target.clear();
        for (String name : names) {
            setTextArea(target, name);
        }
    }

    /**
     * テキストエリアのファイルリストを取得する.
     *
     * @param target ファイル用テキストエリア
     * @return ファイルリスト
     */
    public static List<String> getTextArea(TextArea target) {
        return checkInputText(target) ? Arrays.asList(target.getText().split(CommonTools.BR2))
                : new ArrayList<>();
    }

    /**
     * ディレクトリを選択する.
     *
     * @param target フォルダー用テキストエリア
     * @param stockSearchFolder 検索フォルダー
     * @param node フォルダー選択ダイアログの基準画面
     * @return 選択フォルダーの上位フォルダー
     */
    public static String selectFolder(TextField target, String stockSearchFolder, Node node) {
        DirectoryChooser dc = new DirectoryChooser();
        File searchFolder = new File(stockSearchFolder);
        dc.setTitle("Select output folder");
        dc.setInitialDirectory(searchFolder);
        File folder = dc.showDialog(node.getScene().getWindow());

        if (folder != null) {
            String folderPath = folder.getPath();
            target.setText(folderPath);
            stockSearchFolder = folder.getParent();
        }
        return stockSearchFolder;
    }

    /**
     * 計算条件設定ファイルをインポートする.
     *
     * @param stockSearchFolder 検索ディレクトリ
     * @param node ファイル選択ダイアログの基準画面
     * @return 計算条件設定ファイル
     */
    public static File selectConfigurationFile(String stockSearchFolder, Node node) {
        FileChooser fc = new FileChooser();
        File searchFolder = new File(stockSearchFolder);
        fc.setTitle("Import configuration file");
        fc.setInitialDirectory(searchFolder);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration file", "*.conf"));
        return fc.showOpenDialog(node.getScene().getWindow());
    }

    /**
     * 計算条件設定ファイルをエクスポートする.
     *
     * @param stockSearchFolder 検索ディレクトリ
     * @param node ファイル保存ダイアログの基準画面
     * @return 計算条件設定ファイル
     */
    public static File saveConfigurationFile(String stockSearchFolder, Node node) {
        FileChooser fc = new FileChooser();
        File searchFolder = new File(stockSearchFolder);
        fc.setTitle("Export configuration file");
        fc.setInitialDirectory(searchFolder);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration file", "*.conf"));
        return fc.showSaveDialog(node.getScene().getWindow());
    }

    /**
     * テキストフィールドに数値が指定されていることを確認する.
     *
     * @param inputText 確認するテキストフィールド
     * @return true:数値が指定されている、false:数値が指定されていない
     */
    public static boolean checkInputValue(TextField inputText) {
        if (inputText.getText() != null && inputText.getText().length() > 0) {
            try {
                Double.parseDouble(inputText.getText());
                return true;
            } catch (NumberFormatException numberFormatException) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * テキストフィールドにファイルが格納されていることを確認する.
     *
     * @param inputText 確認するテキストフィールド
     * @return true:ファイルが格納されている、false:ファイルが格納されていない
     */
    public static boolean checkInputText(TextField inputText) {
        return inputText.getText() != null && inputText.getText().length() > 0;
    }

    /**
     * テキストエリアにファイルが格納されていることを確認する.
     *
     * @param inputText 確認するテキストエリア
     * @return true:ファイルが格納されている、false:ファイルが格納されていない
     */
    public static boolean checkInputText(TextArea inputText) {
        return inputText.getText() != null && inputText.getText().length() > 0;
    }

    /**
     * ディレクトリとその配下のファイルを全て削除する.
     *
     * @param file ディレクトリ
     */
    public static void recursiveDeleteFile(final File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File child : fileList) {
                    recursiveDeleteFile(child);
                }
            }
        }
        file.delete();
    }

    /**
     * 入力パターンに一致したファイルをフォルダー内から探しファイル名を返す.
     *
     * @param dir 検索フォルダー
     * @param regex ファイルの検索パターン
     * @return 最初に検出されたファイル名
     */
    public static String getPatternName(File dir, String regex) {
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] targetFiles = dir.listFiles();
        if (targetFiles != null) {
            Arrays.sort(targetFiles, Comparator.reverseOrder());
            for (File f : targetFiles) {
                String fileName = f.getName();
                if (fileName.endsWith(".b") || fileName.endsWith(".bak") || fileName.endsWith(".back")) {
                    continue;
                }
                if (fileName.startsWith(regex)) {
                    return fileName;
                }
            }
        }
        return null;
    }

    /**
     * 集計用CSVファイルのprefixを決める.
     *
     * @param options 計算オプションを管理する
     * @return 集計用CSVファイルのprefix
     */
    public static String collectCsvPrefix(Options options) {
        String name = options.getOutputFolder() + CommonTools.SP + options.getOutputPrefix()
                + "." + options.getChoiceStrand() + "." + CommonTools.SDFFILE.format(new Date());
        return CommonTools.serialFilePrefix(name);
    }

    /**
     * 良く利用されるPATHをシェルスクリプトに出力する.
     *
     * @param ac 解析環境を設定する
     * @param pw 出力スクリプトファイル
     */
    public static void commonScript(AdminConfiguration ac, PrintWriter pw) {
        // MSYS2 bash directory
        pw.println("export PATH=" + ac.getBashHome() + ":$PATH");

        // NCBI blast+ directory
        pw.println("export PATH=" + ac.getBlastHome() + ":$PATH");

        // R directory
        pw.println("export PATH=" + ac.getRHome() + ":$PATH");

        // bin directory
        pw.println("export PATH=" + ac.getBinHome() + ":$PATH");

        // Scripts directory
        pw.println("export PATH=" + ac.getScriptsHome() + ":$PATH");

        // Perl directory
        pw.println("export PATH=" + ac.getPerlHome() + ":$PATH");

        // Perl directory
        pw.println("export PERL5LIB=" + ac.getScriptsHome());

        // LANG
        pw.println("export LANG=en_US.utf8");
    }

    /**
     * プロセスID名のフォルダーを作成する.
     *
     * @param process 実行プロセス
     * @param pw 出力ファイル
     * @param outFolder 作成先上位フォルダー
     * @return プロセスID名のフォルダー
     */
    public static String writeProcessId(Process process, PrintWriter pw, String outFolder) {
        // process
        String pidDir = outFolder + SP + process.pid();
        String pidDirLinux = fromWindows2Linux(pidDir);
        pw.println("mkdir -p " + pidDirLinux);
        pw.println("touch " + pidDirLinux + "/$$");
        return pidDir;
    }

    /**
     * プロセスの停止処理を行う.
     *
     * @param ac 解析環境を設定する
     * @param rootPidDir プロセスID名のあるフォルダーの上位フォルダー
     */
    public static void stopProcess(AdminConfiguration ac, String rootPidDir) {
        File pidDir = new File(rootPidDir);
        File pidList = new File(rootPidDir + ".list");
        File[] files = pidDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String gpid = f.getName();
                    ProcessBuilder pb = new ProcessBuilder();
                    pb.command(ac.getBashHomeWindows() + SP + "bash.exe", ac.getBinHomeWindows() + SP + "getPidFromPgid.sh",
                            gpid, fromWindows2Linux(pidList.getPath()), ac.getBashHome());
                    try {
                        Process process = pb.start();
                        process.waitFor();
                        BufferedReader br = Files.newBufferedReader(pidList.toPath());
                        String id;
                        while ((id = br.readLine()) != null) {
                            ProcessBuilder kp = new ProcessBuilder();
                            kp.command(ac.getBashHomeWindows() + SP + "kill.exe", id);
                            kp.start();
                        }
                        br.close();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        CommonTools.recursiveDeleteFile(pidDir);
        pidList.delete();
    }

    /**
     * ファイルの存在を確認し必要に応じて連番を振る.
     *
     *
     * @param s 入力ファイル名
     * @return 出力ファイル名
     */
    public static String serialFileName(String s) {
        if (new File(s).exists()) {
            int dot = s.lastIndexOf('.');
            if (dot == -1) {
                return s;   // over write
            }
            String base = s.substring(0, dot);
            String ext = s.substring(dot + 1);
            for (int i = 1;; i++) {
                File sadd = new File(base + "_" + i + "." + ext);
                if (!sadd.exists()) {
                    return sadd.getPath();
                }
            }
        } else {
            return s;
        }
    }

    /**
     * ファイルの存在を確認し必要に応じてprefixに連番を振る.
     *
     * @param s 入力ファイルのprefix
     * @return 出力ファイルのprefix
     */
    public static String serialFilePrefix(String s) {
        String fullPath = s + CommonTools.PERCENT_CSV;
        if (new File(fullPath).exists()) {
            for (int i = 1;; i++) {
                File sCsv = new File(s + "_" + i + CommonTools.PERCENT_CSV);
                if (!sCsv.exists()) {
                    return s + "_" + i;
                }
            }
        } else {
            return s;
        }
    }

    /**
     * 一時ファイルを作成する.
     *
     * @param tag ファイル名の先頭文字列
     * @param ext ファイル名の末尾の文字列
     * @param folder 上位フォルダー
     * @param dateStart ファイル名に含む時刻
     * @return 一時ファイル
     */
    public static File createTempFile(String tag, String ext, String folder, Date dateStart) {
        String startTime = SDFVAL.format(dateStart);
        int SEED = 1000000000;
        File tempFile;
        for (;;) {
            int add = (int)(Math.random() * SEED) + SEED;
            tempFile = new File(folder + SP + tag + startTime + "_" + add + ext);
            if (!tempFile.exists()) {
                break;
            }
        }
        return tempFile;
    }

    /**
     * Fastqファイルのサンプル名部分を取得する.
     *
     * @param fastq Fastqファイルのパス
     * @return サンプル名
     */
    public static String getFastqName(String fastq) {
        String first = new File(fastq).getName();
        String second = first.substring(0, first.lastIndexOf(".gz"));
        String third = second.substring(0, second.lastIndexOf(".fastq"));
        return third.replaceAll("_S[0-9]+_L[0-9][0-9][0-9]_R.*", "");
    }

    /**
     * PNGファイルの相対パスを取得する.
     *
     * @param name サンプル名
     * @param ipair Pairのインデックス(0 or 1)
     * @param istrand SubTypesのインデックス(0 or 1)
     * @return PNGファイルの相対パス
     */
    public static String getImgName(String name, int ipair, int istrand) {
        final List<String> Pair = Arrays.asList("R1", "R2");
        final List<String> SubTypes = Arrays.asList("SeqCount", "RevCount");
        return name + CommonTools.SP + name + '_' + Pair.get(ipair) + '.' + SubTypes.get(istrand)  + ".png";
    }

    /**
     * BLAST+データベースを確認する.
     *
     * @param fasta Fastaファイル
     * @return true:データベースを作成する、false:新しいデータベースが存在する
     */
    public static boolean checkBlastdb(File fasta) {
        long fastaTime = fasta.lastModified();
        String path = fasta.getPath();
        List<String> exts = Arrays.asList("nhd", "nhi", "nhr", "nin", "nog", "nsd", "nsi", "nsq");
        for (String s : exts) {
            String dbpath = path + "." + s;
            File dbfile = new File(dbpath);
            if (dbfile.exists() && dbfile.isFile()) {
                if (dbfile.lastModified() < fastaTime) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * BLAST+データベースを作成する.
     *
     * @param ac 解析環境を設定する
     * @param barcodeDna バーコードDNAファイル
     * @param message メッセージ出力欄
     * @param node ダイアログ表示のための基準画面
     * @return true:成功、false:失敗
     */
    public static boolean makeblastdb(AdminConfiguration ac, String barcodeDna,
                                      ListView<String> message, Node node) {
        // Output start time date in Message area
        Date dateStart = new Date();
        // Message
        String calType = "Processing of makeblastdb";
        message.getItems().add(SDF.format(dateStart) + "  " + calType + " start ....");

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(ac.getBlastHomeWindows() + SP
                + "makeblastdb.exe", "-in", barcodeDna, "-dbtype", "nucl", "-hash_index");
        Process process;
        ErrorDialogue errorDialogue = new ErrorDialogue();
        try {
            process = pb.start();
            // Output message
            backgroundMessage(process, message);
            process.waitFor();
            process.destroy();
            // End message
            endMessage(calType, message);
        } catch (IOException | InterruptedException e) {
            String errorMessage = "Processing of makeblastdb failed.";
            try {
                errorDialogue.open(errorMessage, "red", node);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * ファイルをドラッグする.
     *
     * @param event イベント
     * @param exts ファイルの拡張子
     */
    public static void filesDragOver(DragEvent event, List<String> exts) {
        boolean success = false;
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            for (File file : files) {
                for (String s : exts) {
                    if (file.getName().matches(s)) {
                        success = true;
                        break;
                    }
                }
                if (success) {
                    break;
                }
            }
            if (success) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }

    /**
     * ファイルをドロップする.
     *
     * @param event イベント
     * @param exts ファイル名のパターン
     * @param read1 Read1のTextArea
     * @param read2 Read2のTextArea
     */
    public static void filesDragDropped(DragEvent event, List<String> exts, TextArea read1, TextArea read2) {
        boolean success = false;
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            for (File file1 : files) {
                if (file1.isFile()) {
                    for (String s : exts) {
                        String file1Path = file1.getPath();
                        if (file1Path.matches(s)) {
                            setTextArea(read1, file1Path);
                            success = true;
                            String file2Path = file1Path.replace("_R1_", "_R2_");
                            File file2 = new File(file2Path);
                            if (file2.exists()) {
                                setTextArea(read2, file2Path);
                            }
                            break;
                        }
                    }
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * 外部プロセスから出力されるメッセージを表示する.
     *
     * @param process 実行プロセス
     * @param message メッセージ出力欄
     */
    public static void backgroundMessage(Process process, ListView<String> message) {
        InputStream is = process.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                synchronized (LOCK) {
                    message.getItems().add(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommonTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 投入スレッド内でエラーダイアログを表示する.
     *
     * @param message メッセージ
     * @param color red:エラー、green:ワーニング、blue:メッセージ
     * @param node ダイアログ表示のための基準画面
     */
    public static void runTimeErrorMessage(String message, String color, Node node) {
        try {
            CommonTools.FxUtils.getFromApplicationThread(() -> {
                ErrorDialogue errorDialogue = new ErrorDialogue();
                errorDialogue.open(message, color, node);
                return null;
            });
        } catch (Exception ex) {
            Logger.getLogger(CommonTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 計算の終了メッセージを経過時刻と共に表示する.
     *
     * @param calType 計算のタイプ
     * @param begin 計算開始時刻
     * @param message メッセージ出力欄
     */
    public static void endMessage(String calType, final long begin, ListView<String> message) {
        // End time and calculation type
        Date dateEnd = new Date();
        message.getItems().add(SDF.format(dateEnd) + "  End of " + calType);

        // Elapsed time
        long now = System.currentTimeMillis();
        long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(now - begin);
        String elapsedString = "Elapsed time = " + elapsedTime + " sec.";
        message.getItems().add(elapsedString);
    }

    /**
     * 計算の終了メッセージを表示する.
     *
     * @param calType 計算のタイプ
     * @param message メッセージ出力欄
     */
    public static void endMessage(String calType, ListView<String> message) {
        // End time and calculation type
        Date dateEnd = new Date();
        message.getItems().add(SDF.format(dateEnd) + "  End of " + calType);
    }

    //========================================================================//
    // Private function
    //========================================================================//
    /**
     * 非同期処理を行う.
     */
    private static class FxUtils {
        /**
         * アプリケーションスレッドに処理を戻す.
         *
         * @param callable マルチスレッドCallableインターフェイス
         * @param <V> 継承元の型
         * @return スレッドの戻り値
         * @throws Exception 例外処理
         */
        public static <V> V getFromApplicationThread(Callable<? extends V> callable) throws Exception {
            if (Platform.isFxApplicationThread()) {
                return callable.call();
            }
            RunnableFuture<V> future = new FutureTask(callable);
            Platform.runLater(future);
            return future.get();
        }
    }
}
