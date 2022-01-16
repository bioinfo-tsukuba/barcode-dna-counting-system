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
 * バーコードDNA配列をカウントする.
 */
public class CountBarcodeDna {
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
    private ExecutorService service;        // ExecutorService
    private final List<Process> processes;  // Background process
    private final List<String> pidDirs;     // PID folder
    private final List<Future<?>> futures;  // Future
    private String collectCsv;              // 集計データファイル
    private File shellFile;                 // 実行用shell

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * バーコードDNA配列をカウントするクラスのコンストラクター.
     */
    public CountBarcodeDna() {
        // Local data
        this.processes = new ArrayList<>();
        this.pidDirs = new ArrayList<>();
        this.futures = new ArrayList<>();
    }

    /**
     * バーコードDNA配列をカウントするためのパラメーターを設定する.
     *
     * @param ac 解析環境を設定するクラス
     * @param opt 計算オプションを管理するクラス
     */
    public void setParameters(AdminConfiguration ac, Options opt) {
        // Parameters
        this.adminConfiguration = ac;
        this.options = opt;
    }

    /**
     * バーコードDNA配列をカウントする.
     *
     * @param message メッセージ出力欄
     * @param node メッセージを出力するための基準画面
     * @return 集計データファイル
     */
    public String execution(ListView<String> message, Node node)  {
        // Output start time date in Message area
        Date dateStart = new Date();
        long begin = System.currentTimeMillis();

        String calType = ProgramVersion.PROGRAM;
        this.collectCsv = null;

        // prepare calculation
        this.prepareExecution(calType, dateStart, message);

        // Asynchronous thread start
        Callable<Void> task = () -> {
            // Count DNA barcode
            int iret = this.task(dateStart, message);
            if (iret == 1) {
                String errorMessage = "Error. " + calType
                        + " process has abnormally terminated.\nPlease check your input files.";
                CommonTools.runTimeErrorMessage(errorMessage, "red", node);
                return null;
            } else if (iret == 2) {
                String errorMessage = calType + " process has stopped.";
                CommonTools.runTimeErrorMessage(errorMessage, "blue", node);
                return null;
            }

            // End message
            CommonTools.endMessage(calType, begin, message);

            if (this.collectCsv != null) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "excel.exe",
                        this.collectCsv + CommonTools.PERCENT_CSV);
                pb.start();
                ProcessBuilder pb2 = new ProcessBuilder("cmd", "/c", "start", "excel.exe",
                        this.collectCsv + CommonTools.COUNTS_CSV);
                pb2.start();
            } else {
                String errorMessage = "Error. Collect mismatches csv file was not created.";
                message.getItems().add(errorMessage);
            }
            return null;
        };
        this.service = Executors.newSingleThreadExecutor();
        this.futures.add(this.service.submit(task));
        for (Future<?> future : this.futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        this.service.shutdown();
        return this.collectCsv;
    }

    /**
     * 実行中のスレッドのExecutorServiceクラスを取得する.
     *
     * @return 実行中のスレッドのExecutorServiceクラス
     */
    public ExecutorService getService() {
        return this.service;
    }

    /**
     * スレッドを終了する.
     */
    public void cancelFutures() {
        for (Future<?> future : this.futures) {
            if (future != null) {
                future.cancel(true);
            }
        }
        this.futures.clear();
        for (Process process : this.processes) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        this.processes.clear();
        this.service.shutdownNow();

        if (this.shellFile != null && this.shellFile.isFile()) {
            this.shellFile.delete();
        }
        for (String piddir : this.pidDirs) {
            CommonTools.stopProcess(this.adminConfiguration, piddir);
        }
        this.pidDirs.clear();
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
        // clear process
        this.processes.clear();
        this.pidDirs.clear();
        this.futures.clear();
        // Message
        message.getItems().add(CommonTools.SDF.format(dateStart) + "  " + calType + " start ....");
    }

    /**
     * 計算を実行する.
     *
     * @param dateStart 計算開始時刻
     * @param message メッセージ出力欄
     * @return 0:正常終了、1:異常終了、2:中断
     */
    private int task(Date dateStart, ListView<String> message) {
        try {
            // Script file
            this.shellFile = CommonTools.createTempFile(this.options.getOutputPrefix(), ".sh",
                    this.options.getOutputFolder(), dateStart);

            // Background process
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.command(this.adminConfiguration.getBashHomeWindows() + CommonTools.SP + "bash.exe",
                    this.shellFile.getPath());
            Process process = pb.start();
            this.processes.add(process);

            // Create script file
            boolean ret = this.createScript(process);
            if (!ret) {
                return 1;
            }

            // Output message
            CommonTools.backgroundMessage(process, message);
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(CountBarcodeDna.class.getName()).log(Level.SEVERE, null, ex);
                return 2;
            }
            int iret = process.exitValue();
            process.destroy();
            for (String piddir : this.pidDirs) {
                CommonTools.recursiveDeleteFile(new File(piddir));
            }
            this.shellFile.delete();
            if (iret == 1) {
                return 1;
            } else {
                return 0;
            }
        } catch (IOException ex) {
            Logger.getLogger(CountBarcodeDna.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
    }

    /**
     * 実行用シェルファイルを作成する.
     *
     * @param process バックグラウンドプロセス
     * @return true:正常終了、false:異常終了
     */
    private boolean createScript(Process process) {
        try (BufferedWriter bw = Files.newBufferedWriter(this.shellFile.toPath());
             PrintWriter pw = new PrintWriter(bw)) {
            CommonTools.commonScript(this.adminConfiguration, pw);

            // process
            this.pidDirs.add(CommonTools.writeProcessId(process, pw, this.options.getOutputFolder()));

            // 集計用CSVファイルのprefix
            this.collectCsv = CommonTools.collectCsvPrefix(this.options);

            // change directory
            pw.println("cd " + CommonTools.fromWindows2Linux(this.options.getOutputFolder()));

            pw.print("countDNAbarcodeDir.sh");
            pw.print(" -t " + this.options.getRemoveFirst());
            pw.print(" -b " + this.options.getRemoveLast());
            pw.print(" -f " + this.options.getMaxFlank());
            pw.print(" -m " + this.options.getMaxMid());
            pw.print(" -l " + this.options.getMinimumReadLength());
            pw.print(" -p " + this.options.getNumberOfThreads());
            pw.print(" -e " + this.options.getEvalue());
            pw.print(" -s " + this.options.getChoiceStrand().charAt(0));
            pw.print(" -x " + this.options.getCollectFlank());
            pw.print(" -y " + this.options.getCollectMid());
            pw.print(" " + CommonTools.fromWindows2Linux(this.options.getBarcodeDnaFile()));
            pw.print(" " + new File(this.collectCsv).getName());

            for (String s : this.options.getRead1Files()) {
                pw.print(" " + CommonTools.fromWindows2Linux(s));
            }
            pw.println(" || exit 1");
        } catch (IOException ex) {
            Logger.getLogger(CountBarcodeDna.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
