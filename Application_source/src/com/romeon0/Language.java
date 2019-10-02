package com.romeon0;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Romeon0 on 12/13/2017.
 */
public class Language {
    private Map<String,String> vars;
    private String currLanguage="ro";

    Language(){}

    String load(String language){
        String filePath = getFilePath(language);
        if(filePath==null) return "FilePath==NULL";

        Map<String,String> tmpVars = new HashMap<>();
        try{
           // FileReader reader = new FileReader(filePath);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF8"));
            String line, value, id, type;
            Scanner scanner;
       
            while((line=bReader.readLine())!=null){
                if(line.isEmpty()) continue;
                else if(line.startsWith("#")) continue;

                scanner = new Scanner(line);
                type = scanner.next();

                if(type.compareTo("@@@")==0){ //a line/value
                    id = scanner.next();
                    value = scanner.nextLine();
                    tmpVars.put(id,value);
                }
                else if(type.compareTo("%%%")==0){//multiline
                    id = scanner.next();
                    StringBuilder builder = new StringBuilder("");
                    while((line=bReader.readLine())!=null){
                        if(line.startsWith("%%%")) break;
                        builder.append(line);
                    }
                    tmpVars.put(id,builder.toString());
                }

            }

            /*for(Map.Entry<String,String> p: vars.entrySet()){
                System.out.println("ID: "+p.getKey());
                System.out.println("VALUE: "+p.getValue());
            }*/

            bReader.close();
            vars = tmpVars;
        }catch (IOException ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }

    public String get(String id){
        return vars.getOrDefault(id,null);
    }

    private String getFilePath(String lang){
        if(lang.compareTo("ro")==0)
            return "./files/language/ro.txt";
        else  if(lang.compareTo("eng")==0)
            return "./files/language/eng.txt";
        else  if(lang.compareTo("hun")==0)
            return "./files/language/hun.txt";
        else  if(lang.compareTo("rus")==0)
            return "./files/language/rus.txt";
        return null;
    }
}
