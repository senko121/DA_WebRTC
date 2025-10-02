/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author thach
 */
public class Config {
      private static Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("️Khong tim thay file application.properties trong resources!");
            } else {
                properties.load(input);
                System.out.println("Da load file application.properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lấy value theo key (string)
    public static String get(String key) {
        return properties.getProperty(key);
    }

    // Lấy value kiểu int
    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}
