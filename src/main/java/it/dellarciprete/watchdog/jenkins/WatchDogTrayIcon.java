package it.dellarciprete.watchdog.jenkins;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

public class WatchDogTrayIcon {

  private static final Logger LOGGER = WatchDogLogger.get();

  /**
   * Initializes the tray icon allowing for closing the application.
   */
  public static void initializeTrayIcon() {
    if (!SystemTray.isSupported()) {
      LOGGER.log(Level.WARNING, "SystemTray not supported");
      return;
    }
    final PopupMenu popup = new PopupMenu();
    final TrayIcon trayIcon = new TrayIcon(createImage("watchdog.png", "tray icon"), "Jenkins WatchDog");
    final SystemTray tray = SystemTray.getSystemTray();
    MenuItem exitItem = new MenuItem("Exit");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tray.remove(trayIcon);
        System.exit(0);
      }
    });
    popup.add(exitItem);
    trayIcon.setPopupMenu(popup);
    trayIcon.setImageAutoSize(true);
    try {
      tray.add(trayIcon);
    } catch (AWTException e) {
      LOGGER.log(Level.SEVERE, "Unable to add the tray icon", e);
    }
  }

  private static Image createImage(String path, String description) {
    URL imageURL = WatchDogTrayIcon.class.getResource(path);
    if (imageURL == null) {
      LOGGER.log(Level.WARNING, "Unable to load the image for the tray icon");
      return null;
    } else {
      return new ImageIcon(imageURL, description).getImage();
    }
  }

}
