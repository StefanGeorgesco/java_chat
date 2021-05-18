package fr.sgo.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Class Storage.
 * save and resore objects.
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class Storage {
    public static boolean save(Object object, String objectName) {
        String filename = objectName + ".ser";
        boolean res = false;
        try {
            OutputStream os = new FileOutputStream(new File(filename));
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            os.close();
            res = true;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Object restore(String objectName) {
        Object object = null;
        String filename = objectName + ".ser";
        try {
            InputStream is = new FileInputStream(new File(filename));
            ObjectInputStream oin = new ObjectInputStream(is);
            object = oin.readObject();
            oin.close();
            is.close();
        }
        catch(Exception e) {
            System.out.println("impossible d'ouvrir le fichier " + filename);
        }
        return object;
    }
}
