/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author krawz
 */
public class TEST {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // Encontrar palabras dentro de comillas
        String regex = "'([^']*)'";
        String test = "SELECT 'campo' FROM 'tabla' WHERE 'campo' = 'hola'";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(test);
        // Check all occurrences
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end());
            System.out.println(" Found: " + matcher.group());
        }
    }

}
