package main.java.general;

import com.sun.javafx.application.LauncherImpl;

public class Main{
    public static void main(String[] args) {
        LauncherImpl.launchApplication(AppLoader.class, AppPreloader.class , args);
    }
}
