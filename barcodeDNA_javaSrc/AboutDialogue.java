package barcode;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Aboutダイアログを表示する.
 */
public class AboutDialogue {
    //========================================================================//
    // fx:id
    //========================================================================//
    @FXML
    private AnchorPane aboutDialogueID;     // About dialogue
    @FXML
    private Label aboutLabelID;             // Copyright (AIST)
    @FXML
    private ListView<String> aboutListID;   // Open source licenses

    //========================================================================//
    // Public function
    //========================================================================//
    /**
     * Aboutダイアログを表示するクラスのコンストラクター.
     */
    public AboutDialogue() {
    }

    /**
     * Aboutダイアログを表示する.
     *
     * @param information インフォメーション
     * @param node ダイアログ表示のための基準画面
     * @throws IOException 例外処理
     */
    public void open(String information, Node... node) throws IOException {
        // Set information
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("AboutDialogue.fxml"));
        Parent root = loader.load();
        AboutDialogue aboutDialogue = loader.getController();
        aboutDialogue.aboutLabelID.setText(information);
        aboutDialogue.aboutLabelID.setStyle("-fx-text-fill: blue");

        // MSYS2
        aboutDialogue.aboutListID.getItems().add(this.getMSYS2());
        aboutDialogue.aboutListID.getItems().add(CommonTools.BR);

        // BLAST+
        aboutDialogue.aboutListID.getItems().add(this.getBLAST());
        aboutDialogue.aboutListID.getItems().add(CommonTools.BR);

        // Strawberry Perl
        aboutDialogue.aboutListID.getItems().add(this.getStrawberry());

        // Open dialogue
        Stage stage = new Stage( StageStyle.UTILITY);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add(this.getClass().getResource("barcode.css").toExternalForm());

        // Check dialogue type
        stage.setTitle("About");
        stage.initModality( Modality.APPLICATION_MODAL);

        // Check base screen
        if (node.length > 0) {
            stage.initOwner(node[0].getScene().getWindow());
        }

