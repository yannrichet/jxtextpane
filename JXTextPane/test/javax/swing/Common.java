package javax.swing;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author richet
 */
public class Common {

    public static String read(String file) throws Exception {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s + "\n");
        }
        fr.close();
        return sb.toString();
    }
}
