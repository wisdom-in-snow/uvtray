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
import java.util.Scanner;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Simon
 */
public class UVData extends DefaultComboBoxModel {

    private String getLocation(Object data)
    {
        try {
            return (data.toString().substring(18, 38).trim());
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
            data = super.getSelectedItem().toString();
            data = getLocation(data) + ": " + data.substring(50).trim();
        } catch (Exception e) { /* nada */ }
        return data;
    }
    
    public Image getCurrentImage()
    {
        String level;
        try {
            level = super.getSelectedItem().toString().substring(85).trim();
            int max = Integer.parseInt(level);
            
            if (max <= 2) {
                level = "low";
            } else if (max <= 5) {
                level = "moderate";
            } else if (max <= 7) {
                level = "high";
            } else if (max <= 11) {
                level = "veryhigh";
            } else {
                level = "extreme";
            }
        } catch (Exception e) {
            level = "dunno";
        }
        URL imgURL = UVTray.class.getResource(level + ".png");
        return Toolkit.getDefaultToolkit().getImage(imgURL);
    }

    public void downloadData() throws Exception {
        URL url = new URL("ftp://ftp2.bom.gov.au/anon/gen/fwo/IDYGP007.txt");
//        URL url = UVTray.class.getResource("IDYGP007.txt");
        URLConnection urlc = url.openConnection();
        InputStream is = urlc.getInputStream();
        Scanner scanner = new Scanner(is);
        String line;
        
        // get default location
        String defaultLocation = Preferences.userNodeForPackage(UVData.class)
                .get("defaultLocation", "unknown");
        
        // update location list
        removeAllElements();
        addElement("Select a location...");
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            addElement(line);
            if (getLocation(line).equals(defaultLocation))
                setSelectedItem(line);
        }
    }

    public void setDefaultLocation(String location)
    {
        if (! location.equals("Unknown"))
            Preferences.userNodeForPackage(UVData.class)
                    .put("defaultLocation", location);
    }
}
