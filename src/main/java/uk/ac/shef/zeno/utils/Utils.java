/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.zeno.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author samf
 */
public class Utils {
    public static HashMap<String, String> readConfig() {
        
        HashMap<String, String> configs = new HashMap<String, String>();
       
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("resources/zeno-r25-config.txt"));
            String line;
            while ((line=br.readLine())!=null) {
               // System.out.println(line);
                String[] split = line.split("\t");
                configs.put(split[0], split[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();   
        }
        return configs;
        
    }
    
   public static HashSet<String> readSet(String filename) {

        HashSet<String> res = new HashSet<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
   
     public static ArrayList<String> readList(String filename) {

        ArrayList<String> res = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
