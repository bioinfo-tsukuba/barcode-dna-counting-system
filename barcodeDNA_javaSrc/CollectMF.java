package barcode;

import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * バーコードDNA配列のカウント数を集計する.
 */
public class CollectMF {
    //========================================================================//
    // Local class
    //========================================================================//
    /**
     * 解析環境を設定する.
     */
    private AdminConfiguration adminConfiguration;

    /**
     * 計算オプションを管理する.
     */
    private Options options;

    //========================================================================//
    // Local data
    //========================================================================//
    private final List<Future<?>> futures;  // Future
    private String collectCsv;              // 集計データファイル

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * バーコードDNA配列のカウント数を集計するクラスのコンストラクター.
     */
    public CollectMF() {
        // Local data
        this.futures = new ArrayList<>();
    }

    /**
     * バーコードDNA配列のカウント数を集計ためのパラメータを設定する.
     *
     * @param ac 解析環境を設定するクラス.
     * @param opt 計算オプションを管理するクラス.
     */
    public void setParameters(AdminConfiguration ac, Options opt) {
        // Parameters
        this.adminConfiguration = ac;
        this.options = opt;
    }

    /**
     * バーコードDNA配列のカウント数を集計する.
     *
     * @param message メッセージ出力欄
     * @param node メッセージを出力するための基準画面.
     */
    public void execution(ListView<String> message, Node node) {
        // Output start time date in Message area
        Date dateStart = new Date();

        String calType = "Collect mismatches";
        this.collectCsv = null;

        // prepare calculation
        this.prepareExecution(calType, dateStart, message);

        // Asynchronous thread start
        Callable<Void> task = () -> {
            // Count DNA barcode
            int iret = this.task(dateStart, message);
            if (iret != 0) {
                String errorMessage = "Error. " + calType
                        + " process has abnormally terminated. \nPlease check your input files.";
                CommonTools.runTimeErrorMessage(errorMessage, "red", node);
                return null;
            }

            // End message
            CommonTools.endMessage(calType, message);

            if (this.collectCsv != null) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "excel.exe",
                        this.collectCsv + CommonTools.PERCENT_CSV);
                pb.start();
                ProcessBuilder pb2 = new ProcessBuilder("cmd", "/c", "start", "excel.exe",
                        this.collectCsv + CommonTools.COUNTS_CSV);
                pb2.start();
            } else {
                String errorMessage = "Error. " + calType + " csv file was not created.";
                message.getItems().add(errorMessage);
                // open folder
                Runtime rt = Runtime.getRuntime();
                rt.exec("explorer " + this.options.getOutputFolder());
            }
            return null;
        };
        ExecutorService service = Executors.newSingleThreadExecutor();
        this.futures.add(service.submit(task));
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        service.shutdown();
    }

    //========================================================================//
    // Private function
    //========================================================================//
    /**
     * 計算実行の準備を行う.
     *
     * @param calType 計算タイプ
     * @param dateStart 計算開始時刻
     * @param message メッセージ出力欄
     */
    private void prepareExecution(String calType, Date dateStart, ListView<String> message) {
        // clear futures
        this.futures.clear();
        // Message
        message.getItems().add(CommonTools.SDF.format(dateStart) + "  " + calType + " start ....");
    }

    /**
     * 計算を実行する.
     *
     * @param dateStart 計算開始時刻
     * @param message メッセージ出力欄
     * @return 0:正常終了、1:異常終了
     */
    private int task(Date dateStart, ListView<String> message) {
        try {
            // Script file
            File shellFile = CommonTools.createTempFile(this.options.getOutputPrefix() + "_collect", ".sh",
                    this.options.getOutputFolder(), dateStart);

            // Background process
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.command(this.adminConfiguration.getBashHomeWindows() + CommonTools.SP + "bash.exe",
                    shellFile.getPath());
            Process process = pb.start();

            // Create script file
            boolean ret = this.createScript(shellFile);
            if (!ret) {
                return 1;
            }

            // Output message
            CommonTools.backgroundMessage(process, message);
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(CollectMF.class.getName()).log(Level.SEVERE, null, ex);
                return 1;
            }
            int iret = process.exitValue();
            process.destroy();
            shellFile.delete();
            return iret;
        } catch (IOException ex) {
            Logger.getLogger(CollectMF.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
    }

    /**
     * 実行用シェルファイルを作成する.
     *
     * @param shellFile 実行用シェルファイル
     * @return true:正常終了、false:異常終了
     */
    private boolean createScript(File shellFile) {
        String basename = new File(this.options.getBarcodeDnaFile()).getName();
        String dbname = basename.substring(0,basename.lastIndexOf(".fa"));

        String mlist = "";
        for (String s : this.options.getRead1Files()) {
            String name = CommonTools.getFastqName(s);
            String csv = this.options.getOutputFolder() + CommonTools.SP + name + CommonTools.SP + name + '.' + dbname + ".csv";
            File file = new File(csv);
            if (file.isFile() && file.canRead()) {
                mlist += ' ' + name + '/' + name + '.' + dbname + ".csv";
            }
        }
        if (mlist.length() == 0) {
            return false;
        }

        try (BufferedWriter bw = Files.newBufferedWriter(shellFile.toPath());
             PrintWriter pw = new PrintWriter(bw)) {
            CommonTools.commonScript(this.adminConfiguration, pw);

            // 集計用CSVファイルのprefix
            this.collectCsv = CommonTools.collectCsvPrefix(this.options);

            // change directory
            pw.println("cd " + CommonTools.fromWindows2Linux(this.options.getOutputFolder()));

            // for percent.csv
            pw.print("collectMFcsv.pl -t Prop");
            pw.print(" -x " + this.options.getCollectFlank());
            pw.print(" -y " + this.options.getCollectMid());
            pw.print(" -s " + this.options.getChoiceStrand().charAt(0));
            pw.print(" -fa " + CommonTools.fromWindows2Linux(this.options.getBarcodeDnaFile()));
            pw.println(mlist + " > " + new File(this.collectCsv + CommonTools.PERCENT_CSV).getName() + " || exit 1");

            // for counts.csv
            pw.print("collectMFcsv.pl -t Count");
            pw.print(" -x " + this.options.getCollectFlank());
            pw.print(" -y " + this.options.getCollectMid());
            pw.print(" -s " + this.options.getChoiceStrand().charAt(0));
            pw.print(" -fa " + CommonTools.fromWindows2Linux(this.options.getBarcodeDnaFile()));
            pw.println(mlist + " > " + new File(this.collectCsv + CommonTools.COUNTS_CSV).getName() + " || exit 1");
        } catch (IOException ex) {
            Logger.getLogger(CollectMF.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
