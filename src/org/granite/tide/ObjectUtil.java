package org.granite.tide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;


public class ObjectUtil {

    public static boolean isSimple(Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number || value instanceof Date;
    }
    
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

    public static Object copy(Object obj) {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream(500);
            ObjectOutputStream oos = new ObjectOutputStream(buf);
            oos.writeObject(obj);
            ByteArrayInputStream bis = new ByteArrayInputStream(buf.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not copy object " + obj, e);
        }
    }
}
