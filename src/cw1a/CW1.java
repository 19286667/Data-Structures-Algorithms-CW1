package cw1a;

import java.util.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.swing.*;

/**
 *
 * @author David Lightfoot 2025-01
 */

public class CW1 {

    private static Scanner keyboard;
    private static String fileName = "no file choaen";
    
    private static IContactDB db = new ContactsHashOpen(); 
    // change to ContactsHashChained for later part of Coursework 1
    // change to ContactsBST for Coursework 2

    private static boolean acceptable(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' ||
                ch == ' ' || ch == ',' || ch == '@' || ch == '.' ||
                ch == '-'; 
    }

    private static boolean allAcceptable(String s) {
        int i = 0;
        while (i != s.length() && acceptable(s.charAt(i))) i++;
        return i == s.length();
     }

    private static String readAcceptable() {
        // read non-null, non-empty string;
        String s = keyboard.nextLine().trim();
        while (s == null || s.equals("") || !allAcceptable(s) ) {
            System.out.print("all characgters must be acceptable -- try again: ");
            s = keyboard.nextLine().trim();
        }
        assert s != null && !s.equals("")&& allAcceptable(s);
        return s;
    }

    public static void main(String[] args) {
        String option, name, emailAddress;
        Contact resp;
        System.out.println("Starting");
        keyboard = new Scanner(System.in);
        loadFile();

        System.out.print("D)isplay  P)ut  G)et  C)ontains  S)ize  R)emove  Q)uit? ");
        option = readAcceptable();
        while (option.charAt(0) != 'Q' && option.charAt(0) != 'q') {
            switch (option.charAt(0)) {
                case 'D':
                case 'd':  // display
                    db.displayDB();
                    break;

                case 'P':
                case 'p':  // put
                    System.out.print("Name? ");
                    name = readAcceptable();
                    System.out.print("Affiliation? ");
                    emailAddress = readAcceptable();
                    Contact contact = new Contact(name, emailAddress);
                    resp = db.put(contact);
                    System.out.print(name);
                    if (resp == null) {
                        System.out.println(" : new contact added");
                    } else {
                        System.out.println(" : contact overridden");
                        System.out.println("previous was " + resp.toString());
                    }
                    break;

                case 'S':
                case 's':  //size
                    System.out.println("Size " + db.size());
                    break;
                    
                case 'G':
                case 'g':  // get
                    System.out.print("Name? ");
                    name = readAcceptable();
                    resp = db.get(name);
                    if (resp != null) {
                        System.out.println(resp.toString());
                    } else {
                        System.out.println(name + " not found");
                    }
                    break;

                case 'C':
                case 'c':  // contains
                    System.out.print("Name? ");
                    name = readAcceptable();
                    System.out.print(name);
                    if (db.containsName(name)) {
                        System.out.println(" found");
                    } else {
                        System.out.println(" not found");
                    }
                    break;

                case 'R':
                case 'r':  // remove
                    System.out.print("Name? ");
                    name = readAcceptable();
                    resp = db.remove(name);
                    System.out.print(name);
                    if (resp != null) {
                        System.out.println(" deleted");
                    } else {
                        System.out.println(" not found");
                    }
                    break;

                default: //?
                    System.out.println("unknown option");

            } // switch
                    System.out.println();
                    System.out.print("D)isplay  P)ut  G)et  C)ontains  S)ize  R)emove  Q)uit? ");
            option = readAcceptable();
        } // while
    }

    /**
     * Allows the user to select a file containing a description of a graph. If
     * no file is selected, the program terminates.
     *
     * @param startFolder the folder that the file chooser should start from
     * @return the file that was selected
     */
    public static File getDataFile(String startFolder) {
        File f;
        // setting up a dialogue box,
        // to select a file containing data
        JFileChooser fc = new JFileChooser(startFolder);
        fc.setDialogTitle("Choose a file containing data -- 'Cancel' for console input ");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int fcReturnValue = fc.showOpenDialog(null);

        // now, which file did the user select?
        if (fcReturnValue != JFileChooser.APPROVE_OPTION) {
            // user must have cancelled, or an error occurred
            System.out.println("No file selected. input from keyboard.");
            fileName = "no file chosen";
            f = null;
          
            // f = fc.getSelectedFile();
        } else { // user selected a file ok
            fileName = fc.getSelectedFile().getName();
            System.out.println("input from file." + fileName);
            f = fc.getSelectedFile();
        }
        return f;
    }
    
    private static String startFolder = ".";

    private static void loadFile() {
        String cvsSplitBy = ",";
        File file;
        Scanner text;
        int totalVisited;
        Contact contact;
        db.resetTotalVisited();
        try {
            file = getDataFile(startFolder);
            if (file != null) {
                FileInputStream streamIn = new FileInputStream(file);
                text = new Scanner(streamIn);
                while (text.hasNextLine()) {
                    String line = text.nextLine();
                    String[] parts = line.split(cvsSplitBy);
                    String surname = parts[0].trim();
                    String firstNames = parts[1].trim();
                    String affiliation = parts[2].trim();
                    contact = new Contact(surname + ", " + firstNames, affiliation);
                    db.put(contact);
                }
                text.close();
                totalVisited = db.getTotalVisited();
                System.out.println("total number of  buckets visited = " + totalVisited);
                System.out.printf("average number of  buckets visited =  %.2f", 
                  totalVisited /(double)db.getNumEntries());
        System.out.println();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Can't open chosen file " + fileName);
        }
    }

}

