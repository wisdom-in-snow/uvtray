/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uvtray;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Simon
 */
public class UVData extends DefaultComboBoxModel {

    private String getLocation(Object data)
    {
        try {
            return (data.toString()
                    .split(": ")[0] // remove UV data
//                    .replaceFirst(".*, ", "") // remove state
                    .trim());
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public String getCurrentLocation()
    {
        return (getLocation(super.getSelectedItem()));
    }
    
    public String getCurrentAlert()
    {
        String data = "Error getting UV data!";
        try {
            data = super.getSelectedItem().toString().split(", ",2)[1];
            //data = getLocation(data) + ": " + data.substring(50).trim();
        } catch (Exception e) { /* nada */ }
        return data;
    }

    static final Map<String , String> images = new HashMap<String , String>() {{
        put("Low", "low.png");
        put("Moderate", "moderate.png");
        put("High", "high.png");
        put("Very High", "veryhigh.png");
        put("Extreme", "extreme.png");
    }};

    public Image getCurrentImage()
    {
        try {
            String image = images.get(super.getSelectedItem().toString()
                                      .split("[\\[\\]]")[1]);
            URL imgURL = UVTray.class.getResource(image);
            return Toolkit.getDefaultToolkit().getImage(imgURL);
        } catch (Exception e) {
            URL imgURL = UVTray.class.getResource("dunno.png");
            return Toolkit.getDefaultToolkit().getImage(imgURL);
        }
    }

    public void downloadData() throws Exception {

        String[] states = {"NSW", "NT", "QLD", "SA", "TAS", "VIC", "WA"};
        //String bomurl = "ftp://ftp2.bom.gov.au/anon/gen/fwo/IDYGP007.XXX.txt";
        String bomurl = "ftp2.bom.gov.au_anon_gen_fwo_IDYGP007.XXX.txt";
        String line;

        LinkedList locations = new LinkedList();

        for (int i = 0; i < 7; i++) {

            //URL url = new URL(bomurl.replaceFirst("XXX", states[i]));
            URL url = UVTray.class.getResource(bomurl.replaceFirst("XXX", states[i]));
            URLConnection urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            Scanner scanner = new Scanner(is);

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.length() > 100) {
                    line = states[i] + ", " +
                           line.substring(18,37).trim() + ": " +
                           line.substring(50,84).trim() + ", " +
                           line.substring(85).trim();
                    locations.add(line);
                }
            }
        }

        // get default location
        String defaultLocation = Preferences.userNodeForPackage(UVData.class)
                .get("defaultLocation", "unknown");
        
        // update location list
        removeAllElements();
        if (! locations.isEmpty())
        {
            addElement("Select a location...");
            while (! locations.isEmpty())
            {
                line = locations.removeFirst().toString();
                addElement(line);
                if (getLocation(line).equals(defaultLocation))
                    setSelectedItem(line);
            }
        }
    }

    public void setDefaultLocation(String location)
    {
        if (! location.equals("Unknown"))
            Preferences.userNodeForPackage(UVData.class)
                    .put("defaultLocation", location);
    }
}