        stage.showAndWait();
    }

    //========================================================================//
    // On Action
    //========================================================================//
    /**
     * OKボタンがクリックされたときにダイアログを閉じる.
     *
     * @param event アクションイベント
     */
    @FXML
    private void okAction(ActionEvent event) {       // OK button
        this.aboutDialogueID.getScene().getWindow().hide();
    }

    //========================================================================//
    // Private function
    //========================================================================//
    /**
     * MSYS2のライセンス情報を取得する.
     *
     * @return MSYS2のライセンス情報
     */
    private String getMSYS2() {
        return "MSYS2\n" +
                "msys2/MSYS2-packages is licensed under the BSD 3-Clause \"New\" or \"Revised\" License\n" +
                "\n" +
                "Copyright (c) 2013, Алексей\n" +
                "All rights reserved.\n" +
                "\n" +
                "Redistribution and use in source and binary forms, with or without modification,\n" +
                "are permitted provided that the following conditions are met:\n" +
                "\n" +
                "* Redistributions of source code must retain the above copyright notice, this\n" +
                "  list of conditions and the following disclaimer.\n" +
                "\n" +
                "* Redistributions in binary form must reproduce the above copyright notice, this\n" +
                "  list of conditions and the following disclaimer in the documentation and/or\n" +
                "  other materials provided with the distribution.\n" +
                "\n" +
                "* Neither the name of the {organization} nor the names of its\n" +
                "  contributors may be used to endorse or promote products derived from\n" +
                "  this software without specific prior written permission.\n" +
                "\n" +
                "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\n" +
                "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n" +
                "WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
                "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR\n" +
                "ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n" +
                "(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n" +
                "LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON\n" +
                "ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
                "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
                "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n";
    }

    /**
     * BLAST+のライセンス情報を取得する.
     *
     * @return BLAST+のライセンス情報
     */
    private String getBLAST() {
        return "BLAST+\n" +
                "                          PUBLIC DOMAIN NOTICE\n" +
                "             National Center for Biotechnology Information\n" +
                "\n" +
                "This software/database is a \"United States Government Work\" under the\n" +
                "terms of the United States Copyright Act.  It was written as part of\n" +
                "the author's official duties as a United States Government employee and\n" +
                "thus cannot be copyrighted.  This software/database is freely available\n" +
                "to the public for use. The National Library of Medicine and the U.S.\n" +
                "Government have not placed any restriction on its use or reproduction.\n" +
                "\n" +
                "Although all reasonable efforts have been taken to ensure the accuracy\n" +
                "and reliability of the software and data, the NLM and the U.S.\n" +
                "Government do not and cannot warrant the performance or results that\n" +
                "may be obtained by using this software or data. The NLM and the U.S.\n" +
                "Government disclaim all warranties, express or implied, including\n" +
                "warranties of performance, merchantability or fitness for any particular\n" +
                "purpose.\n" +
                "\n" +
                "Please cite the author in any work or product based on this material.\n" +
                "\n" +
                "Altschul, Stephen; Gish, Warren; Miller, Webb; Myers, Eugene; Lipman, David (1990).\n" +
                "Basic local alignment search tool\". Journal of Molecular Biology. 215 (3): 403–410.\n";
    }

    /**
     * Strawberry Perlのライセンス情報を取得する.
     *
     * @return Strawberry Perlのライセンス情報
     */
    private String getStrawberry() {
        return  "Strawberry Perl\n" +
                "This software is copyright (c) 2018 by KMX <kmx@cpan.org>.\n" +
                "\n" +
                "This is free software; you can redistribute it and/or modify it under\n" +
                "the same terms as the Perl 5 programming language system itself.\n" +
                "\n" +
                "Terms of the Perl programming language system itself\n" +
                "\n" +
                "a) the GNU General Public License as published by the Free\n" +
                "   Software Foundation; either version 1, or (at your option) any\n" +
                "   later version, or\n" +
                "b) the \"Artistic License\"\n" +
                "\n" +
                "--- The GNU General Public License, Version 1, February 1989 ---\n" +
                "\n" +
                "This software is Copyright (c) 2018 by KMX <kmx@cpan.org>.\n" +
                "\n" +
                "This is free software, licensed under:\n" +
                "\n" +
                "  The GNU General Public License, Version 1, February 1989\n" +
                "\n" +
                "                    GNU GENERAL PUBLIC LICENSE\n" +
                "                     Version 1, February 1989\n" +
                "\n" +
                " Copyright (C) 1989 Free Software Foundation, Inc.\n" +
                " 51 Franklin St, Suite 500, Boston, MA  02110-1335  USA\n" +
                "\n" +
                " Everyone is permitted to copy and distribute verbatim copies\n" +
                " of this license document, but changing it is not allowed.\n" +
                "\n" +
                "                            Preamble\n" +
                "\n" +
                "  The license agreements of most software companies try to keep users\n" +
                "at the mercy of those companies.  By contrast, our General Public\n" +
                "License is intended to guarantee your freedom to share and change free\n" +
                "software--to make sure the software is free for all its users.  The\n" +
                "General Public License applies to the Free Software Foundation's\n" +
                "software and to any other program whose authors commit to using it.\n" +
                "You can use it for your programs, too.\n" +
                "\n" +
                "  When we speak of free software, we are referring to freedom, not\n" +
                "price.  Specifically, the General Public License is designed to make\n" +
                "sure that you have the freedom to give away or sell copies of free\n" +
                "software, that you receive source code or can get it if you want it,\n" +
                "that you can change the software or use pieces of it in new free\n" +
                "programs; and that you know you can do these things.\n" +
                "\n" +
                "  To protect your rights, we need to make restrictions that forbid\n" +
                "anyone to deny you these rights or to ask you to surrender the rights.\n" +
                "These restrictions translate to certain responsibilities for you if you\n" +
                "distribute copies of the software, or if you modify it.\n" +
                "\n" +
                "  For example, if you distribute copies of a such a program, whether\n" +
                "gratis or for a fee, you must give the recipients all the rights that\n" +
                "you have.  You must make sure that they, too, receive or can get the\n" +
                "source code.  And you must tell them their rights.\n" +
                "\n" +
                "  We protect your rights with two steps: (1) copyright the software, and\n" +
                "(2) offer you this license which gives you legal permission to copy,\n" +
                "distribute and/or modify the software.\n" +
                "\n" +
                "  Also, for each author's protection and ours, we want to make certain\n" +
                "that everyone understands that there is no warranty for this free\n" +
                "software.  If the software is modified by someone else and passed on, we\n" +
                "want its recipients to know that what they have is not the original, so\n" +
                "that any problems introduced by others will not reflect on the original\n" +
                "authors' reputations.\n" +
                "\n" +
                "  The precise terms and conditions for copying, distribution and\n" +
                "modification follow.\n" +
                "\n" +
                "                    GNU GENERAL PUBLIC LICENSE\n" +
                "   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION\n" +
                "\n" +
                "  0. This License Agreement applies to any program or other work which\n" +
                "contains a notice placed by the copyright holder saying it may be\n" +
                "distributed under the terms of this General Public License.  The\n" +
                "\"Program\", below, refers to any such program or work, and a \"work based\n" +
                "on the Program\" means either the Program or any work containing the\n" +
                "Program or a portion of it, either verbatim or with modifications.  Each\n" +
                "licensee is addressed as \"you\".\n" +
                "\n" +
                "  1. You may copy and distribute verbatim copies of the Program's source\n" +
                "code as you receive it, in any medium, provided that you conspicuously and\n" +
                "appropriately publish on each copy an appropriate copyright notice and\n" +
                "disclaimer of warranty; keep intact all the notices that refer to this\n" +
                "General Public License and to the absence of any warranty; and give any\n" +
                "other recipients of the Program a copy of this General Public License\n" +
                "along with the Program.  You may charge a fee for the physical act of\n" +
                "transferring a copy.\n" +
                "\n" +
                "  2. You may modify your copy or copies of the Program or any portion of\n" +
                "it, and copy and distribute such modifications under the terms of Paragraph\n" +
                "1 above, provided that you also do the following:\n" +
                "\n" +
                "    a) cause the modified files to carry prominent notices stating that\n" +
                "    you changed the files and the date of any change; and\n" +
                "\n" +
                "    b) cause the whole of any work that you distribute or publish, that\n" +
                "    in whole or in part contains the Program or any part thereof, either\n" +
                "    with or without modifications, to be licensed at no charge to all\n" +
                "    third parties under the terms of this General Public License (except\n" +
                "    that you may choose to grant warranty protection to some or all\n" +
                "    third parties, at your option).\n" +
                "\n" +
                "    c) If the modified program normally reads commands interactively when\n" +
                "    run, you must cause it, when started running for such interactive use\n" +
                "    in the simplest and most usual way, to print or display an\n" +
                "    announcement including an appropriate copyright notice and a notice\n" +
                "    that there is no warranty (or else, saying that you provide a\n" +
                "    warranty) and that users may redistribute the program under these\n" +
                "    conditions, and telling the user how to view a copy of this General\n" +
                "    Public License.\n" +
                "\n" +
                "    d) You may charge a fee for the physical act of transferring a\n" +
                "    copy, and you may at your option offer warranty protection in\n" +
                "    exchange for a fee.\n" +
                "\n" +
                "Mere aggregation of another independent work with the Program (or its\n" +
                "derivative) on a volume of a storage or distribution medium does not bring\n" +
                "the other work under the scope of these terms.\n" +
                "\n" +
                "  3. You may copy and distribute the Program (or a portion or derivative of\n" +
                "it, under Paragraph 2) in object code or executable form under the terms of\n" +
                "Paragraphs 1 and 2 above provided that you also do one of the following:\n" +
                "\n" +
                "    a) accompany it with the complete corresponding machine-readable\n" +
                "    source code, which must be distributed under the terms of\n" +
                "    Paragraphs 1 and 2 above; or,\n" +
                "\n" +
                "    b) accompany it with a written offer, valid for at least three\n" +
                "    years, to give any third party free (except for a nominal charge\n" +
                "    for the cost of distribution) a complete machine-readable copy of the\n" +
                "    corresponding source code, to be distributed under the terms of\n" +
                "    Paragraphs 1 and 2 above; or,\n" +
                "\n" +
                "    c) accompany it with the information you received as to where the\n" +
                "    corresponding source code may be obtained.  (This alternative is\n" +
                "    allowed only for noncommercial distribution and only if you\n" +
                "    received the program in object code or executable form alone.)\n" +
                "\n" +
                "Source code for a work means the preferred form of the work for making\n" +
                "modifications to it.  For an executable file, complete source code means\n" +
                "all the source code for all modules it contains; but, as a special\n" +
                "exception, it need not include source code for modules which are standard\n" +
                "libraries that accompany the operating system on which the executable\n" +
                "file runs, or for standard header files or definitions files that\n" +
                "accompany that operating system.\n" +
                "\n" +
                "  4. You may not copy, modify, sublicense, distribute or transfer the\n" +
                "Program except as expressly provided under this General Public License.\n" +
                "Any attempt otherwise to copy, modify, sublicense, distribute or transfer\n" +
                "the Program is void, and will automatically terminate your rights to use\n" +
                "the Program under this License.  However, parties who have received\n" +
                "copies, or rights to use copies, from you under this General Public\n" +
                "License will not have their licenses terminated so long as such parties\n" +
                "remain in full compliance.\n" +
                "\n" +
                "  5. By copying, distributing or modifying the Program (or any work based\n" +
                "on the Program) you indicate your acceptance of this license to do so,\n" +
                "and all its terms and conditions.\n" +
                "\n" +
                "  6. Each time you redistribute the Program (or any work based on the\n" +
                "Program), the recipient automatically receives a license from the original\n" +
                "licensor to copy, distribute or modify the Program subject to these\n" +
                "terms and conditions.  You may not impose any further restrictions on the\n" +
                "recipients' exercise of the rights granted herein.\n" +
                "\n" +
                "  7. The Free Software Foundation may publish revised and/or new versions\n" +
                "of the General Public License from time to time.  Such new versions will\n" +
                "be similar in spirit to the present version, but may differ in detail to\n" +
                "address new problems or concerns.\n" +
                "\n" +
                "Each version is given a distinguishing version number.  If the Program\n" +
                "specifies a version number of the license which applies to it and \"any\n" +
                "later version\", you have the option of following the terms and conditions\n" +
                "either of that version or of any later version published by the Free\n" +
                "Software Foundation.  If the Program does not specify a version number of\n" +
                "the license, you may choose any version ever published by the Free Software\n" +
                "Foundation.\n" +
                "\n" +
                "  8. If you wish to incorporate parts of the Program into other free\n" +
                "programs whose distribution conditions are different, write to the author\n" +
                "to ask for permission.  For software which is copyrighted by the Free\n" +
                "Software Foundation, write to the Free Software Foundation; we sometimes\n" +
                "make exceptions for this.  Our decision will be guided by the two goals\n" +
                "of preserving the free status of all derivatives of our free software and\n" +
                "of promoting the sharing and reuse of software generally.\n" +
                "\n" +
                "                            NO WARRANTY\n" +
                "\n" +
                "  9. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY\n" +
                "FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN\n" +
                "OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES\n" +
                "PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED\n" +
                "OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
                "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS\n" +
                "TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE\n" +
                "PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,\n" +
                "REPAIR OR CORRECTION.\n" +
                "\n" +
                "  10. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING\n" +
                "WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR\n" +
                "REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,\n" +
                "INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING\n" +
                "OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED\n" +
                "TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY\n" +
                "YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER\n" +
                "PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE\n" +
                "POSSIBILITY OF SUCH DAMAGES.\n" +
                "\n" +
                "                     END OF TERMS AND CONDITIONS\n";
    }
}
