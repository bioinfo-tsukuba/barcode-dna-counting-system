package barcode;

import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * 計算オプションを管理する.
 */
public class Options {
    //========================================================================//
    // Local parameter
    //========================================================================//
    // About fx:id
    private String barcodeDnaFile;          // Barcode DNA file
    private List<String> read1Files;        // Read1 FASTQ files
    private List<String> read2Files;        // Read2 FASTQ files
    private boolean checkSelectFolder;      // Select folder
    private int removeFirst;                // Remove first bases
    private int removeLast;                 // Remove last bases
    private int maxFlank;                   // Max flank mismatches
    private int maxMid;                     // Max mid mismatches
    private int minimumReadLength;          // Minimum read length
    private double evalue;                  // E-value
    private int collectFlank;               // Collect flank mismatches
    private int collectMid;                 // Collect mid mismatches
    private String choiceStrand;            // Strand
    private String outputFolder;            // Output folder
    private String outputPrefix;            // Output prefix
    private int numberOfThreads;            // Number of threads

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * 計算オプションを管理するクラスのコンストラクター.
     */
    public Options() {
        this.read1Files = new ArrayList<>();
        this.read2Files = new ArrayList<>();
    }

    /**
     * 計算条件の内容をクリアする.
     */
    public void clearConfiguration() {
        // About fx:id
        this.barcodeDnaFile = null;
        this.read1Files = new ArrayList<>();
        this.read2Files = new ArrayList<>();
        this.checkSelectFolder = true;
        this.removeFirst = 3;
        this.removeLast = 0;
        this.maxFlank = 2;
        this.maxMid = 1;
        this.minimumReadLength = 20;
        this.evalue = 1.0e-5;
        this.collectFlank = 2;
        this.collectMid = 1;
        this.choiceStrand = "forward";
        this.outputFolder = null;
        this.outputPrefix = null;
        this.numberOfThreads = 8;
    }

    //========================================================================//
    // Setter / Getter
    //========================================================================//
    // Barcode DNA sequence
    public void setBarcodeDnaFile(String s) {
        this.barcodeDnaFile = s;
    }
    public void setBarcodeDnaFile(TextField tf) {
        if (tf.getText().length() > 0) {
            this.barcodeDnaFile = tf.getText();
        }
    }
    public String getBarcodeDnaFile() {
        return this.barcodeDnaFile;
    }

    // Read1 FASTQ files
    public void setRead1Files(TextArea ta) {
        this.read1Files = CommonTools.getTextArea(ta);
    }
    public List<String> getRead1Files() {
        return this.read1Files;
    }

    // Read2 FASTQ files
    public void setRead2Files(TextArea ta) {
        this.read2Files = CommonTools.getTextArea(ta);
    }
    public List<String> getRead2Files() {
        return this.read2Files;
    }

    // Select folder
    public void setCheckSelectFolder(boolean b) {
        this.checkSelectFolder = b;
    }
    public void setCheckSelectFolder(CheckBox cb) {
        this.checkSelectFolder = cb.isSelected();
    }
    public boolean getCheckSelectFolder() {
        return this.checkSelectFolder;
    }

    // Remove first bases
    public void setRemoveFirst(int i) {
        this.removeFirst = i;
    }
    public void setRemoveFirst(Spinner<Integer> sp) {
        this.removeFirst = this.spinnerValue(sp);
    }
    public int getRemoveFirst() {
        return this.removeFirst;
    }

    // Remove last bases
    public void setRemoveLast(Spinner<Integer> sp) {
        this.removeLast = this.spinnerValue(sp);
    }
    public void setRemoveLast(int i) {
        this.removeLast = i;
    }
    public int getRemoveLast() {
        return this.removeLast;
    }

    // Max flank mismatches
    public void setMaxFlank(int i) {
        this.maxFlank = i;
    }
    public void setMaxFlank(Spinner<Integer> sp) {
        this.maxFlank = this.spinnerValue(sp);
    }
    public int getMaxFlank() {
        return this.maxFlank;
    }

    // Max mid mismatches
    public void setMaxMid(int i) {
        this.maxMid = i;
    }
    public void setMaxMid(Spinner<Integer> sp) {
        this.maxMid = this.spinnerValue(sp);
    }
    public int getMaxMid() {
        return this.maxMid;
    }

    // Minimum read length
    public void setMinimumReadLength(int i) {
        this.minimumReadLength = i;
    }
    public void setMinimumReadLength(Spinner<Integer> sp) {
        this.minimumReadLength = this.spinnerValue(sp);
    }
    public int getMinimumReadLength() {
        return this.minimumReadLength;
    }

    // E-value
    public void setEvalue(double d) {
        this.evalue = d;
    }
    public void setEvalue(TextField tf) {
        if (tf.getText().length() > 0) {
            this.evalue = Double.parseDouble(tf.getText());
        }
    }
    public double getEvalue() {
        return this.evalue;
    }

    // Collect flank mismatches
    public void setCollectFlank(int i) {
        this.collectFlank = i;
    }
    public void setCollectFlank(Spinner<Integer> sp) {
        this.collectFlank = this.spinnerValue(sp);
    }
    public int getCollectFlank() {
        return this.collectFlank;
    }

    // Collect mid mismatches
    public void setCollectMid(int i) {
        this.collectMid = i;
    }
    public void setCollectMid(Spinner<Integer> sp) {
        this.collectMid = this.spinnerValue(sp);
    }
    public int getCollectMid() {
        return this.collectMid;
    }

    // Strand
    public void setChoiceStrand(String s) {
        this.choiceStrand = s;
    }
    public void setChoiceStrand(ChoiceBox<String> cb) {
        this.choiceStrand = cb.getValue();
    }
    public String getChoiceStrand() {
        return this.choiceStrand;
    }

    // Output folder
    public void setOutputFolder(String s) {
        this.outputFolder = s;
    }
    public void setOutputFolder(TextField tf) {
        if (tf.getText().length() > 0) {
            this.outputFolder = tf.getText();
        }
    }
    public String getOutputFolder() {
        return this.outputFolder;
    }

    // Output prefix
    public void setOutputPrefix(String s) {
        this.outputPrefix = s;
    }
    public void setOutputPrefix(TextField tf) {
        if (tf.getText().length() > 0) {
            this.outputPrefix = tf.getText();
        }
    }
    public String getOutputPrefix() {
        return this.outputPrefix;
    }

    // Number of threads
    public void setNumberOfThreads(int i) {
        this.numberOfThreads = i;
    }
    public void setNumberOfThreads(Spinner<Integer> sp) {
        this.numberOfThreads = this.spinnerValue(sp);
    }
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    //========================================================================//
    // Private function
    //========================================================================//
    /**
     * スピナー値の範囲を考慮して値を取得する.
     *
     * @param sp スピナー
     * @return スピナーの値
     */
    private int spinnerValue(Spinner<Integer> sp) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory)sp.getValueFactory();
        return min(max(sp.getValue(), factory.getMin()), factory.getMax());
    }
}
