package main.lwjglb.engine;

import java.io.IOException;
import java.nio.file.*;
/**
 * Defines a public method to load a file into String
 */
public class Utils {
    private Utils(){

    }
    
    public static String readFile(String filePath){
        String str;
        try{
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } 
        catch (IOException excp){
            throw new RuntimeException("Error reading file [" +filePath+"]", excp);
        }
        return str;
    }
}
