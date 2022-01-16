package barcode;

import java.io.File;

/**
 * 解析環境を設定する.
 */
public class AdminConfiguration {
    //========================================================================//
    // Local parameter
    //========================================================================//
    String bashHome;    // MSYS2 path of bash.exe
    String blastHome;   // BLAST+ path of blastn.exe
    String perlHome;    // Strawberry perl path of perl.exe
    String rHome;       // R path of Rscript.exe
    String binHome;     // bin path of Tools
    String scriptsHome; // Barcode DNA counting system scripts path

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * 解析環境を設定するクラスのコンストラクター.
     */
    public AdminConfiguration() {
        String sp = CommonTools.SP;
        File folder = new File(".").getAbsoluteFile().getParentFile();
        String folderPath = folder.getAbsolutePath();
        File tools = new File(folderPath + sp + "Tools").getAbsoluteFile();
        String toolsPath = tools.getAbsolutePath();

        // Bash
        String bash = CommonTools.getPatternName(tools,  "msys2");
        this.bashHome = toolsPath + sp + bash + sp + "usr" + sp + "bin";

        // BLAST+
        String blast = CommonTools.getPatternName(tools,  "ncbi-blast");
        this.blastHome = toolsPath + sp + blast + sp + "bin";

        // Perl
        String perl = CommonTools.getPatternName(tools,  "Strawberry");
        this.perlHome = toolsPath + sp + perl + sp + "perl" + sp + "bin";

        // R
        String r = CommonTools.getPatternName(tools,  "R-");
        this.rHome = toolsPath + sp + r + sp + "bin" + sp + "x64";

        // bin
        String bin = CommonTools.getPatternName(tools,   "bin");
        this.binHome = toolsPath + sp + bin;

        // scripts
        String scripts = CommonTools.getPatternName(folder, "scripts");
        this.scriptsHome = folderPath + sp + scripts;
    }

    //========================================================================//
    // Getter
    //========================================================================//
    // MSYS2 path of bash.exe
    public String getBashHome() {
        return CommonTools.fromWindows2Linux(this.bashHome);
    }
    public String getBashHomeWindows() {
        return this.bashHome;
    }

    // BLAST+ path of blastn.exe
    public String getBlastHome() {
        return CommonTools.fromWindows2Linux(this.blastHome);
    }
    public String getBlastHomeWindows() {
        return this.blastHome;
    }

    // Strawberry perl path of perl.exe
    public String getPerlHome() {
        return CommonTools.fromWindows2Linux(this.perlHome);
    }

    // R path of Rscript.exe
    public String getRHome() {
        return CommonTools.fromWindows2Linux(this.rHome);
    }

    // bin path of Tools
    public String getBinHome() {
        return CommonTools.fromWindows2Linux(this.binHome);
    }
    public String getBinHomeWindows() {
        return this.binHome;
    }

    // Barcode DNA counting system scripts path
    public String getScriptsHome() {
        return CommonTools.fromWindows2Linux(this.scriptsHome);
    }
}
