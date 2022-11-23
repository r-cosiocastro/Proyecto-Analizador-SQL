/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import database.ConnectionManager;
import grammar.MySqlLexer;
import grammar.MySqlParser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import utils.TextLineNumber;
import utils.ThrowingErrorListener;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

/**
 *
 * @author krawz
 */
public class MainWindow extends javax.swing.JFrame {

    String[] funciones = {"select", "delete", "update", "insert"};
    String[] funcionesSelect = {"from", "where", "group by", "order by"};
    final String reserved = "SELECT|FROM|WHERE|NULL|DELETE|UPDATE|INSERT|GROUP|BY|ORDER|"
            + "ADD|ALTER|TABLE|AND|AS|ALL|ASC|"
            + "BEFORE|BETWEEN|BEGIN|BIGINT|BINARY|"
            + "CALL|CASCADE|CASE|CHANGE|CHAR|CHARACTER|CHECK|COLLATE|COLUMN|CONDITION|CONSTRAINT|CONTINUE|CONVERT|CREATE|CROSS|CURSOR|CURRENT_USER|CURRENT_TIME|CURRENT_DATE|"
            + "DELIMITER|DATABASE|DATABASES|DAY_HOUR|DAY_MICROSECOND|DAY_MINUTE|DAY_SECOND|DEC|DECIMAL|DECLARE|DEFAULT|DELAYED|DESC|DESCRIBE|DETERMINISTIC|DISTINCT|DISTINCTROW|DIV|DOUBLE|DROP|DUAL|"
            + "END|EACH|ELSE|ELSEIF|ENCLOSED|ESCAPED|EXISTS|EXIT|EXPLAIN|"
            + "FALSE|FETCH|FLOAT|FLOAT4|FLOAT8|FOR|FORCE|FOREIGN|FULLTEXT|"
            + "GET|GRANT|"
            + "HAVING|HIGH_PRIORITY|HOUR_MICROSECOND|HOUR_MINUTE|HOUR_SECOND|"
            + "IF|IGNORE|IN|INDEX|INFILE|INNER|INOUT|INSENSITIVE|INT|INT1|INT2|INT3|INT4|INT8|INTEGER|INTERVAL|INTO|IO_AFTER_GTIDS|IO_BEFORE_GTIDS|IS|ITERATE|"
            + "JOIN|"
            + "KEY|KEYS|KILL|"
            + "LEADING|LEAVE|LEFT|LIKE|LIMIT|LINEAR|LINES|LOAD|LOCALTIME|LOCALTIMESTAMP|LOCK|LONG|LONGBLOB|LONGTEXT|LOOP|LOW_PRIORITY|"
            + "MESSAGE_TEXT|MASTER_BIND|MASTER_SSL_VERIFY_SERVER_CERT|MATCH|MAXVALUE|MEDIUMBLOB|MEDIUMINT|MEDIUMTEXT|MIDDLEINT|MINUTE_MICROSECOND|MINUTE_SECOND|MODIFIES|"
            + "NATURAL|NEW|NOT|NO_WRITE_TO_BINGLOG|NUMERIC|"
            + "ON|OPTIMIZE|OPTION|OPTIONALLY|OR|OUT|OUTER|OUTFILE|"
            + "PARTITION|PRIMARY|PROCEDURE|PURGE|"
            + "ROW|RANGE|READ|READS|READ_WRITE|REAL|REFERENCES|REGEXP|RELEASE|RENAME|REPEAT|REPLACE|REQUIRE|RESIGNAL|RESTRICT|RETURN|REVOKE|RIGHT|RLIKE|"
            + "SET|SIGNAL|SQLSTATE|SHA1|"
            + "TABLE|TERMINATED|THEN|TINYBLOB|TINYINT|TINYTEXT|TO|TRAILING|TRIGGER|TRUE|"
            + "UNDO|UNION|UNIQUE|UNLOCK|UNSIGNED|USAGE|USE|USING|UTC_DATE|UTC_TIME|UTC_TIMESTAMP|"
            + "VALUES|VARBINARY|VARCHAR|VARCHARACTER|VARYING|VIRTUAL|"
            + "WHEN|WHILE|WITH|WRITE|"
            + "XOR|"
            + "YEAR_MONTH|"
            + "ZEROFILL";
    String lastScript = "";
    String singleQuotes = "'([^']*)'";
    String doubleQuotes = "\"([^\"]*)\"";
    String reverseQuotes = "`([^`]*)`";
    String singleLineComment = "(--)[\\w\\s]*(\\n|\\r)";
    String multiLineComment = "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
    String numbers = "[^\\w\\W?]?([0-9]*)";

    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                if (!String.valueOf(text.charAt(index)).matches("[\"'`]")) {
                    break;
                }
            }
        }
        return index;
    }

    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }

    /**
     * Creates new form Ventana
     */
    public MainWindow() {
        initComponents();
        setTitle("Analizador léxico y sintáctico - MySQL");
        TextLineNumber tln = new TextLineNumber(jTextPane1);
        jScrollPane1.setRowHeaderView(tln);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final AttributeSet attrReservedWord = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(15, 72, 188));
        final AttributeSet attrSingleQuote = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(8, 145, 54));
        final AttributeSet attrDoubleQuote = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(85, 142, 98));
        final AttributeSet attrReverseQuote = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(163, 142, 107));
        final AttributeSet attrComment = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(135, 135, 135));
        final AttributeSet attrNumber = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(234, 161, 35));
        final AttributeSet attrBlack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength()).toUpperCase();
                int before = findLastNonWordChar(text, offset);
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                        if (text.substring(wordL, wordR).matches("(\\W)*(" + reserved + ")")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrReservedWord, true);
                        } else {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlack, true);
                        }
                        wordL = wordR;
                    }
                    wordR++;
                }

                Pattern pattern;
                Matcher matcher;

                //  Números
                pattern = Pattern.compile(numbers);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrNumber, true);
                }

                //  Comillas simples
                pattern = Pattern.compile(singleQuotes);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrSingleQuote, true);
                }

                //  Comillas dobles
                pattern = Pattern.compile(doubleQuotes);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrDoubleQuote, true);
                }

                //  Comillas inversas
                pattern = Pattern.compile(reverseQuotes);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrReverseQuote, true);
                }

                //  Comentario de una línea
                pattern = Pattern.compile(singleLineComment);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrComment, true);
                }

                //  Comentario de varias líneas
                pattern = Pattern.compile(multiLineComment);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attrComment, true);
                }

            }

            @Override

            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offs);

                if (text.substring(before, after).matches("(\\W)*(" + reserved + ")")) {
                    setCharacterAttributes(before, after - before, attrReservedWord, false);
                } else {
                    setCharacterAttributes(before, after - before, attrBlack, false);
                }
            }
        };
        jTextPane1.setDocument(doc);

        jTextPane1.setText("select * from usuarios");
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        btnEjecutar = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel1MouseDragged(evt);
            }
        });
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel1MouseReleased(evt);
            }
        });

        jTextPane1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextPane1KeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jTextPane1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Mensajes:");

        jTextPane2.setEditable(false);
        jScrollPane2.setViewportView(jTextPane2);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Consulta:");

        jButton1.setBackground(new java.awt.Color(63, 81, 181));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ic_spellcheck_black_24dp.png"))); // NOI18N
        jButton1.setText("Analizar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnEjecutar.setBackground(new java.awt.Color(0, 153, 0));
        btnEjecutar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEjecutar.setForeground(new java.awt.Color(255, 255, 255));
        btnEjecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ic_play_circle_outline_black_24dp.png"))); // NOI18N
        btnEjecutar.setText("Ejecutar");
        btnEjecutar.setEnabled(false);
        btnEjecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEjecutarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 748, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(btnEjecutar))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEjecutar)
                    .addComponent(jLabel3))
                .addGap(9, 9, 9)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jMenu1.setText("Archivo");

        jMenuItem1.setText("Abrir\t(Ctrl + O)");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Guardar\t(Ctrl + S)");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Salir");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("Edición");

        jMenuItem6.setText("Copiar");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuItem7.setText("Cortar");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem7);

        jMenuItem8.setText("Pegar");
        jMenu4.add(jMenuItem8);

        jMenuBar1.add(jMenu4);

        jMenu2.setText("Generar");

        jMenuItem4.setText("Árbol de derivación");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Ayuda");

        jMenuItem5.setText("Acerca de");
        jMenu3.add(jMenuItem5);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private Point mouseDownCompCoords = null;

    private void jPanel1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseDragged
        // TODO add your handling code here:
        Point currCoords = evt.getLocationOnScreen();
        setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
    }//GEN-LAST:event_jPanel1MouseDragged

    private void jPanel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MousePressed
        mouseDownCompCoords = evt.getPoint();
    }//GEN-LAST:event_jPanel1MousePressed

    private void jPanel1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseReleased
        mouseDownCompCoords = null;
    }//GEN-LAST:event_jPanel1MouseReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String consulta = jTextPane1.getText().toUpperCase().replaceAll("\n", " ");
        ANTLRInputStream input = new ANTLRInputStream(consulta);
        MySqlLexer lexer = new MySqlLexer(input);
