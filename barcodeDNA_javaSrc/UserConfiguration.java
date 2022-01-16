package barcode;

import javafx.scene.Node;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * ユーザーの計算条件を保存して再現する.
 */
public class UserConfiguration {
    //========================================================================//
    // Local class
    //========================================================================//
    /**
     * 計算オプションを管理する.
     */
    private Options options;

    //========================================================================//
    // Local parameter
    //========================================================================//
    /**
     * 検索フォルダー.
     */
    String searchFolder;

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * ユーザーの計算条件を保存して再現するクラスのコンストラクター.
     */
    public UserConfiguration() {
        this.options = new Options();
    }

    /**
     * 計算条件設定ファイルを読み込む.
     *
     * @param file 計算条件設定ファイル
     * @param node エラーメッセージ出力用の基準画面
     * @return 0:入力エラー、1:正常終了、2:読み込みの中断
     */
    public int readConfigurationFile(File file, Node node) {
        this.options.clearConfiguration();
        List<String> readList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            String str;
            while ((str = br.readLine()) != null) {
                readList.add(str);
            }

            for (String s : readList) {
                String[] data = s.split("\t");
                switch (data[0]) {
                    // Program version
                    case "PROGRAM_VERSION":
                        if (!data[1].equals(ProgramVersion.VERSION)) {
                            String warningMessage = "Warning. Version of configuration file does not match.";
                            ExecutionDialogue executionDialogue = new ExecutionDialogue();
                            if (!executionDialogue.open(warningMessage, "green", node)) {
                                return 2;
                            }
                        }
                        break;
                    // Search folder
                    case "SEARCH_FOLDER":
                        this.searchFolder = data[1];
                        break;
                    // About fx:id
                    case "BARCODE_DNA":
                        this.options.setBarcodeDnaFile(data[1]);
                        break;
                    case "READ1_FILES":
                        this.options.getRead1Files().add(data[1]);
                        break;
                    case "READ2_FILES":
                        this.options.getRead2Files().add(data[1]);
                        break;
                    case "SELECT_FOLDER":
                        this.options.setCheckSelectFolder(Boolean.parseBoolean(data[1]));
                        break;
                    case "REMOVE_FIRST_BASES":
                        this.options.setRemoveFirst(Integer.parseInt(data[1]));
                        break;
                    case "REMOVE_LAST_BASES":
                        this.options.setRemoveLast(Integer.parseInt(data[1]));
                        break;
                    case "MAX_FLANK_MISMATCHES":
                        this.options.setMaxFlank(Integer.parseInt(data[1]));
                        break;
                    case "MAX_MID_MISMATCHES":
                        this.options.setMaxMid(Integer.parseInt(data[1]));
                        break;
                    case "MINIMUM_READ_LENGTH":
                        this.options.setMinimumReadLength(Integer.parseInt(data[1]));
                        break;
                    case "EVALUE":
                        this.options.setEvalue(Double.parseDouble(data[1]));
                        break;
                    case "COLLECT_FLANK_MISMATCHES":
                        this.options.setCollectFlank(Integer.parseInt(data[1]));
                        break;
                    case "COLLECT_MID_MISMATCHES":
                        this.options.setCollectMid(Integer.parseInt(data[1]));
                        break;
                    case "STRAND":
                        this.options.setChoiceStrand(data[1]);
                        break;
                    case "OUTPUT_FOLDER":
                        this.options.setOutputFolder(data[1]);
                        break;
                    case "OUTPUT_PREFIX":
                        this.options.setOutputPrefix(data[1]);
                        break;
                    case "NUMBER_OF_THREADS":
                        this.options.setNumberOfThreads(Integer.parseInt(data[1]));
                        break;
                    default:
                        String warningMessage = "Warning. Tag of configuration file (" + data[0] + ") is illegal.";
                        ExecutionDialogue executionDialogue = new ExecutionDialogue();
                        if (!executionDialogue.open(warningMessage, "green", node)) {
                            return 2;
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    /**
     * 計算条件を設定ファイルに保存する.
     *
     * @param file 計算条件設定ファイル
     * @return true:正常終了、false:出力エラー
     */
    public boolean writeConfigurationFile(File file) {
        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath())) {
            PrintWriter pw = new PrintWriter(bw);
            // Program version
            pw.println("PROGRAM_VERSION\t" + ProgramVersion.VERSION);
            // Search folder
            pw.println("SEARCH_FOLDER\t" + this.searchFolder);
            // About fx:id
            String barcodeDnaFile = this.options.getBarcodeDnaFile();
            if (barcodeDnaFile != null && barcodeDnaFile.length() > 0) {
                pw.println("BARCODE_DNA\t" + barcodeDnaFile);
            }
            for (String s : this.options.getRead1Files()) {
                pw.println("READ1_FILES\t" + s);
            }
            for (String s : this.options.getRead2Files()) {
                pw.println("READ2_FILES\t" + s);
            }
            pw.println("SELECT_FOLDER\t" + this.options.getCheckSelectFolder());
            pw.println("REMOVE_FIRST_BASES\t" + this.options.getRemoveFirst());
            pw.println("REMOVE_LAST_BASES\t" + this.options.getRemoveLast());
            pw.println("MAX_FLANK_MISMATCHES\t" + this.options.getMaxFlank());
            pw.println("MAX_MID_MISMATCHES\t" + this.options.getMaxMid());
            pw.println("MINIMUM_READ_LENGTH\t" + this.options.getMinimumReadLength());
            if (this.options.getEvalue() >= 0.0) {
                pw.println("EVALUE\t" + this.options.getEvalue());
            }
            pw.println("COLLECT_FLANK_MISMATCHES\t" + this.options.getCollectFlank());
            pw.println("COLLECT_MID_MISMATCHES\t" + this.options.getCollectMid());
            pw.println("STRAND\t" + this.options.getChoiceStrand());
            String outputFolder = this.options.getOutputFolder();
            if (outputFolder != null && outputFolder.length() > 0) {
                pw.println( "OUTPUT_FOLDER\t" + outputFolder );
            }
            String outputPrefix = this.options.getOutputPrefix();
            if (outputPrefix != null && outputPrefix.length() > 0) {
                pw.println( "OUTPUT_PREFIX\t" + outputPrefix );
            }
            pw.println("NUMBER_OF_THREADS\t" + this.options.getNumberOfThreads());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //========================================================================//
    // Setter / Getter
    //========================================================================//
    /**
     * 検索フォルダーを設定する.
     *
     * @param s 検索フォルダー
     */
    public void setSearchFolder(String s) {
        if (s != null && s.length() > 0) {
            this.searchFolder = s;
        }
    }

    /**
     * 検索フォルダーを取得する.
     *
     * @return 検索フォルダー
     */
    public String getSearchFolder() {
        return this.searchFolder;
    }

    /**
     * 計算オプションを管理するクラスを設定する.
     *
     * @param opt 計算オプションを管理するクラス
     */
    public void setOptions(Options opt) {
        this.options = opt;
    }

    /**
     * 計算オプションを管理するクラスを取得する.
     *
     * @return 計算オプションを管理するクラス
     */
    public Options getOptions() {
        return this.options;
    }
}
