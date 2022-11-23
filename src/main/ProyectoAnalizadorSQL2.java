/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import grammar.MySqlLexer;
import grammar.MySqlParser;
import utils.ThrowingErrorListener;
import java.awt.Color;
import java.util.Arrays;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import view.MainWindow;

/**
 *
 * @author krawz
 */
public class ProyectoAnalizadorSQL2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        for (int x = 5; x > 0; x--) {
            for (int y = 1; y < 6; y++) {
                if (y > x) {
                    System.out.print("0");
                } else {
                    System.out.print("*");
                }
            }
            System.out.println();
        }
        
        System.out.print("\033[H\033[2J");  
    System.out.flush(); 
    
    new MainWindow().setVisible(true);
        /*
        System.out.println('`'=='`');
        String consulta = "SELECT Orders.OrderID, Customers.CustomerName, Shippers.ShipperName " +
"FROM ((Orders " +
"INNER JOIN Customers ON Orders.CustomerID = Customers.CustomerID) " +
"INNER JOIN Shippers ON Orders.ShipperID = Shippers.ShipperID);".toUpperCase();
        consulta = consulta.toUpperCase();
        System.out.println(consulta);
        ANTLRInputStream input = new ANTLRInputStream(consulta);
        MySqlLexer lexer = new MySqlLexer(input);
//            Token token = lexer.nextToken();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
//            while (token.getType() != MySqlLexer.EOF) {
//                System.out.println("Tipo[" + token.getType() + "], Token["
//                        + token.getText() + "]");
//                token = lexer.nextToken();
//            }
//        System.out.println("OK");

        // Analisis Sintactico
        MySqlParser parserCheck = new MySqlParser(tokens);
        parserCheck.addErrorListener(ThrowingErrorListener.INSTANCE);
        try {
            //parserCheck.root();
            System.out.println("El código está limpio alv");
            ParseTree tree = parserCheck.root();
            TreeViewer viewer = new TreeViewer(Arrays.asList(parserCheck.getRuleNames()), tree);
            viewer.open();
        } catch (ParseCancellationException ex) {
            System.err.println(ex.getMessage());
            System.err.println("Hubo un error en el código");
        }*/
    }

}
