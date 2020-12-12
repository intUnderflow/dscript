package site.intunderflow.dscript.utility;

import java.io.File;

public class FileStorageLocation {

    public static String getFileStorageLocationWithFolder(String folder){
        String fullPath = getLocationString() + folder + "\\";
        createFolderIfDoesntExist(fullPath);
        return fullPath;
    }

    public static String getFileStorageLocation(){
        String locationString = getLocationString();
        createFolderIfDoesntExist(locationString);
        return locationString;
    }

    private static void createFolderIfDoesntExist(String path){
        File folder = new File(path);
        if (!(folder.exists() && folder.isDirectory())){
            folder.mkdirs();
        }
    }

    private static String getLocationString(){
        return System.getProperty("user.home") + "\\AppData\\Local\\DScript\\";

    }

}
