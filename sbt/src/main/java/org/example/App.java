package org.example;

import java.io.*;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Please enter: ");
        var reader = new BufferedReader(new InputStreamReader(System.in));
        var input = reader.readLine();
        System.out.println("You entered: " + input);
    }
}
