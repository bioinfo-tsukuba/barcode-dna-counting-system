package barcode;

import javafx.scene.control.ListView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * HTMLファイルを作成する.
 */
public class CreateHtml {
    //========================================================================//
    // Local data
    //========================================================================//
    private final List<String> Pair = Arrays.asList("R1", "R2");
    private final List<String> Strand = Arrays.asList("forward", "reverse");
    private final List<String> FigNumb = Arrays.asList("First", "second", "third", "fourth");

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * HTMLファイルを作成するクラスのコンストラクター.
     */
    public CreateHtml() {
    }

    /**
     * HTMLファイルを作成する.
     *
     * @param op 計算オプションを管理するクラス
     * @param message メッセージ出力欄
     * @return HTMLファイル
     */
    public String create(Options op, ListView<String> message) {
        Options options = op;
        String html = options.getOutputFolder() + CommonTools.SP + options.getOutputPrefix() + ".html";
        html = CommonTools.serialFileName(html);
        File htmlFile = new File(html);

        // Output start time date in Message area
        Date dateStart = new Date();

        // Message
        String calType = "Draw figures";
        message.getItems().add(CommonTools.SDF.format(dateStart) + "  " + calType + " start ....");

        try (BufferedWriter bw = Files.newBufferedWriter(htmlFile.toPath())) {
            PrintWriter pw = new PrintWriter(bw);
            pw.println("<!DOCTYPE html>");
            pw.println("<html>");
            pw.println("<head>");
            pw.println("<title>" + ProgramVersion.PROGRAM +"</title>");
            pw.println("<style type=\"text/css\">");
            pw.println("img {max-width:100%; height:auto;}");
            pw.println(".figure {font-weight:bold;}");
            pw.println(".left {float:left;}");
            pw.println(".right {float:right;}");
            pw.println("</style>");
            pw.println("</head>");
            pw.println("<body>");

            int ifigure = 0;
            for (int i = 0; i < options.getRead1Files().size(); i++) {
                String figure = null;
                int figCount = 0;
                String name = CommonTools.getFastqName(options.getRead1Files().get(i));
                for (int j = 0; j < this.Pair.size(); j++) {
                    for (int k = 0; k < this.Strand.size(); k++) {
                        String img = CommonTools.getImgName(name, j, k);
                        String png = options.getOutputFolder() + CommonTools.SP + img;
                        File pngFile = new File(png);
                        if (pngFile.isFile() && pngFile.canRead()) {
                            String addCaption = this.FigNumb.get(figCount++) + ":" + this.Pair.get(j) + "&nbsp;" + this.Strand.get(k);
                            if (figure == null) {
                                figure = "Results&nbsp;of&nbsp;" + name + "&nbsp;count. &nbsp;" + addCaption;
                            } else {
                                figure += ", " +  addCaption;
                            }
                            pw.println("<p align=center><img src=\"" + img + "\"></p>");
                        }
                    }
                }
                if (figure != null) {
                    pw.println("<p align=center><b>Figure&nbsp;" + ++ifigure + ":&nbsp;" + figure + ".</b></p>");
                    pw.println("<hr color=\"#0000ff\">");
                }
            }
            pw.println("<footer>");
            pw.println("<div class=\"parent clearfix\">");
            pw.println("<div class=\"left\">" + ProgramVersion.PROGRAM + " " + ProgramVersion.VERSION + "</div>");
            pw.println("<div class=\"right\">" + CommonTools.SDFFILE.format(new Date()) + "</div>");
            pw.println("</div>");
            pw.println("</footer>");
            pw.println("</body>");
            pw.println("</html>");
        } catch (IOException e) {
            String errorMessage = "Error. " + calType + " html file was not created.";
            message.getItems().add(errorMessage);
            return null;
        }
        // End message
        CommonTools.endMessage(calType, message);
        return html;
    }
}
