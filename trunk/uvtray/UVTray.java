package uvtray;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Simon Windows
 */
public class UVTray {

    static UVFrame uvframe;
    static UVData uvdata;
    static TrayIcon trayIcon;
    static Timer timer;
    static TimerTask timertask;
    
    static void downloadData() {
        long nextUpdate;
        // kill off old timer task
        if (timertask != null) timertask.cancel();
        // download data
        try {
            uvdata.downloadData();
            nextUpdate = 1000 * 60 * 60; // one hour
        } catch (Exception ie) {
            nextUpdate = 1000 * 60 * 5; // five minutes
        }
        // setup new timer task
        timertask = new TimerTask() { public void run() { downloadData(); } };
        timer = new Timer();
        timer.schedule(timertask, nextUpdate);
        uvframe.setUpdateText(new Date().getTime(),
                timertask.scheduledExecutionTime());
        // update UVTray
        String alert = uvdata.getCurrentAlert();
        Image image = uvdata.getCurrentImage();
        uvframe.setDataText(alert);
        uvframe.setIconImage(image);
        trayIcon.setToolTip(alert);
        trayIcon.setImage(image);
    }
   
    static void changeLocation() {
        String alert = uvdata.getCurrentAlert();
        uvframe.setDataText(alert);
        trayIcon.setToolTip(alert);
        trayIcon.setImage(uvdata.getCurrentImage());
        uvdata.setDefaultLocation(uvdata.getCurrentLocation());
    }
    
    static ActionListener listener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            //System.err.println(action + e);
            if (action == null || action.equals("Show/Hide Info")) {
                uvframe.setVisible(!uvframe.isVisible());
            } else if (action.equals("Update Now")) {
                downloadData();
            } else if (action.equals("comboBoxChanged")) {
                changeLocation();
            } else if (action.equals("Exit")) {
                System.exit(0);
            }
        }
    };

    public static void main(String[] a) throws Exception
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        uvdata = new UVData();
        uvframe = new UVFrame();
        uvframe.addActionListener(listener);
        
        if (SystemTray.isSupported()) {
            // init global variables
            final PopupMenu popup = new PopupMenu();
            trayIcon = new TrayIcon(uvdata.getCurrentImage(),
                                    "Not yet updated", popup);
            // Setup menu
            MenuItem item;
            item = new MenuItem("Show/Hide Info");
            item.addActionListener(listener);
            popup.add(item);
            item = new MenuItem("Update Now");
            item.addActionListener(listener);
            popup.add(item);
            popup.addSeparator();
            item = new MenuItem("Exit");
            item.addActionListener(listener);
            popup.add(item);
            // setup tray
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(listener);

            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
        } else {
            uvframe.setVisible(true);
        }
        downloadData();
        uvframe.setModel(uvdata);
    }
}