//            Token token = lexer.nextToken();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
//            while (token.getType() != SqlBaseLexer.EOF) {
//                System.out.println("Tipo[" + token.getType() + "], Token["
//                        + token.getText() + "]");
//                token = lexer.nextToken();
//            }
        System.out.println("OK");

        // Analisis Sintactico
//            SqlBaseParser parser = new SqlBaseParser(tokens);
        MySqlParser parserCheck = new MySqlParser(tokens);
        parserCheck.addErrorListener(ThrowingErrorListener.INSTANCE);
        try {
            parserCheck.root();
            jTextPane2.setForeground(Color.black);
            jTextPane2.setText("La consulta está bien escrita");
            lastScript = jTextPane1.getText();
            btnEjecutar.setEnabled(true);
        } catch (ParseCancellationException ex) {
            System.err.println(ex.getMessage());
            jTextPane2.setForeground(Color.red);
            jTextPane2.setText("Se encontró un error al analizar el código\n"
                    + ex.getMessage());
            btnEjecutar.setEnabled(false);
        }
//            ParseTree tree = parser.statement();
//            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
//            System.out.println(tree.toStringTree(parser));

//            System.out.println("OK");
//            viewer.open();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEjecutarActionPerformed
        try {
            // TODO add your handling code here:

            // EJECUTAR CÓDIGO SQL
            String query = jTextPane1.getText();
            if (jTextPane1.getText().toLowerCase().contains("select")) {

                Connection conn = ConnectionManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                String resultado = "";
//                while (rs.next()) {
//                    for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
//                        System.out.println(rsmd.getColumnLabel(columnIndex) + ": " + rs.getString(columnIndex));
//                        resultado += rsmd.getColumnLabel(columnIndex) + ": " + rs.getString(columnIndex) + "\n";
//                    }
//                    System.out.println();
//                    resultado+="\n";
//                }
//                jTextPane2.setText(resultado);
                //JTable table = new JTable(buildTableModel(rs));

                ResultTable vU = new ResultTable(buildTableModel(rs));

                vU.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {

                    }
                });

                vU.show();
                //JOptionPane.showMessageDialog(null, new JScrollPane(table));
                conn.close();
            } else {
                int result = 0;
                Connection conn = ConnectionManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                result = ps.executeUpdate();

                if (result > 0) {
                    jTextPane2.setText("La operación se completó exitosamente.\n"
                            + "Se modificaron: " + result + " filas.");
                } else {
                    jTextPane2.setText("Se produjo un error en la operación.\n"
                            + "Ninguna fila fue afectada.");
                    jTextPane2.setForeground(Color.red);
                }
                conn.close();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(),
                    "Ocurrió un error al ejecutar la consulta", JOptionPane.ERROR_MESSAGE);
            jTextPane2.setText("Error de consulta: " + ex.getMessage());
            jTextPane2.setForeground(Color.red);
            Logger
                    .getLogger(MainWindow.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnEjecutarActionPerformed

    private void jTextPane1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextPane1KeyTyped
        // TODO add your handling code here:
        btnEjecutar.setEnabled(false);
    }//GEN-LAST:event_jTextPane1KeyTyped

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        jTextPane1.setText(openFile());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        saveFile();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        // TODO add your handling code here:
        StringSelection stringSelection = new StringSelection(jTextPane1.getSelectedText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private String openFile() {
        String aux = "";
        String text = "";
        try {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Scripts de SQL", "sql");
            fileChooser.setFileFilter(filter);
            fileChooser.showOpenDialog(this);

            File fileToOpen = fileChooser.getSelectedFile();

            if (fileToOpen != null) {
                FileReader files = new FileReader(fileToOpen);
                try (BufferedReader reader = new BufferedReader(files)) {
                    while ((aux = reader.readLine()) != null) {
                        text += aux + "\n";
                    }
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex + ""
                    + "\nNo se ha encontrado el archivo",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return text;
    }

    private void saveFile() {
        try {
            String nombre = "";
            JFileChooser file = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Scripts de SQL", "sql");
            file.setFileFilter(filter);
            file.showSaveDialog(this);
            File guarda = file.getSelectedFile();

            if (guarda != null) {
                FileWriter save = new FileWriter(guarda);
                save.write(jTextPane1.getText());
                save.close();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Su archivo no se ha guardado",
                    "Advertencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {

        ResultSetMetaData metaData = (ResultSetMetaData) rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        return tableModel;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEjecutar;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables
}
