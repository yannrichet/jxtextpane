/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author richet
 */
public class REditorPane extends CodeEditorPane {

    public static final String HTTP_BASE = "http://stat.ethz.ch/R-manual/R-patched/library/base/html/";
    public static HashMap<String, Color> keyword_color = new HashMap<String, Color>();
    public static HashMap<String, String> help_link = new HashMap<String, String>();

    public static String readURL(String urlstr) {

        BufferedReader in = null;
        StringBuffer str = new StringBuffer();
        URLConnection conn = null;
        try {
            URL url = new URL(urlstr);

            conn = url.openConnection();
            conn.setConnectTimeout(2000);

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                str.append(inputLine);
                str.append("\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to access " + urlstr);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str.toString();
    }

    static public void buildCompletionMap() {
        if (!help_link.isEmpty()) {
            return;
        }

        String index = "00Index.html";
        String indexContent = readURL(HTTP_BASE + index).trim();
        if (indexContent == null || indexContent.length() == 0) {
            return;
        }

        String startStr = "-- A --";
        String endStr = "-- misc --";
        String toParse = indexContent.substring(indexContent.indexOf(startStr), indexContent.indexOf(endStr));

        Matcher href = Pattern.compile("href=(.+)<").matcher(toParse);
        while (href.find()) {
            String tosplit = href.group();
            String link = tosplit.substring(6, tosplit.indexOf(".html", 7));
            String key = tosplit.substring(tosplit.indexOf(">") + 1, tosplit.indexOf("<"));
            help_link.put(key, link);
        }
    }

    static class RHelpMap extends HashMap {

        volatile LinkedList<Object> triedKeys = new LinkedList<Object>();

        @Override
        public boolean containsKey(Object key) {
            if (!triedKeys.contains(key)) {
                tryget(key);
            }
            return super.containsKey(key);
        }

        @Override
        public Object get(Object key) {
            if (!triedKeys.contains(key)) {
                tryget(key);
            }
            Object a = super.get(key);
            return a;
        }

        public void tryget(Object key) {
            //System.err.println("tryget " + key);
            if (triedKeys.contains(key)) {
                return;
            }
            String map_key = key.toString();
            if (help_link.containsKey(map_key)) {
                map_key = help_link.get(map_key);
            }
            String content = getBody(HTTP_BASE + map_key + ".html");
            put((String) key, content);

            triedKeys.add(key);
        }
    }

    private static String getBody(String url) {
        String content = readURL(url).trim();

        if (content != null) {
            if (content.contains("Error 404") || content.length() == 0) {
                return "No information available";
            }
        }
        if (content.contains("<html>")) {
            content = content.substring(content.indexOf("<html>"), content.indexOf("</html>") + 7);
        }
        if (content.contains("<body>")) {
            content = "<html>" + content.substring(content.indexOf("<body>") + 6, content.indexOf("</body>")) + "</html>";
        }

        Matcher href = Pattern.compile("href=").matcher(content);
        int offset = 0;
        while (href.find()) {
            if (!content.substring(offset + href.start() + 6).startsWith("http://")) {
                content = content.substring(0, offset + href.start() + 6) + HTTP_BASE + content.substring(offset + href.start() + 6);
                offset = offset + HTTP_BASE.length();
            }
        }

        return content;
    }

    class RDocPane extends JEditorPane implements HyperlinkListener {

        public RDocPane(String text) {
            setEditable(false);
            setContentType("text/html");
            setText((text));

            HTMLEditorKit html = (HTMLEditorKit) getEditorKit();
            StyleSheet styleSheet = html.getStyleSheet();
            styleSheet.addRule("body {font: arial; font-size: 10pt;}");

            addHyperlinkListener(this);
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    String url = e.getDescription();
                    String content = getBody(url);
                    setText(content);
                    setCaretPosition(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public REditorPane() {
        buildCompletionMap();

        for (String f : help_link.keySet()) {
            keyword_color.put(f, Color.BLUE);
        }
        setKeywordColor(keyword_color);
        syntaxDocumentFilter.operands = syntaxDocumentFilter.operands.replace(".", ""); // '.' is not an operand char in R

        if (help == null) {
            help = new RHelpMap();
        }

        /*for (String key : help_link.keySet()) {
        ((RHelpMap) keyword_help).tryget(key);
        }*/
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(3000); // needed to let proxy conf to be done
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                for (String key : help_link.keySet()) {
                    ((RHelpMap) help).tryget(key);
                }
            }
        }).start();
    }

    @Override
    public JComponent buildHelpMenu(String help) {
        JScrollPane h = new JScrollPane(new RDocPane(help));
        h.setPreferredSize(new Dimension(600, 600));
        return h;
    }

    public static void main(String[] args) {
        REditorPane edit = new REditorPane();

        String R_src = "function (x) \n{\n    x1 <- x[1] * 15 - 5\n    x2 <- x[2] * 15\n    (x2 - 5/(4 * pi^2) * (x1^2) + 5/pi * x1 - 6)^2 + 10 * (1 - \n        1/(8 * pi)) * cos(x1) + 10\n}";
        edit.setText(R_src);

        edit.setVerticalLineAtPos(80);

        JFrame frame = new JFrame("R Code editor");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}
