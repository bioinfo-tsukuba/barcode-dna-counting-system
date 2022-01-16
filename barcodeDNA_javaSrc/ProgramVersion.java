package barcode;

/**
 * プログラム名とバージョンを管理する.
 */
public class ProgramVersion {
    /**
     * プログラム名とバージョンを管理するクラスのコンストラクター.
     */
    private ProgramVersion() {     // prohibit the creation of an instance
    }

    /**
     * プログラム名.
     */
    public static final String PROGRAM = "Barcode DNA counting system";

    /**
     * プログラムのバージョン.
     */
    public static final String VERSION = "1.2";
}
