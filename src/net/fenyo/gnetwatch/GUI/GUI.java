
/*
 * GNetWatch
 * Copyright 2006, 2007, 2008 Alexandre Fenyo
 * gnetwatch@fenyo.net
 *
 * This file is part of GNetWatch.
 *
 * GNetWatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GNetWatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GNetWatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

// icones :
// http://www.kde-look.org/content/show.php?content=8341

// http://www.oxygen-icons.org/?cat=3
// http://icon-king.com/?p=34
// http://www.kde-artists.org/
// http://www.kde-look.org/content/show.php?content=8341

package net.fenyo.gnetwatch.GUI;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.swing.*;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.actions.*;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.targets.*;
import net.fenyo.gnetwatch.data.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.tree.xpath.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.browser.*;
import org.hibernate.Session;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.util.TableEvent;

/**
 * Manages the main GUI.
 * @author Alexandre Fenyo
 * @version $Id: GUI.java,v 1.139 2008/12/05 13:17:38 fenyo Exp $
 */

public class GUI implements Runnable {
  private static Log log = LogFactory.getLog(GUI.class);

  private Config config;
  private Background background;
  private Main main;
  private SNMPManager snmp_manager;
  private Views views;
  private Synchro synchro;

  private AwtGUI awtGUI = null;

  // GUI/SWT/AWT objects
  private Display display = null;

  // main Shell
  private Shell shell = null;
  private GridLayout layout = null;

  // Menu
  private Menu menu_bar = null, menu_file = null;
  private MenuItem menu_item_file = null;
  private MenuItem menu_item_exit = null;
  private MenuItem menu_item_merge_now = null;
  private MenuItem menu_item_hide = null;
  private MenuItem menu_item_show = null;
  private MenuItem menu_item_clear = null;
  
  // Toolbar
  private ToolBar toolbar = null;
  private GridData toolbar_grid_data = null;

  // vertical Sash for horizontal Composite and text console
  private SashForm vertical_sash = null;
  private GridData vertical_sash_grid_data = null;

  // horizontal Composite
  private Composite horizontal_composite = null;
  private GridLayout horizontal_composite_layout = null;

  // text console
  private Browser text_console = null;
  private StringBuffer text_console_content = new StringBuffer();

  // groups composite, groups and childs
  private Composite groups_composite = null;
  private RowLayout groups_composite_layout = null;
  private GridData groups_composite_grid_data = null;
  private Group group_target_subnet = null, group_target_range = null, group_target_host = null, group_target_group = null, group_target_host6 = null;
  // private Group group_data = null;
  private GridLayout group_target_subnet_layout = null, group_target_range_layout = null, group_target_host_layout = null, group_target_group_layout = null, group_target_host6_layout = null;
  // private GridLayout group_data_layout = null;
  private Text target_subnet_address = null, target_subnet_mask = null;
  private Text target_range_begin = null, target_range_end = null;
  private Button target_subnet_push = null, target_range_push = null;
  private Text target_host_value = null;
  private Text target_group_value = null;
  private Text target_host6_value = null;
  private Button target_host_push = null, target_group_push = null, target_host6_push = null;
  private TargetGroup user_defined = null;
  private final int bglevel = 100;

  // horizontal Sash for Tree and Label
  private SashForm horizontal_sash = null;
  private GridData horizontal_sash_grid_data = null;

  // Tree
  private Tree tree = null;
  private TreeColumn tree_column1 = null, tree_column2 = null, tree_column3 = null;

  // bottom status
  private StyledText status = null;
  private GridData status_grid_data = null;

  // progress bar
  private ProgressBar progress_bar = null;
  private GridData progress_bar_grid_data = null;
  private CTabFolder tab_folder = null;
  
  // General variables
  private Thread thread = null;
  private final boolean GUI_created[] = { false };

  // images
  private Image image_folder = null, image_oscillo = null, image_exec = null,
          image_watch = null, image_host = null, image_host6 = null,
          image_host_snmp = null, image_host6_snmp = null,
          image_interface = null, image_queue = null, image_network = null,
          image_multirow = null;

  // visuals
  private VisualElement visual_root = null;
  private VisualElement visual_queues = null;
  private VisualElement visual_transient = null;
  private TargetGroup visual_thishost = null;
//  private VisualElement visual_persistent = null;
  private TargetGroup visual_transient_all = null;
  private TargetGroup visual_transient_networks = null;
//  private VisualElement visual_persistent_all = null;

  private Label label1 = null, label2 = null, label3 = null,
                label4 = null, label5 = null, label6 = null,
                label7 = null;

  private MenuItem menu_item_add_host = null, menu_item_add_host6 = null, menu_item_add_range = null,
                   menu_item_add_network = null, menu_item_add_group = null, menu_item_remove_target = null;

  private ToolItem item_add_host = null, item_add_network = null,
                   item_discover_start = null, item_discover_stop = null,
                   item_remove_target = null, item_add_ping = null, item_remove_action = null;

  private MenuItem menu_item_credentials = null, menu_item_ip_options = null, menu_item_http_options = null, menu_item_generic_options = null;

  private MenuItem menu_item_add_ping = null, menu_item_add_process = null, menu_item_add_source = null, menu_item_add_flood = null, menu_item_add_http = null,
                   menu_item_explore_snmp = null, menu_item_explore_nmap = null, menu_item_remove_action = null,
                   menu_item_remove_action_ping = null, menu_item_remove_action_flood = null,
                   menu_item_remove_action_explore = null, menu_item_remove_view = null,
                   menu_item_remove_view_ping = null, menu_item_remove_view_flood = null,
                   menu_item_remove_view_explore = null;

  VisualElement previous_selection = null;

  // synchronization object for access and modifications to the tree of visual elements
  public Object sync_tree = new Object();

  private final Map<Target, Target> target_map = new HashMap<Target, Target>();

  private CTabItem tab_item1, tab_item2;

  private int text_console_do_not_go_on_top = 0;

  /**
   * Return the configuration.
   * @param none.
   * @return Config configuration.
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Return the root shell.
   * @param none.
   * @return Shell shell.
   */
  public Shell getShell() {
    return shell;
  }

  /**
   * Computes the desired background color.
   * @param none.
   * @return background color.
   */
  public Color getBackgroundColor() {
    return new Color(display, (int) Math.min((int) shell.getBackground().getRed() * 1.05, 255),
        (int) Math.min((int) shell.getBackground().getGreen() * 1.05, 255),
        (int) Math.min((int) shell.getBackground().getBlue() * 1.05, 255));
  }

  /**
   * Returns the SNMPManager instance.
   * @param none.
   * @return SNMPManager instance.
   */
  public SNMPManager getSNMPManager() {
    return snmp_manager;
  }

  /**
   * Returns the Synchro instance.
   * @param none.
   * @return Synchro instance.
   */
  public Synchro getSynchro() {
    return synchro;
  }

  /**
   * Returns the multithreaded synchronization lock for GUI creation.
   * @param none.
   * @return lock.
   */
  public boolean [] getGUICreated() {
    return GUI_created;
  }

  /**
   * Gets the tree node acting as the visual transient root.
   * @param none.
   * @return VisualElement visual transient element.
   */
  public VisualElement getVisualTransient() {
    return visual_transient;
  }

  /**
   * Gets the tree node acting as the "local host" root.
   * @param none.
   * @return TargetGroup visual transient node.
   */
  public TargetGroup getVisualThisHost() {
    return visual_thishost;
  }

  /**
   * Gets the tree node acting as the "every host" root.
   * @param none.
   * @return TargetGroup "every host" node.
   */
  public TargetGroup getVisualTransientAll() {
    return visual_transient_all;
  }

  /**
   * Gets the tree node acting as the "every network" root.
   * @param none.
   * @return TargetGroup "every network" node.
   */
  public TargetGroup getVisualTransientNetworks() {
    return visual_transient_networks;
  }

  /**
   * Returns the AwtGUI instance used to build AWT frames that host Java2D-drawn components.
   * @param none.
   * @return AwtGUI AWT gui.
   */
  // GUI thread
  public AwtGUI getAwtGUI() {
    return awtGUI;
  }

  /**
   * Removes a target.
   * @param target target to remove.
   * @return void.
   */
  // GUI thread
  public void dropTargetInstance(final VisualElement target) {
    target_map.remove(target);
  }

  /**
   * Returns the canonical instance of a target and registers this target as the canonical one if needed.
   * The canonical instance is an instance that equals to this one
   * and that was the first created.
   * @param target instance.
   * @return Target canonical instance.
   */
  // GUI thread
  // lock survey: sync_tree << synchro << HERE
  public Target getCanonicalInstance(final Target target) {
    final Target canonic_instance = target_map.get(target);
    if (canonic_instance == null) {
      target_map.put(target, target);
      return target;
    } else return canonic_instance;
  }

  /**
   * Checks that this instance has already been created.
   * @param target checks against this instance.
   * @return true if this instance has already been created.
   */
  // Capture thread
  // lock survey : sync_tree << synchro << HERE
  // lock survey : sync_tree << HERE
  public boolean containsCanonicalInstance(final Target target) {
    return target_map.get(target) != null;
  }

  /**
   * Execute this operation in the future in the SWT thread.
   * @param r operation to execute.
   * @return void.
   */
  // any thread
  public void asyncExecIfNeeded(final Runnable r) {
    try {
      // If thread is null, the thread GUI is not started.
      if (thread == null || Thread.currentThread().equals(thread)) r.run();
      else if (config != null && config.isEnd() == false && display != null && !display.isDisposed())
        display.asyncExec(r);
    } catch (final Exception ex) {
      if (log != null && ex != null) log.error(ex);
    }
  }

  public void asyncExec(final Runnable r) {
    try {
      if (config != null && config.isEnd() == false && display != null && !display.isDisposed())
        display.asyncExec(r);
    } catch (final Exception ex) {
      log.error(ex);
    }
  }

  /**
   * Returns the tab folder.
   * @param none.
   * @return CTabFolder tab folder.
   */
  // GUI thread
  public CTabFolder getTabFolder() {
    return tab_folder;
  }

  /**
   * Returns the picture "image folder".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageFolder() {
    return image_folder;
  }

  /**
   * Returns the picture "exec".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageExec() {
    return image_exec;
  }
  
  /**
   * Returns the picture "oscilloscope".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageOscillo() {
    return image_oscillo;
  }

  /**
   * Returns the picture "multirow".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageMultiRow() {
    return image_multirow;
  }

  /**
   * Returns the picture "watch".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageWatch() {
    return image_watch;
  }

  /**
   * Returns the picture "IPv4 host".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageHost() {
    return image_host;
  }

  /**
   * Returns the picture "IPv6 host".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageHost6() {
    return image_host6;
  }

  /**
   * Returns the picture "interface".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageInterface() {
    return image_interface;
  }

  /**
   * Returns the picture "queue".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageQueue() {
    return image_queue;
  }

  /**
   * Returns the picture "network".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageNetwork() {
    return image_network;
  }

  /**
   * Returns the picture "IPv4 SNMP host".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  // lock survey : sync_tree << synchro << HERE
  public Image getImageHostSNMP() {
    return image_host_snmp;
  }

  /**
   * Returns the picture "IPv6 SNMP host".
   * @param none.
   * @return Image picture.
   */
  // GUI thread
  public Image getImageHost6SNMP() {
    return image_host6_snmp;
  }

  /**
   * Resets the status string.
   * May be called from any thread.
   * @param none.
   * @return void.
   */
  public void setStatus(final String str) {
    asyncExecIfNeeded(new Runnable() {
      public void run() {
        if (status.isDisposed() == false) status.setText(" " + str);
      }
    });
  }

  /**
   * Sets the progress bar position.
   * @param position position.
   * @return void.
   */
  public void setProgress(final int position) {
    asyncExecIfNeeded(new Runnable() {
      public void run() {
        progress_bar.setSelection(position);
      }
    });
  }

  /**
   * Encapsulates an html part into a face definition.
   * @param html html source part.
   * @return embeded html part.
   */
  public String htmlFace(final String html) {
    return "<FONT FACE='Verdana' size='-2'>" + html + "</FONT>";
  }

  /**
   * Adds a string to the console.
   * May be called from any thread.
   * @param none.
   * @return void.
   */
  public void appendConsole(final String str) {
    asyncExecIfNeeded(new Runnable() {
      public void run() {
        text_console_content.append(str);
        text_console.setText("<html><body bgcolor='#" +
            String.format("%2x%2x%2x",
            getBackgroundColor().getRed(),
            getBackgroundColor().getGreen(),
            getBackgroundColor().getBlue()) +
            "'>" +
            htmlFace(text_console_content.toString()) +
            "</body></html>");
      }
    });
  }

  /**
   * Clear console content.
   * May be called from any thread.
   * @param none.
   * @return void.
   */
  public void clearConsole() {
    asyncExecIfNeeded(new Runnable() {
      public void run() {
        text_console_content.setLength(0);
        text_console.setText("<html><body bgcolor='#" +
            String.format("%2x%2x%2x",
            getBackgroundColor().getRed(),
            getBackgroundColor().getGreen(),
            getBackgroundColor().getBlue()) +
            "'>" +
            htmlFace(text_console_content.toString()) +
            "</body></html>");
      }
    });
  }

  /**
   * Check that the selection is under the transient node.
   * @param none.
   * @return true if the selection is under the transient node.
   */
  // GUI thread
  private boolean isSelectionTransient() {
    if (tree.getSelectionCount() == 1) {
      TreeItem current_item = tree.getSelection()[0];
      while (current_item != null &&
          visual_transient.isThisOurTreeItem(current_item) == false /* &&
          visual_persistent.isThisOurTreeItem(current_item) == false */)
        current_item = current_item.getParentItem();
      if (visual_transient.isThisOurTreeItem(current_item)) return true;
    }
    return false;
  }

  // GUI thread
  /*
   private boolean isSelectionPersistent() {
    if (tree.getSelectionCount() == 1) {
      TreeItem current_item = tree.getSelection()[0];
      while (current_item != null &&
          visual_transient.isThisOurTreeItem(current_item) == false &&
          visual_persistent.isThisOurTreeItem(current_item) == false)
        current_item = current_item.getParentItem();
      if (visual_persistent.isThisOurTreeItem(current_item)) return true;
    }
    return false;
  }
  */

  /**
   * Expands every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void expandAll(final TreeItem item) {
    synchronized (sync_tree) {
      item.setExpanded(true);
      for (final TreeItem it : item.getItems()) expandAll(it);
    }
  }

  /**
   * Merges every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void mergeAll(final TreeItem item) {
    synchronized (sync_tree) {
      item.setExpanded(false);
      for (final TreeItem it : item.getItems()) mergeAll(it);
    }
  }

  /**
   * Adds an action on every node under a specified root node.
   * @param item root node.
   * @param clazz action class.
   * @return void.
   */
  // GUI thread
  private void addActionAll(final TreeItem item, Class clazz) {
    synchronized (synchro) {
      synchronized (sync_tree) {
        final Session session = synchro.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try  {
          for (final VisualElement elt : VisualElement.getSubElements(item, Target.class)) {
          final Target target = (Target) elt;

          boolean already_has_action = false;
          for (final VisualElement child : target.getChildren())
            if (clazz.isInstance(child)) {
              already_has_action = true;
              break;
            }

          if (already_has_action == false) {
            net.fenyo.gnetwatch.actions.Action action;
            try {
              action = (net.fenyo.gnetwatch.actions.Action) clazz.newInstance();
            } catch (final IllegalAccessException ex) {
              log.error("Exception", ex);
              return;
            } catch (final InstantiationException ex) {
              log.error("Exception", ex);
              return;
            }
            action.setTarget(target);
            action.setBackground(background);
            if (target.canManageThisChild(action))
              try {
                background.addActionQueue(action);
                action.setParent(this, target);
                session.save(action);
              } catch (final GeneralException ex) {
                log.error("Exception", ex);
              }
            }
          }

          session.getTransaction().commit();
        } catch (final Exception ex) {
          log.error("Exception", ex);
          session.getTransaction().rollback();
        }
      }
    }
  }

  /**
   * Adds a ping action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void addPingAll(final TreeItem item) {
    addActionAll(item, ActionPing.class);
  }

  /**
   * Adds a process action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void addProcessAll(final TreeItem item) {
    addActionAll(item, ActionGenericProcess.class);
  }

  /**
   * Adds a source action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void addSourceAll(final TreeItem item) {
    addActionAll(item, ActionGenericSrc.class);
  }

  /**
   * Adds a flood action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void addFloodAll(final TreeItem item) {
    addActionAll(item, ActionFlood.class);
  }

  /**
   * Adds a flood action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void addHTTPAll(final TreeItem item) {
    addActionAll(item, ActionHTTP.class);
  }

  /**
   * Adds an "SNMP explore" action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void exploreSNMP(final TreeItem item) {
    addActionAll(item, ActionSNMP.class);
  }

  /**
   * Adds an "nmap explore" action on every nodes under a specified root node.
   * @param item root node.
   * @return void.
   */
  // GUI thread
  private void exploreNmap(final TreeItem item) {
    addActionAll(item, ActionNmap.class);
  }

  /**
   * Synchronously get system descriptions via SNMP on every nodes under a specified root node.
   * @param item item root node.
   * @return void.
   */
  // GUI thread
  private void getSysdescr(final TreeItem item) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    // ne pas faire ce synchronized (sync_tree) car trop long donc bug apparent si on déplace une fenetre de graphe par ex
//    XXX ICI BLOCAGE QD FENETRE DEFILEMENT ET POPUP LONGUE ATTENTE
    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole("description[" + target.getAddress() + "]=\"" + target.getSNMPQuerier().getSysDescr() + "\"<BR/>");

          if (target.getSNMPQuerier().isSNMPCapable()) target.setImageHostSNMP();
          if (target.getSNMPQuerier().getLastSysdescr() != null) target.setType(target.getSNMPQuerier().getLastSysdescr());
        }
      }

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole("description[" + target.getAddress() + "]=\"" + target.getSNMPQuerier().getSysDescr() + "\"<BR/>");

          if (target.getSNMPQuerier().isSNMPCapable()) target.setImageHost6SNMP();
          if (target.getSNMPQuerier().getLastSysdescr() != null) target.setType(target.getSNMPQuerier().getLastSysdescr());
        }
      }
    }

    tg.end();
  }

  // GUI thread
  private void getGeneralInformation(final TreeItem item) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole(target.getSNMPQuerier().getGeneralInformation());
        }
      }
      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole(target.getSNMPQuerier().getGeneralInformation());
        }
      }
    }

    tg.end();
  }

  // GUI thread
  private void getRoutingTable(final TreeItem item) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole(target.getSNMPQuerier().getRoutingTable(tg));
        }
      }
      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole(target.getSNMPQuerier().getRoutingTable(tg));
        }
      }
    }

    tg.end();
  }

  // GUI thread
  private void getArpTable(final TreeItem item) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole(target.getSNMPQuerier().getArpTable(tg));
        }
      }
      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole(target.getSNMPQuerier().getArpTable(tg));
        }
      }
    }

    tg.end();
  }

  // GUI thread
  private void getGenericTable(final TreeItem item, final String tablename,
      final String oid, final int oids[], final String names[], Object fields[][]) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole(target.getSNMPQuerier().getGenericTable(tg, tablename, oid, oids, names, fields));
        }
      }
      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole(target.getSNMPQuerier().getGenericTable(tg, tablename, oid, oids, names, fields));
        }
      }
    }

    tg.end();
  }

  // GUI thread
  private void getMACTable(final TreeItem item) {
    final TransientGUI tg = new TransientGUI();
    tg.begin();

    synchronized (sync_tree) {
      tg.setSize(VisualElement.getSubElements(item, TargetIPv4.class).size() +
          VisualElement.getSubElements(item, TargetIPv6.class).size());

      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv4.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv4 target = (TargetIPv4) elt;
          appendConsole(target.getSNMPQuerier().getMACTable(tg));
        }
      }
      for (final VisualElement elt : VisualElement.getSubElements(item, TargetIPv6.class)) {
        if (tg.isFinished() == false) {
          tg.inc();

          final TargetIPv6 target = (TargetIPv6) elt;
          appendConsole(target.getSNMPQuerier().getMACTable(tg));
        }
      }
    }

    tg.end();
  }

  /**
   * Detaches every nodes under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeVisualElements(final TreeItem item) {
    synchronized (synchro) {
      synchronized (sync_tree) {
        final VisualElement visual_element = (VisualElement) item.getData(VisualElement.class.toString());
        if (item.getParentItem() == null) return;
        final VisualElement visual_parent = (VisualElement) item.getParentItem().getData(VisualElement.class.toString());

        final Session session = synchro.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try {
          visual_element.removeVisualElements(visual_parent);
          session.getTransaction().commit();
        } catch (final Exception ex) {
          log.error("Exception", ex);
          session.getTransaction().rollback();
        }
      }
    }
  }

  /**
   * Detaches action or view nodes under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeActionOrView(final TreeItem item, final Class clazz) {
    synchronized (synchro) {
      synchronized (sync_tree) {
        final Session session = synchro.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try  {
          for (final VisualElement action : VisualElement.getSubElements(item, clazz))
            if (action.getParents().size() == 1)
              action.removeVisualElements(action.getParents().get(0));
          session.getTransaction().commit();
        } catch (final Exception ex) {
          log.error("Exception", ex);
          session.getTransaction().rollback();
        }
      }
    }
  }

  /**
   * Detaches action nodes under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeActions(final TreeItem item) {
    removeActionOrView(item, net.fenyo.gnetwatch.actions.Action.class);
  }

  /**
   * Detaches ping actions under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeActionsPing(final TreeItem item) {
    removeActionOrView(item, ActionPing.class);
  }

  /**
   * Detaches flood actions under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeActionsFlood(final TreeItem item) {
    removeActionOrView(item, ActionFlood.class);
  }

  /**
   * Detaches "SNMP explore" actions under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeActionsExplore(final TreeItem item) {
    removeActionOrView(item, ActionSNMP.class);
  }

  /**
   * Detaches every views under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeViews(final TreeItem item) {
    removeActionOrView(item, DataView.class);
  }

  /**
   * Detaches ping views under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeViewsPing(final TreeItem item) {
    removeActionOrView(item, ReachableView.class);
  }

  /**
   * Detaches flood views under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeViewsFlood(final TreeItem item) {
    removeActionOrView(item, FloodView.class);
  }

  /**
   * Detaches "SNMP explore" views under a specified root node.
   * @param item item root node.
   * @return void.
   */
  private void removeViewsExplore(final TreeItem item) {
    removeActionOrView(item, BytesReceivedView.class);
    removeActionOrView(item, BytesSentView.class);
  }

  /**
   * Creates a new target or attaches an existing one to the current position.
   * @param target target.
   * @return boolean true if the target has been created or attached.
   */
  // GUI thread
  // appelï¿½ par boutons sur la gauche pour ts types de targets
  // utilise la selection courante
  // lock survey : sync_tree << synchro << HERE
  private boolean addTargetAtCurrentPosition(final Target target) {
    if (tree.getSelectionCount() == 1 &&
        (/* isSelectionPersistent() || */ isSelectionTransient())) {
      final VisualElement parent = (VisualElement) tree.getSelection()[0].getData(VisualElement.class.toString());
      if (parent != null && Target.class.isInstance(parent)) return target.addTarget(this, (Target) parent);
      else log.error("can not add a target under a null or non target parent: " + parent);
    }
    return false;
  }

  /**
   * Terminates the application.
   * @param none.
   * @return void.
   */
  // only called in the SWT thread
  private void exitApplication() {
    shell.close();
  }

  private void appendNetworkInterfaces() {
    try {
      // should be localized
      String str = "<HR/><B>Local network interfaces</B>";
      str += "<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'>";
      for (final Enumeration nifs = NetworkInterface.getNetworkInterfaces(); nifs.hasMoreElements(); ) {
        final NetworkInterface nif = (NetworkInterface) nifs.nextElement();
        str += "<TR><TD bgcolor='lightyellow' align='right'><B>" + htmlFace("interface name") + "</B></TD><TD bgcolor='lightyellow'><B>" + htmlFace(nif.getName())+ "</B></TD></TR>";
        str += "<TR><TD bgcolor='lightyellow' align='right'>" + htmlFace("display name") + "</TD><TD bgcolor='lightyellow'>" + htmlFace(nif.getDisplayName())+ "</TD></TR>";
        for (final Enumeration addrs = nif.getInetAddresses(); addrs.hasMoreElements(); ) {
          final InetAddress addr = (InetAddress) addrs.nextElement();
          if (Inet4Address.class.isInstance(addr))
            str += "<TR><TD bgcolor='lightyellow' align='right'>" + htmlFace("IPv4 address") + "</TD><TD bgcolor='lightyellow'>" + htmlFace(addr.getHostAddress()) + "</TD></TR>";
          if (Inet6Address.class.isInstance(addr))
            str += "<TR><TD bgcolor='lightyellow' align='right'>" + htmlFace("IPv6 address") + "</TD><TD bgcolor='lightyellow'>" + htmlFace(addr.getHostAddress()) + "</TD></TR>";
        }
      }
      str += "</TABLE>";
      appendConsole(str);
    } catch (final SocketException ex) {
      log.error("Exception", ex);
    }
  }

  private void appendDatabaseStatistics() {
    synchronized (synchro) {
      // should be localized
      String str = "<HR/><B>Database statistics</B><BR/>";

      Session session = getSynchro().getSessionFactory().getCurrentSession();
      session.beginTransaction();
      try {
        Integer count = (Integer) session.createQuery("select count(*) from EventGeneric").uniqueResult();
        str += "total number of events: " + count + "<BR/>";
        count = (Integer) session.createQuery("select count(*) from Target").uniqueResult();
        str += "total number of targets: " + count + "<BR/>";

        session.getTransaction().commit();

        appendConsole(str);
      } catch (final Exception ex) {
        log.error("Exception", ex);
        session.getTransaction().rollback();
      }
    }

    synchronized (synchro) {
      synchronized (sync_tree) {
        final Session session = synchro.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        try  {
          // should be localized
          String str = "<TABLE BORDER='0' BGCOLOR='black' cellspacing='1' cellpadding='1'>" +
          "<TR><TD bgcolor='lightyellow' align='left'><B>" + htmlFace("target") + "</B></TD>" +
          "<TD bgcolor='lightyellow' align='left'><B>" + htmlFace("class") + "</B></TD>" +
          "<TD bgcolor='lightyellow' align='right'><B>" + htmlFace("events") + "</B></TD></TR>";
          for (final VisualElement elt : visual_transient.getSubElements(Target.class)) {
            final Target target = (Target) elt;
            session.update(target);
            for (final String event_type : target.getEventLists().keySet()) {
              final Integer count = (Integer) session.createQuery("select count(*) from EventGeneric ev " +
                  "where ev.eventList = :event_list")
                  .setString("event_list", target.getEventLists().get(event_type).getId().toString()).uniqueResult();

              str += "<TR><TD bgcolor='lightyellow' align='left'>"
                + htmlFace((TargetInterface.class.isInstance(target) ? target.getParents().get(0).getItem() + ": " : "") + target.getItem()) +
              "</TD><TD bgcolor='lightyellow' align='left'>"
                + htmlFace(event_type.replaceAll(".*\\.", "")) + "</TD>" +
              "<TD bgcolor='lightyellow' align='right'>" + htmlFace("" + count)+ "</TD></TR>";
            }
          }

          session.getTransaction().commit();

          str += "</TABLE>";
          appendConsole(str);
        } catch (final Exception ex) {
          log.error("Exception", ex);
          session.getTransaction().rollback();
        }
      }
    }
  }

  /**
   * Instanciates the GUI objects.
   * GUI thread.
   * @param none.
   * @return void.
   */
  private void createGUI() throws UnknownHostException, SocketException, AlgorithmException {
    final GUI gui = this;

    synchronized (sync_tree) {

    display = new Display();
    shell = new Shell(display);
    shell.setText(getConfig().getString("version"));

    // Shell Layout
    layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginHeight = 2;
    layout.marginWidth = 2;
    layout.verticalSpacing = 1;
    shell.setLayout(layout);
    // shell.setBackground(display.getSystemColor(SWT.COLOR_BLUE));

    // Menu
    menu_bar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menu_bar);

    // Menu "File"
    menu_file = new Menu(shell, SWT.DROP_DOWN);
    menu_item_file = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_file.setText(getConfig().getString("menu_file"));
    menu_item_file.setMenu(menu_file);

    // Menu "file": MenuItem "Merge events now"
    menu_item_merge_now = new MenuItem(menu_file, SWT.PUSH);
    menu_item_merge_now.setText(getConfig().getString("menu_merge_events_now"));
    menu_item_merge_now.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        background.getMergeQueue().informCycle();
     }
    });

    new MenuItem(menu_file, SWT.SEPARATOR);

    // Menu "file": MenuItem "Exit"
    menu_item_exit = new MenuItem(menu_file, SWT.PUSH);
    menu_item_exit.setText(getConfig().getString("menu_exit"));
    menu_item_exit.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        gui.exitApplication();
      }
    });
    menu_item_exit.setImage(new Image(display, "pictures/exit.png"));

    // Menu "Discover"
    final Menu menu_discover = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_discover = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_discover.setText(getConfig().getString("menu_discover"));
    menu_item_discover.setMenu(menu_discover);

    // Menu "Discover": MenuItem "Start" and "Stop"
    final MenuItem menu_item_discover_start = new MenuItem(menu_discover, SWT.PUSH);
    final MenuItem menu_item_discover_stop = new MenuItem(menu_discover, SWT.PUSH);
    menu_item_discover_start.setText(getConfig().getString("menu_start"));
    menu_item_discover_start.setImage(new Image(display, "pictures/exec.png"));
    menu_item_discover_stop.setText(getConfig().getString("menu_stop"));
    menu_item_discover_stop.setImage(new Image(display, "pictures/fileclose.png"));
    menu_item_discover_stop.setEnabled(false);
    menu_item_discover_start.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_discover_start.setEnabled(false);
        item_discover_start.setEnabled(false);
        main.startDiscover();
        menu_item_discover_stop.setEnabled(true);
        item_discover_stop.setEnabled(true);
      }
    });
    menu_item_discover_stop.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_discover_start.setEnabled(true);
        item_discover_start.setEnabled(true);
        main.stopDiscover();
        menu_item_discover_stop.setEnabled(false);
        item_discover_stop.setEnabled(false);
      }
    });

    // Menu "Tree"
    final Menu menu_tree = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_tree = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_tree.setText(getConfig().getString("menu_tree"));
    menu_item_tree.setMenu(menu_tree);


    // Menu "Tree": MenuItem "Expand"
    final MenuItem menu_item_expand = new MenuItem(menu_tree, SWT.PUSH);
    menu_item_expand.setText(getConfig().getString("expand"));
    menu_item_expand.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) tree.getSelection()[0].setExpanded(true);
      }
    });

    // Menu "Tree": MenuItem "Merge"
    final MenuItem menu_item_merge = new MenuItem(menu_tree, SWT.PUSH);
    menu_item_merge.setText(getConfig().getString("collapse"));
    menu_item_merge.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) tree.getSelection()[0].setExpanded(false);
      }
    });

    new MenuItem(menu_tree, SWT.SEPARATOR);

    // Menu "Tree": MenuItem "Expand All"
    final MenuItem menu_item_expand_all = new MenuItem(menu_tree, SWT.PUSH);
    menu_item_expand_all.setText(getConfig().getString("menu_expand_all"));
    menu_item_expand_all.setImage(new Image(display, "pictures/show_table_column.png"));
    menu_item_expand_all.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) expandAll(tree.getSelection()[0]);
      }
    });

    // Menu "Tree": MenuItem "Merge All"
    final MenuItem menu_item_merge_all = new MenuItem(menu_tree, SWT.PUSH);
    menu_item_merge_all.setText(getConfig().getString("menu_collapse_all"));
    menu_item_merge_all.setImage(new Image(display, "pictures/hide_table_column.png"));
    menu_item_merge_all.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) mergeAll(tree.getSelection()[0]);
      }
    });

    // Menu "Target"
    final Menu menu_target = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_target = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_target.setText(getConfig().getString("menu_target"));
    menu_item_target.setMenu(menu_target);

    // Menu "Target": MenuItem "Set Credentials"
    menu_item_credentials = new MenuItem(menu_target, SWT.PUSH);
    menu_item_credentials.setText(getConfig().getString("menu_set_credentials"));
    menu_item_credentials.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        final SNMPQuerier querier;

        synchronized (sync_tree) {
          if (tree.getSelectionCount() == 1 &&
              (/* isSelectionPersistent() || */ isSelectionTransient())) {
            final VisualElement target = (VisualElement) tree.getSelection()[0].getData(VisualElement.class.toString());

            if (target != null && (TargetIPv4.class.isInstance(target) || TargetIPv6.class.isInstance(target))) {
              if (TargetIPv4.class.isInstance(target)) querier = ((TargetIPv4) target).getSNMPQuerier();
              else querier = ((TargetIPv6) target).getSNMPQuerier();
            } else return;
          } else return;
        }

        final int version = querier.getVersion();
        final int sec = querier.getSec();
        final int port_src = querier.getRetries();
        final int timeout = querier.getTimeout();
        final int port = querier.getPort();
        final String community = querier.getCommunity();
        final String username = querier.getUsername();
        final String password_auth = querier.getPasswordAuth();
        final String password_priv = querier.getPasswordPriv();
        final int pdu_max_size = querier.getPDUMaxSize();

        final DialogCredentials dialog = new DialogCredentials(gui, shell);
        dialog.setVersion(version);
        dialog.setSec(sec);
        dialog.setRetries(port_src);
        dialog.setTimeout(timeout);
        dialog.setPort(port);
        dialog.setCommunity(community);
        dialog.setUsername(username);
        dialog.setPasswordAuth(password_auth);
        dialog.setPasswordPriv(password_priv);
        dialog.setPDUMaxSize(pdu_max_size);

        dialog.open();

        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              session.update(querier);
              if (dialog.getVersion() != version) querier.setVersion(dialog.getVersion());
              if (dialog.getSec() != sec) querier.setSec(dialog.getSec());
              if (dialog.getRetries() != port_src) querier.setRetries(dialog.getRetries());
              if (dialog.getTimeout() != timeout) querier.setTimeout(dialog.getTimeout());
              if (dialog.getPort() != port) querier.setPort(dialog.getPort());
              if (dialog.getCommunity() != community) querier.setCommunity(dialog.getCommunity());
              if (dialog.getUsername() != username) querier.setUsername(dialog.getUsername());
              if (dialog.getPasswordAuth() != password_auth) querier.setPasswordAuth(dialog.getPasswordAuth());
              if (dialog.getPasswordPriv() != password_priv) querier.setPasswordPriv(dialog.getPasswordPriv());
              if (dialog.getPDUMaxSize() != pdu_max_size) querier.setPDUMaxSize(dialog.getPDUMaxSize());
              querier.update();
              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }
      }
    });

    // Menu "Target": MenuItem "Set IP options"
    menu_item_ip_options = new MenuItem(menu_target, SWT.PUSH);
    menu_item_ip_options.setText(getConfig().getString("menu_set_ip_options"));
    menu_item_ip_options.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        final IPQuerier querier;

        synchronized (sync_tree) {
          if (tree.getSelectionCount() == 1 &&
              (/* isSelectionPersistent() || */ isSelectionTransient())) {
            final VisualElement target = (VisualElement) tree.getSelection()[0].getData(VisualElement.class.toString());

            if (target != null && (TargetIPv4.class.isInstance(target) || TargetIPv6.class.isInstance(target))) {
              if (TargetIPv4.class.isInstance(target)) querier = ((TargetIPv4) target).getIPQuerier();
              else querier = ((TargetIPv6) target).getIPQuerier();
            } else return;
          } else return;
        }

        final int tos = querier.getTos();
        final int port_src = querier.getPortSrc();
        final int port_dst = querier.getPortDst();
        final int pdu_max_size = querier.getPDUMaxSize();

        final DialogIPOptions dialog = new DialogIPOptions(gui, shell);

        dialog.setTOS(tos);
        dialog.setPortSrc(port_src);
        dialog.setPortDst(port_dst);
        dialog.setPDUMaxSize(pdu_max_size);

        dialog.open();

        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              session.update(querier);
              if (dialog.getTOS() != tos) querier.setTos(dialog.getTOS());
              if (dialog.getPortSrc() != port_src) querier.setPortSrc(dialog.getPortSrc());
              if (dialog.getPortDst() != port_dst) querier.setPortDst(dialog.getPortDst());
              if (dialog.getPDUMaxSize() != pdu_max_size) querier.setPDUMaxSize(dialog.getPDUMaxSize());
              querier.update();
              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }
      }
    });

    // Menu "Target": MenuItem "Set HTTP/FTP options"
    menu_item_http_options = new MenuItem(menu_target, SWT.PUSH);
    menu_item_http_options.setText(getConfig().getString("menu_set_http_options"));
    menu_item_http_options.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        final IPQuerier querier;

        synchronized (sync_tree) {
          if (tree.getSelectionCount() == 1 &&
              (/* isSelectionPersistent() || */ isSelectionTransient())) {
            final VisualElement target = (VisualElement) tree.getSelection()[0].getData(VisualElement.class.toString());

            if (target != null && (TargetIPv4.class.isInstance(target) || TargetIPv6.class.isInstance(target))) {
              if (TargetIPv4.class.isInstance(target)) querier = ((TargetIPv4) target).getIPQuerier();
              else querier = ((TargetIPv6) target).getIPQuerier();
            } else return;
          } else return;
        }

        final int nparallel = querier.getNParallel();
        final String proxy_host = querier.getProxyHost();
        final int proxy_port = querier.getProxyPort();
        final String URL = querier.getURL();
//        final boolean reconnect = querier.getReconnect();
        final boolean use_proxy = querier.getUseProxy();

        final DialogHTTPOptions dialog = new DialogHTTPOptions(gui, shell);

        dialog.setNParallel(nparallel);
        dialog.setProxyHost(proxy_host);
        dialog.setProxyPort(proxy_port);
        dialog.setURL(URL);
//        dialog.setReconnect(reconnect);
        dialog.setUseProxy(use_proxy);

        dialog.open();

        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              session.update(querier);
              if (dialog.getNParallel() != nparallel) querier.setNParallel(dialog.getNParallel());
              if (dialog.getProxyHost() != proxy_host) querier.setProxyHost(dialog.getProxyHost());
              if (dialog.getProxyPort() != proxy_port) querier.setProxyPort(dialog.getProxyPort());
              if (dialog.getURL() != URL) querier.setURL(dialog.getURL());
//              if (dialog.getReconnect() != reconnect) querier.setReconnect(dialog.getReconnect());
              if (dialog.getUseProxy() != use_proxy) querier.setUseProxy(dialog.getUseProxy());
              querier.update();
              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }
      }
    });

    // Menu "Target": MenuItem "Set generic options"
    menu_item_generic_options = new MenuItem(menu_target, SWT.PUSH);
    menu_item_generic_options.setText(getConfig().getString("menu_set_generic_options"));
    menu_item_generic_options.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        final GenericQuerier querier;

        synchronized (sync_tree) {
          if (tree.getSelectionCount() == 1 &&
              (/* isSelectionPersistent() || */ isSelectionTransient())) {
            final VisualElement target = (VisualElement) tree.getSelection()[0].getData(VisualElement.class.toString());

            if (target != null && TargetGroup.class.isInstance(target)) {
              querier = ((TargetGroup) target).getGenericQuerier();
            } else return;
          } else return;
        }

        final String title = querier.getTitle();
        final String cmdline = querier.getCommandLine();
        final String workdir = querier.getWorkingDirectory();
        final String filename = querier.getFileName();
        final String unit = querier.getUnit();

        final DialogGeneric dialog = new DialogGeneric(gui, shell);

        dialog.setTitle(title);
        dialog.setCommandLine(cmdline);
        dialog.setWorkdir(workdir);
        dialog.setFilename(filename);
        dialog.setUnit(unit);

        dialog.open();

        if (dialog.isOK()) synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            try {
              session.update(querier);
              if (dialog.getTitle() != title) querier.setTitle(dialog.getTitle());
              if (dialog.getCommandLine() != cmdline) querier.setCommandLine(dialog.getCommandLine());
              if (dialog.getWorkdir() != workdir) querier.setWorkingDirectory(dialog.getWorkdir());
              if (dialog.getFilename() != filename) querier.setFileName(dialog.getFilename());
              if (dialog.getUnit() != unit) querier.setUnit(dialog.getUnit());

              session.getTransaction().commit();
            } catch (final Exception ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            }
          }
        }
      }
    });

    new MenuItem(menu_target, SWT.SEPARATOR);

    // Menu "Target": MenuItem "Add IPv4 Host"
    menu_item_add_host = new MenuItem(menu_target, SWT.PUSH);
    menu_item_add_host.setText(getConfig().getString("menu_add_ipv4"));
    menu_item_add_host.setImage(new Image(display, "pictures/network_local.png"));
    menu_item_add_host.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        target_host_push.notifyListeners(SWT.Selection, e);
      }
    });

    // Menu "Target": MenuItem "Add IPv6 Host"
    menu_item_add_host6 = new MenuItem(menu_target, SWT.PUSH);
    menu_item_add_host6.setText(getConfig().getString("menu_add_ipv6"));
    menu_item_add_host6.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        target_host6_push.notifyListeners(SWT.Selection, e);
      }
    });

    // Menu "Target": MenuItem "Add Range"
    menu_item_add_range = new MenuItem(menu_target, SWT.PUSH);
    menu_item_add_range.setText(getConfig().getString("menu_add_range"));
    menu_item_add_range.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        target_range_push.notifyListeners(SWT.Selection, e);
      }
    });

    // Menu "Target": MenuItem "Add Network"
    menu_item_add_network = new MenuItem(menu_target, SWT.PUSH);
    menu_item_add_network.setText(getConfig().getString("menu_add_network"));
    menu_item_add_network.setImage(new Image(display, "pictures/network.png"));
    menu_item_add_network.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        target_subnet_push.notifyListeners(SWT.Selection, e);
      }
    });

    // Menu "Target": MenuItem "Add Group"
    menu_item_add_group = new MenuItem(menu_target, SWT.PUSH);
    menu_item_add_group.setText(getConfig().getString("menu_add_group"));
    menu_item_add_group.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        target_group_push.notifyListeners(SWT.Selection, e);
      }
    });

    new MenuItem(menu_target, SWT.SEPARATOR);

    // Menu "Target": MenuItem "Remove Element"
    menu_item_remove_target = new MenuItem(menu_target, SWT.PUSH);
    menu_item_remove_target.setText(getConfig().getString("menu_remove_element"));
    menu_item_remove_target.setImage(new Image(display, "pictures/nomailappt.png"));
    menu_item_remove_target.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeVisualElements(tree.getSelection()[0]);
      }
    });

    // Menu "Action"
    final Menu menu_action = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_action = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_action.setText(getConfig().getString("menu_action"));
    menu_item_action.setMenu(menu_action);

/*
    // Menu "Action": MenuItem "Sysdescr"
    final MenuItem menu_item_sysdescr = new MenuItem(menu_action, SWT.PUSH);
    menu_item_sysdescr.setText(getConfig().getString("menu_get_system_description"));
    menu_item_sysdescr.setImage(new Image(display, "pictures/yahoo_idle-af.png"));
    menu_item_sysdescr.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) getSysdescr(tree.getSelection()[0]);
        }
    });
*/

    // Menu "Action": MenuItem "Add Ping"
    menu_item_add_ping = new MenuItem(menu_action, SWT.PUSH);
    menu_item_add_ping.setText(getConfig().getString("menu_ping_target"));
    menu_item_add_ping.setImage(new Image(display, "pictures/yahoo_idle-af.png"));
    menu_item_add_ping.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) addPingAll(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Add Generic Process"
    menu_item_add_process = new MenuItem(menu_action, SWT.PUSH);
    menu_item_add_process.setText(getConfig().getString("menu_generic_process"));
    menu_item_add_process.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) addProcessAll(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Add Generic Source"
    menu_item_add_source = new MenuItem(menu_action, SWT.PUSH);
    menu_item_add_source.setText(getConfig().getString("menu_generic_source"));
    menu_item_add_source.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) addSourceAll(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Add Flood"
    menu_item_add_flood = new MenuItem(menu_action, SWT.PUSH);
    menu_item_add_flood.setText(getConfig().getString("menu_flood_target"));
    menu_item_add_flood.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) addFloodAll(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Add Flood"
    menu_item_add_http = new MenuItem(menu_action, SWT.PUSH);
    menu_item_add_http.setText(getConfig().getString("menu_http_target"));
    menu_item_add_http.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) addHTTPAll(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Explore via SNMP"
    menu_item_explore_snmp = new MenuItem(menu_action, SWT.PUSH);
    menu_item_explore_snmp.setText(getConfig().getString("menu_explore_via_snmp"));
    menu_item_explore_snmp.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) exploreSNMP(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Explore via Nmap"
    menu_item_explore_nmap = new MenuItem(menu_action, SWT.PUSH);
    menu_item_explore_nmap.setText(getConfig().getString("menu_explore_via_nmap"));
    menu_item_explore_nmap.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) exploreNmap(tree.getSelection()[0]);
      }
    });

    new MenuItem(menu_action, SWT.SEPARATOR);

    // Menu "Action": MenuItem "Drop Action"
    menu_item_remove_action = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_action.setText(getConfig().getString("menu_drop_action"));
    menu_item_remove_action.setImage(new Image(display, "pictures/button_cancel-af.png"));
    menu_item_remove_action.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeActions(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop Action Ping"
    menu_item_remove_action_ping = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_action_ping.setText(getConfig().getString("menu_drop_action_ping"));
    menu_item_remove_action_ping.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeActionsPing(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop Action Flood"
    menu_item_remove_action_flood = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_action_flood.setText(getConfig().getString("menu_drop_action_flood"));
    menu_item_remove_action_flood.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeActionsFlood(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop Action Explore"
    menu_item_remove_action_explore = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_action_explore.setText(getConfig().getString("menu_drop_action_explore"));
    menu_item_remove_action_explore.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeActionsExplore(tree.getSelection()[0]);
      }
    });

    new MenuItem(menu_action, SWT.SEPARATOR);

    // Menu "Action": MenuItem "Drop View"
    menu_item_remove_view = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_view.setText(getConfig().getString("menu_drop_view"));
    menu_item_remove_view.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeViews(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop View Ping"
    menu_item_remove_view_ping = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_view_ping.setText(getConfig().getString("menu_drop_view_ping"));
    menu_item_remove_view_ping.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeViewsPing(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop View Flood"
    menu_item_remove_view_flood = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_view_flood.setText(getConfig().getString("menu_drop_view_flood"));
    menu_item_remove_view_flood.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeViewsFlood(tree.getSelection()[0]);
      }
    });

    // Menu "Action": MenuItem "Drop View Explore"
    menu_item_remove_view_explore = new MenuItem(menu_action, SWT.PUSH);
    menu_item_remove_view_explore.setText(getConfig().getString("menu_drop_view_explore"));
    menu_item_remove_view_explore.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        if (tree.getSelectionCount() == 1) removeViewsExplore(tree.getSelection()[0]);
      }
    });

    // Menu "View"
    final Menu menu_view = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_view = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_view.setText(getConfig().getString("menu_snmp_info"));
    menu_item_view.setMenu(menu_view);

    // Menu "View": MenuItem "Get SysDescr"
    final MenuItem menu_item_open_view = new MenuItem(menu_view, SWT.PUSH);
    menu_item_open_view.setText(getConfig().getString("menu_get_system_description"));
    menu_item_open_view.setImage(new Image(display, "pictures/multirow.png"));
    menu_item_open_view.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) getSysdescr(tree.getSelection()[0]);
        }
    });

    // Menu "View": MenuItem "Get General Information"
    final MenuItem menu_item_get_general_information = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_general_information.setText(getConfig().getString("menu_get_general_information"));
    menu_item_get_general_information.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) getGeneralInformation(tree.getSelection()[0]);
        }
    });

    // Menu "View": MenuItem "Get Routing Table"
    final MenuItem menu_item_get_routing_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_routing_table.setText(getConfig().getString("menu_get_routing_table"));
    menu_item_get_routing_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        // if (tree.getSelectionCount() == 1) getRoutingTable(tree.getSelection()[0]);
        if (tree.getSelectionCount() == 1)
          getGenericTable(tree.getSelection()[0],
            "route",
            "1.3.6.1.2.1.4.21.1",
            new int [] {9, 1, 11, 2, 7, 8, 10, 3, 4, 5, 6, 12, 13},
            new String [] {"proto", "destination", "netmask", "interface", "next hop",
            "type", "age", "metric1", "metric2", "metric3", "metric4", "metric5", "info"},
            new Object [][] {
              {0, "gnetwatch error", "other", "local",
                "netmgmt", "icmp", "egp", "ggp", "hello", "rip", "is-is", "es-is",
                "ciscoIgrp", "bbnSpfIgp", "ospf", "bgp"},
              {5, "gnetwatch error", "other", "invalid",
                "direct", "indirect"},
              {3}
            });
        }
    });

    // Menu "View": MenuItem "Get Arp Table"
    final MenuItem menu_item_get_arp_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_arp_table.setText(getConfig().getString("menu_get_arp_table"));
    menu_item_get_arp_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) getArpTable(tree.getSelection()[0]);
        }
    });

    // Menu "View": MenuItem "Get Interfaces Table"
    final MenuItem menu_item_get_ifs_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_ifs_table.setText(getConfig().getString("menu_get_ifs_table"));
    menu_item_get_ifs_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      // snmptranslate -Td IF-MIB::ifMtu
      // ::= { iso(1) org(3) dod(6) internet(1) mgmt(2) mib-2(1) interfaces(2) ifTable(2) ifEntry(1) 4 }
      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1)
          getGenericTable(tree.getSelection()[0],
            "interface",
            "1.3.6.1.2.1.2.2.1",
            // echo {1..22} | sed 's/ /, /g'
            new int [] {1, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22},
            // res.txt : dï¿½finition de ifEntry
            // echo `cat res.txt | grep -v ',' | grep if | sed 's/ */"/' | sed 's/$/"/'` | sed 's/ /, /g'
            new String [] {"ifIndex", "ifDescr", "ifType", "ifMtu", "ifSpeed", "ifPhysAddress",
            "ifAdminStatus", "ifOperStatus", "ifLastChange", "ifInOctets", "ifInUcastPkts",
            "ifInNUcastPkts", "ifInDiscards", "ifInErrors", "ifInUnknownProtos", "ifOutOctets",
            "ifOutUcastPkts", "ifOutNUcastPkts", "ifOutDiscards", "ifOutErrors", "ifOutQLen", "ifSpecific"},
            new Object [][] {
              {1},
              // type.txt : dï¿½finition de ifType
              // echo `cat type.txt | fgrep '(' | sed 's/ *//' | sed 's/(.*/"/' | sed 's/^/"/'` | sed 's/ /, /g'
              // et rajouter "gnetwatch error" au dï¿½but
              // if Type ::= { ifEntry 3 }
              {2, "gnetwatch error", "other", "regular1822", "hdh1822", "ddn-x25",
                "rfc877-x25", "ethernet-csmacd", "iso88023-csmacd", "iso88024-tokenBus",
                "iso88025-tokenRing", "iso88026-man", "starLan", "proteon-10Mbit",
                "proteon-80Mbit", "hyperchannel", "fddi", "lapb", "sdlc", "ds1",
                "e1", "basicISDN", "primaryISDN", "propPointToPointSerial", "ppp",
                "softwareLoopback", "eon", "ethernet-3Mbit", "nsip", "slip", "ultra",
                "ds3", "sip", "frame-relay"},
              {6, "gnetwatch error", "up", "down", "testing"},
              {7, "gnetwatch error", "up", "down", "testing"}
            });
      }
    });

    // Menu "View": MenuItem "Get .3 Stats Table"
    final MenuItem menu_item_get_dot3stats_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_dot3stats_table.setText(getConfig().getString("menu_get_dot3stats_table"));
    menu_item_get_dot3stats_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      // snmptranslate -m ALL -M"/usr/share/snmp/mibs:$HOME/MIBs/Cisco:$HOME/MIBS/3Com" -Td ETHERLIKE-MIB::dot3StatsDuplexStatus 2> /dev/null
      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1)
          getGenericTable(tree.getSelection()[0],
            "Ethernet",
            "1.3.6.1.2.1.10.7.2.1",
            new int [] {1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 16, 18, 19, 20, 21},
            // res.txt : dï¿½finition de ifEntry
            // echo `egrep '^   ' res.txt | grep ',' | sed 's/ */"/' | sed 's/ .*/"/' ` | sed  's/ /, /g'
            new String [] {"dot3StatsIndex", "ifName", "dot3StatsAlignmentErrors", "dot3StatsFCSErrors",
            "dot3StatsSingleCollisionFrames", "dot3StatsMultipleCollisionFrames", "dot3StatsSQETestErrors",
            "dot3StatsDeferredTransmissions", "dot3StatsLateCollisions", "dot3StatsExcessiveCollisions",
            "dot3StatsInternalMacTransmitErrors", "dot3StatsCarrierSenseErrors", "dot3StatsFrameTooLongs",
            "dot3StatsInternalMacReceiveErrors", "dot3StatsSymbolErrors", "dot3StatsDuplexStatus",
            "dot3StatsRateControlAbility", "dot3StatsRateControlStatus"},
            new Object [][] {
              {1},
              {15, "gnetwatch error", "duplex mode unknown", "half-duplex", "full-duplex"},
              {16, "false", "true"},
              {17, "gnetwatch error", "control off", "control on", "unknown control mode"},
            });
      }
    });

    new MenuItem(menu_view, SWT.SEPARATOR);

    // Menu "View": MenuItem "Get VLAN Table"
    final MenuItem menu_item_get_vlan_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_vlan_table.setText(getConfig().getString("menu_get_vlan_table"));
    menu_item_get_vlan_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1)
          getGenericTable(tree.getSelection()[0],
            "VLANs",
            "1.3.6.1.4.1.9.9.46.1.3.1.1",
            new int [] {18, 4, 2},
            new String [] {"vlan ID", "vlan name", "vlan state"},
            new Object [][] {
            {0, "#idx"},
            {2, "gnetwatch error", "operational", "suspended", "mtuTooBigForDevice", "mtuTooBigForTrunk"}
          });
      }
    });

    // Menu "View": MenuItem "Mac2If"
    final MenuItem menu_item_get_mac2if_table = new MenuItem(menu_view, SWT.PUSH);
    menu_item_get_mac2if_table.setText(getConfig().getString("menu_mac2if_table"));
    menu_item_get_mac2if_table.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1)
          getMACTable(tree.getSelection()[0]);
      }
    });

    /*
    // Menu "View": MenuItem getConfig().getString("menu_open_graph")
    final MenuItem menu_item_open_graph = new MenuItem(menu_view, SWT.PUSH);
    menu_item_open_graph.setText(getConfig().getString("menu_open_graph"));
    menu_item_open_graph.setImage(new Image(display, "pictures/oscilloscope-af.png"));
    menu_item_open_graph.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        log.debug("open graph");
      }
    });

    // Menu "View": MenuItem getConfig().getString("menu_close_view")
    final MenuItem menu_item_close_view = new MenuItem(menu_view, SWT.PUSH);
    menu_item_close_view.setText(getConfig().getString("menu_close_view"));
    menu_item_close_view.setImage(new Image(display, "pictures/multirow-af-cross.png"));
    menu_item_close_view.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        log.debug("close view");
      }
    });
    */

    // Menu "Status"
    final Menu menu_status = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_status = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_status.setText(getConfig().getString("menu_status"));
    menu_item_status.setMenu(menu_status);

    // Menu "Status": MenuItem "Local interfaces"
    final MenuItem menu_item_local_interfaces = new MenuItem(menu_status, SWT.PUSH);
    menu_item_local_interfaces.setText(getConfig().getString("menu_local_interfaces"));
    menu_item_local_interfaces.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        appendNetworkInterfaces();
      }
    });

    // Menu "Status": MenuItem "Database statistics"
    final MenuItem menu_item_database_statistics = new MenuItem(menu_status, SWT.PUSH);
    menu_item_database_statistics.setText(getConfig().getString("menu_database_statistics"));
    menu_item_database_statistics.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        appendDatabaseStatistics();
      }
    });

    // Menu "Window"
    final Menu menu_window = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_window = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_window.setText(getConfig().getString("menu_window"));
    menu_item_window.setMenu(menu_window);

    // Menu "Window": MenuItem "Hide left panel"
    menu_item_hide = new MenuItem(menu_window, SWT.PUSH);
    menu_item_hide.setText(getConfig().getString("menu_hide"));
    menu_item_hide.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_hide.setEnabled(false);
        menu_item_show.setEnabled(true);
        groups_composite_grid_data.exclude = true;
        groups_composite.setVisible(false);
        horizontal_composite.layout();
      }
    });

    // Menu "Window": MenuItem "Show left panel"
    menu_item_show = new MenuItem(menu_window, SWT.PUSH);
    menu_item_show.setText(getConfig().getString("menu_show"));
    menu_item_show.setEnabled(false);
    menu_item_show.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_hide.setEnabled(true);
        menu_item_show.setEnabled(false);
        groups_composite_grid_data.exclude = false;
        groups_composite.setVisible(true);
        horizontal_composite.layout();
      }
    });

    new MenuItem(menu_window, SWT.SEPARATOR);

    // Menu "Window": MenuItem "Clear information panel"
    menu_item_clear = new MenuItem(menu_window, SWT.PUSH);
    menu_item_clear.setText(getConfig().getString("menu_clear"));
    menu_item_clear.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        clearConsole();
      }
    });

    // Menu "Help"
    final Menu menu_help = new Menu(shell, SWT.DROP_DOWN);
    final MenuItem menu_item_help = new MenuItem(menu_bar, SWT.CASCADE);
    menu_item_help.setText(getConfig().getString("menu_help"));
    menu_item_help.setMenu(menu_help);

    // Menu "Help": MenuItem "Welcome"
    final MenuItem menu_item_welcome = new MenuItem(menu_help, SWT.PUSH);
    menu_item_welcome.setText(getConfig().getString("menu_welcome"));
    menu_item_welcome.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        tab_folder.setSelection(tab_item1);
      }
    });

    // Menu "Help": MenuItem "Help Contents"
    final MenuItem menu_item_help_contents = new MenuItem(menu_help, SWT.PUSH);
    menu_item_help_contents.setText(getConfig().getString("menu_help_contents"));
    menu_item_help_contents.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        tab_folder.setSelection(tab_item2);
      }
    });

    new MenuItem(menu_help, SWT.SEPARATOR);

    // Menu "Help": MenuItem "About"
    final MenuItem menu_item_about = new MenuItem(menu_help, SWT.PUSH);
    menu_item_about.setText(getConfig().getString("menu_about"));
    menu_item_about.setImage(new Image(display, "pictures/info.png"));
    menu_item_about.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        final DialogAbout dialog = new DialogAbout(gui, shell);
        dialog.open();
      }
    });

    /*
    new MenuItem(menu_help, SWT.SEPARATOR);

    // Menu "Help": MenuItem "Software Update"
    final MenuItem menu_item_new_version = new MenuItem(menu_help, SWT.PUSH);
    menu_item_new_version.setText(getConfig().getString("menu_software_update"));
    menu_item_new_version.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        log.debug("new version");
      }
    });
    */

    /*
     * ToolBar
     */
    toolbar = new ToolBar(shell, SWT.FLAT);
    final ToolItem item_exit = new ToolItem(toolbar, SWT.PUSH);
    item_exit.setImage(new Image(display, "pictures/exit.png"));
    item_exit.setToolTipText(getConfig().getString("exit"));
    item_exit.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_exit.notifyListeners(SWT.Selection, e);
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    item_discover_start = new ToolItem(toolbar, SWT.PUSH);
    item_discover_start.setImage(new Image(display, "pictures/exec.png"));
    item_discover_start.setToolTipText(getConfig().getString("start_sniffer"));
    item_discover_start.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_discover_start.notifyListeners(SWT.Selection, e);
      }
    });

    item_discover_stop = new ToolItem(toolbar, SWT.PUSH);
    item_discover_stop.setImage(new Image(display, "pictures/fileclose.png"));
    item_discover_stop.setToolTipText(getConfig().getString("stop_sniffer"));
    item_discover_stop.setEnabled(false);
    item_discover_stop.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_discover_stop.notifyListeners(SWT.Selection, e);
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    final ToolItem item_expand_all = new ToolItem(toolbar, SWT.PUSH);
    item_expand_all.setImage(new Image(display, "pictures/show_table_column.png"));
    item_expand_all.setToolTipText(getConfig().getString("expand_all"));
    item_expand_all.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_expand_all.notifyListeners(SWT.Selection, e);
      }
    });

    final ToolItem item_merge_all = new ToolItem(toolbar, SWT.PUSH);
    item_merge_all.setImage(new Image(display, "pictures/hide_table_column.png"));
    item_merge_all.setToolTipText(getConfig().getString("collapse_all"));
    item_merge_all.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_merge_all.notifyListeners(SWT.Selection, e);
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    item_add_host = new ToolItem(toolbar, SWT.PUSH);
    item_add_host.setImage(new Image(display, "pictures/network_local.png"));
    item_add_host.setToolTipText(getConfig().getString("add_ipv4_host"));
    item_add_host.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_add_host.notifyListeners(SWT.Selection, e);
      }
    });

    item_add_network = new ToolItem(toolbar, SWT.PUSH);
    item_add_network.setImage(new Image(display, "pictures/network.png"));
    item_add_network.setToolTipText(getConfig().getString("add_network"));
    item_add_network.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_add_network.notifyListeners(SWT.Selection, e);
      }
    });

    item_remove_target = new ToolItem(toolbar, SWT.PUSH);
    item_remove_target.setImage(new Image(display, "pictures/nomailappt.png"));
    item_remove_target.setToolTipText(getConfig().getString("remove_element"));
    item_remove_target.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_remove_target.notifyListeners(SWT.Selection, e);
      }
    });

    new ToolItem(toolbar, SWT.SEPARATOR);

    item_add_ping = new ToolItem(toolbar, SWT.PUSH);
    item_add_ping.setImage(new Image(display, "pictures/yahoo_idle-af.png"));
    item_add_ping.setToolTipText("ping");
    item_add_ping.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_add_ping.notifyListeners(SWT.Selection, e);
      }
    });

    item_remove_action = new ToolItem(toolbar, SWT.PUSH);
    item_remove_action.setImage(new Image(display, "pictures/button_cancel-af.png"));
    item_remove_action.setToolTipText(getConfig().getString("drop_action"));
    item_remove_action.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_remove_action.notifyListeners(SWT.Selection, e);
      }
    });

/*
    new ToolItem(toolbar, SWT.SEPARATOR);

    final ToolItem item_open_view = new ToolItem(toolbar, SWT.PUSH);
    item_open_view.setImage(new Image(display, "pictures/multirow.png"));
    item_open_view.setToolTipText("open view");
    item_open_view.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_open_view.notifyListeners(SWT.Selection, e);
      }
    });
*/

    /*
    final ToolItem item_open_graph = new ToolItem(toolbar, SWT.PUSH);
    item_open_graph.setImage(new Image(display, "pictures/oscilloscope-af.png"));
    item_open_graph.setToolTipText("open graph");
    item_open_graph.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_open_graph.notifyListeners(SWT.Selection, e);
      }
    });

    final ToolItem item_close_view = new ToolItem(toolbar, SWT.PUSH);
    item_close_view.setImage(new Image(display, "pictures/multirow-af-cross.png"));
    item_close_view.setToolTipText("close view");
    item_close_view.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_close_view.notifyListeners(SWT.Selection, e);
      }
    });
*/

    new ToolItem(toolbar, SWT.SEPARATOR);

    ToolItem item_about = new ToolItem(toolbar, SWT.PUSH);
    item_about.setImage(new Image(display, "pictures/info.png"));
    item_about.setToolTipText(getConfig().getString("help"));
    item_about.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        menu_item_about.notifyListeners(SWT.Selection, e);
      }
    });

    // GridData
    toolbar_grid_data = new GridData(GridData.FILL_HORIZONTAL);
    toolbar.setLayoutData(toolbar_grid_data);

    // vertical Sash for Composite and text console
    vertical_sash = new SashForm(shell, SWT.VERTICAL);
    vertical_sash_grid_data = new GridData(GridData.FILL_BOTH);
    vertical_sash.setLayoutData(vertical_sash_grid_data);
    // vertical_sash.setBackground(display.getSystemColor(SWT.COLOR_RED));

    // horizontal Composite
    horizontal_composite = new Composite(vertical_sash, SWT.FLAT);
    // composite.setBackground(display.getSystemColor(SWT.COLOR_RED));
    horizontal_composite_layout = new GridLayout();
    horizontal_composite_layout.numColumns = 2;
    horizontal_composite_layout.marginHeight = 0;
    horizontal_composite_layout.marginWidth = 0;
    horizontal_composite.setLayout(horizontal_composite_layout);

    // text console
    text_console = new Browser(vertical_sash, SWT.BORDER | SWT.FILL);
    appendConsole(htmlFace("GNetWatch - &copy; 2006, 2007, 2008<BR/>"));
    text_console.addProgressListener(new ProgressListener() {
      public void changed(ProgressEvent e) {}
      public void completed(ProgressEvent e) {
        // il faudrait mettre une valeur max plutï¿½t que ï¿½a...
        if (text_console_do_not_go_on_top > 0) text_console_do_not_go_on_top--;
        else text_console.execute("window.scroll(0,1000000);");
      }
    });

    // set vertical_sash relative weights
    vertical_sash.setWeights(new int [] { 4, 1 });

    // Composite for groups at left
    groups_composite = new Composite(horizontal_composite, SWT.FLAT);
    groups_composite_layout = new RowLayout(SWT.VERTICAL);
    groups_composite_layout.fill = true;
    groups_composite_layout.marginTop = 0;
    groups_composite_layout.marginBottom = 0;
    groups_composite.setLayout(groups_composite_layout);
    groups_composite_grid_data = new GridData(GridData.FILL_VERTICAL);
    groups_composite.setLayoutData(groups_composite_grid_data);

    // Group for subnet targets

    group_target_subnet = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_target_subnet_layout = new GridLayout();
    group_target_subnet_layout.numColumns = 2;
    group_target_subnet.setLayout(group_target_subnet_layout);
    group_target_subnet.setText(getConfig().getString("create_network_target"));

    label1 = new Label(group_target_subnet, SWT.SHADOW_IN);
    label1.setText(getConfig().getString("address"));
    label1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_subnet_address = new Text(group_target_subnet, SWT.READ_ONLY | SWT.SINGLE);
    target_subnet_address.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END /*| GridData.GRAB_HORIZONTAL*/ | GridData.FILL_VERTICAL));
    target_subnet_address.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_subnet_address.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    new IpAddressEditor(getConfig(), target_subnet_address);

    label2 = new Label(group_target_subnet, SWT.SHADOW_IN);
    label2.setText(getConfig().getString("netmask"));
    label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_subnet_mask = new Text(group_target_subnet, SWT.READ_ONLY | SWT.SINGLE);
    target_subnet_mask.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END /*| GridData.GRAB_HORIZONTAL*/ | GridData.FILL_VERTICAL));
    target_subnet_mask.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_subnet_mask.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_subnet_mask.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    new IpAddressEditor(getConfig(), target_subnet_mask);

    new Label(group_target_subnet, SWT.SHADOW_NONE);

    target_subnet_push = new Button(group_target_subnet, SWT.PUSH);
    target_subnet_push.setText(getConfig().getString("add_subnet"));
    target_subnet_push.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    target_subnet_push.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              addTargetAtCurrentPosition(new TargetIPv4Subnet("added by GUI",
                  GenericTools.stringToInet4Address(target_subnet_address.getText()),
                  GenericTools.stringToInet4Address(target_subnet_mask.getText())));
              target_subnet_address.setText("000.000.000.000");
              target_subnet_mask.setText("000.000.000.000");
              session.getTransaction().commit();
            } catch (final UnknownHostException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            } catch (final AlgorithmException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
              System.exit(1);
            }
          }
        }
      }
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    // Group for range targets

    group_target_range = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_target_range_layout = new GridLayout();
    group_target_range_layout.numColumns = 2;
    group_target_range.setLayout(group_target_range_layout);
    group_target_range.setText(getConfig().getString("create_range_target"));

    label3 = new Label(group_target_range, SWT.SHADOW_IN);
    label3.setText(getConfig().getString("first_address"));
    label3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_range_begin = new Text(group_target_range, SWT.READ_ONLY | SWT.SINGLE);
    target_range_begin.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    target_range_begin.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_range_begin.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    new IpAddressEditor(getConfig(), target_range_begin);

    label4 = new Label(group_target_range, SWT.SHADOW_IN);
    label4.setText(getConfig().getString("last_address"));
    label4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_range_end = new Text(group_target_range, SWT.READ_ONLY | SWT.SINGLE);
    target_range_end.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    target_range_end.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_range_end.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    new IpAddressEditor(getConfig(), target_range_end);

    new Label(group_target_range, SWT.SHADOW_NONE);

    target_range_push = new Button(group_target_range, SWT.PUSH);
    target_range_push.setText(getConfig().getString("add_range"));
    target_range_push.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    target_range_push.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              addTargetAtCurrentPosition(new TargetIPv4Range("added by GUI",
                  GenericTools.stringToInet4Address(target_range_begin.getText()),
                  GenericTools.stringToInet4Address(target_range_end.getText())));
              target_range_begin.setText("000.000.000.000");
              target_range_end.setText("000.000.000.000");
              session.getTransaction().commit();
            } catch (final UnknownHostException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            } catch (final AlgorithmException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
              System.exit(1);
            }
          }
        }
      }
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    // Group for host targets

    group_target_host = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_target_host_layout = new GridLayout();
    group_target_host_layout.numColumns = 2;
    group_target_host.setLayout(group_target_host_layout);
    group_target_host.setText(getConfig().getString("create_ipv4_target"));

    label5 = new Label(group_target_host, SWT.SHADOW_IN);
    label5.setText(getConfig().getString("host_address"));
    label5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_host_value = new Text(group_target_host, SWT.READ_ONLY | SWT.SINGLE);
    target_host_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    target_host_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_host_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
    new IpAddressEditor(getConfig(), target_host_value);

    new Label(group_target_host, SWT.SHADOW_NONE);

    target_host_push = new Button(group_target_host, SWT.PUSH);
    target_host_push.setText(getConfig().getString("add_ipv4_host"));
    target_host_push.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    target_host_push.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        synchronized (synchro) {
          synchronized (sync_tree) {
            final Session session = synchro.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            try  {
              final TargetIPv4 foo = new TargetIPv4("added by GUI",
                  GenericTools.stringToInet4Address(target_host_value.getText()), snmp_manager);
              if (addTargetAtCurrentPosition(foo) == true) foo.checkSNMPAwareness();
              target_host_value.setText("000.000.000.000");
              session.getTransaction().commit();
            } catch (final UnknownHostException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
            } catch (final AlgorithmException ex) {
              log.error("Exception", ex);
              session.getTransaction().rollback();
              System.exit(1);
            }
          }
        }
      }
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    target_host_push.addControlListener(new ControlListener() {
      public void controlMoved(final ControlEvent e) {}

      public void controlResized(final ControlEvent e) {
        final GC gc = new GC(target_host_value);
        gc.setFont(target_host_value.getFont());
        ((GridData) (target_group_value.getLayoutData())).widthHint = gc.stringExtent(target_host_value.getText()).x;
        ((GridData) (target_host6_value.getLayoutData())).widthHint = gc.stringExtent(target_host_value.getText()).x;
        shell.changed(new Control [] { target_group_value, target_host6_value });
      }
    });

    // Group for IPv6 targets

    group_target_host6 = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_target_host6_layout = new GridLayout();
    group_target_host6_layout.numColumns = 2;
    group_target_host6.setLayout(group_target_host6_layout);
    group_target_host6.setText(getConfig().getString("create_ipv6_target"));

    label7 = new Label(group_target_host6, SWT.SHADOW_IN);
    label7.setText(getConfig().getString("ipv6_address"));
    label7.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_host6_value = new Text(group_target_host6, SWT.SINGLE);
    target_host6_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    target_host6_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_host6_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

    new Label(group_target_host6, SWT.SHADOW_NONE);

    target_host6_push = new Button(group_target_host6, SWT.PUSH);
    target_host6_push.setText(getConfig().getString("add_ipv6_host"));
    target_host6_push.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    target_host6_push.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        try {
          final Inet6Address address = GenericTools.stringToInet6Address(target_host6_value.getText());
          if (address == null) {
            final MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            dialog.setText(getConfig().getString("gnetwatch_error"));
            dialog.setMessage(getConfig().getString("cannot_parse_ipv6"));
            dialog.open();
            return;
          }
          synchronized (synchro) {
            synchronized (sync_tree) {
              final Session session = synchro.getSessionFactory().getCurrentSession();
              session.beginTransaction();
              try  {
                final TargetIPv6 foo = new TargetIPv6("added by GUI", address, snmp_manager);
                if (addTargetAtCurrentPosition(foo) == true) foo.checkSNMPAwareness();
                target_host6_value.setText("");
                session.getTransaction().commit();
              } catch (Exception ex) {
                session.getTransaction().rollback();
                throw ex;
              }
            }
          }
        } catch (final UnknownHostException ex) {
          log.error("Exception", ex);
          final MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          dialog.setText("GNetWatch - Error");
          dialog.setMessage(getConfig().getString("cannot_parse_ipv6"));
          dialog.open();
        } catch (final AlgorithmException ex) {
          log.error("Exception", ex);
          System.exit(1);
        } catch (final Exception ex) {
          log.error("Exception", ex);
        }
      }
      public void widgetSelected(final SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    // Group for group targets

    group_target_group = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_target_group_layout = new GridLayout();
    group_target_group_layout.numColumns = 2;
    group_target_group.setLayout(group_target_group_layout);
    group_target_group.setText(getConfig().getString("create_group_target"));

    label6 = new Label(group_target_group, SWT.SHADOW_IN);
    label6.setText(getConfig().getString("group_name"));
    label6.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    target_group_value = new Text(group_target_group, SWT.SINGLE);
    target_group_value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL));
    target_group_value.setBackground(new Color(display, bglevel, bglevel, bglevel));
    target_group_value.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

    new Label(group_target_group, SWT.SHADOW_NONE);

    target_group_push = new Button(group_target_group, SWT.PUSH);
    target_group_push.setText(getConfig().getString("add_group"));
    target_group_push.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    target_group_push.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        try {
          if (target_group_value.getText() != "")
            synchronized (synchro) {
              synchronized (sync_tree) {
                final Session session = synchro.getSessionFactory().getCurrentSession();
                session.beginTransaction();
                try {
                  addTargetAtCurrentPosition(new TargetGroup("added by GUI", target_group_value.getText()));
                  session.getTransaction().commit();
                } catch (Exception ex) {
                  session.getTransaction().rollback();
                  throw ex;
                }
              }
            } else {
              final MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              dialog.setText(getConfig().getString("gnetwatch_error"));
              dialog.setMessage(getConfig().getString("cannot_add_group"));
              dialog.open();
            } target_group_value.setText("");
        } catch (final AlgorithmException ex) {
          log.error("Exception", ex);
          System.exit(1);
        } catch (final Exception ex) {
          log.error("Exception", ex);
        }
      }
      public void widgetSelected(final SelectionEvent e) {
        widgetDefaultSelected(e);
      }
    });

    // vertical space (hint to be sure left groups are visible at launch time)
    new Label(groups_composite, SWT.SHADOW_ETCHED_IN);

    // group container for data

    /*
    group_data = new Group(groups_composite, SWT.SHADOW_ETCHED_IN);
    group_data.setText("Data");
    group_data_layout = new GridLayout();
    group_data_layout.numColumns = 2;
    group_data.setLayout(group_target_subnet_layout);
    */

    // Sash for Tree and Label
    horizontal_sash = new SashForm(horizontal_composite, SWT.HORIZONTAL);
    horizontal_sash_grid_data = new GridData(GridData.FILL_BOTH);
    horizontal_sash.setLayoutData(horizontal_sash_grid_data);

    // Tree
    tree = new Tree(horizontal_sash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    tree.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        if (tree.getSelectionCount() == 1) {
          TreeItem current_item = tree.getSelection()[0];
          if (current_item != null) {
            synchronized (synchro) {
            synchronized (sync_tree) {
              final VisualElement visual_element = (VisualElement) current_item.getData(VisualElement.class.toString());
              if (visual_element != null) visual_element.informSelected();
            }
            }
            widgetSelected(e);
          }
        }
      }

      public void widgetSelected(SelectionEvent e) {
        gui.updateEnableState();
        if (tree.getSelectionCount() == 1) {
          TreeItem current_item = tree.getSelection()[0];
          if (current_item != null) {
            synchronized (sync_tree) {
              final VisualElement visual_element = (VisualElement) current_item.getData(VisualElement.class.toString());

              if (visual_element.getProgress() != -1) setProgress(visual_element.getProgress());

              if (previous_selection != null) previous_selection.unselected();
              visual_element.selected();
              previous_selection = visual_element;

              setEnableMenuItemOptions(TargetIPv4.class.isInstance(visual_element) || TargetIPv6.class.isInstance(visual_element));

              if (ActionFlood.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_flood.setEnabled(true);
              } else if (ActionPing.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_ping.setEnabled(true);
              } else if (ActionSNMP.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_explore.setEnabled(true);
              } else if (ActionHTTP.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ActionNmap.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ReachableView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_ping.setEnabled(true);
              } else if (FloodView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_flood.setEnabled(true);
              } else if (HTTPView.class.isInstance(visual_element) || HTTPPagesView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
              } else if (BytesReceivedView.class.isInstance(visual_element) || BytesSentView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_explore.setEnabled(true);
              } else {
                // visual_element is neither a view nor an action
                setEnableMenuAndTool(true);

                if (visual_element.getChildren().size() == 0) {
                  // no children
                  menu_item_remove_action.setEnabled(false);
                  item_remove_action.setEnabled(false);
                  menu_item_remove_action_ping.setEnabled(false);
                  menu_item_remove_action_flood.setEnabled(false);
                  menu_item_remove_action_explore.setEnabled(false);
                  menu_item_remove_view.setEnabled(false);
                  menu_item_remove_view_ping.setEnabled(false);
                  menu_item_remove_view_flood.setEnabled(false);
                  menu_item_remove_view_explore.setEnabled(false);
                }
              }

              if (net.fenyo.gnetwatch.actions.Action.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (Target.class.isInstance(visual_element)) {
                try {
                  boolean foo;

                  final TargetGroup target_group = new TargetGroup("", "");
                  target_group.initialize(gui);
                  foo = target_group.canAddTarget(visual_element) && visual_element.canManageThisChild(target_group);
                  setEnableGroupTargetGroup(foo);

                  // No name service is checked for the validity of the address.
                  final Inet4Address bar = (Inet4Address) Inet4Address.getByAddress("bar", new byte [] { 0, 0, 0, 0 });

                  final TargetIPv4 target_ipv4 = new TargetIPv4("", bar, null);
                  target_ipv4.initialize(gui);
                  foo = target_ipv4.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4);
                  setEnableGroupTargetHost(foo);

                  final TargetIPv4Range target_ipv4_range = new TargetIPv4Range("", bar, bar);
                  target_ipv4_range.initialize(gui);
                  foo = target_ipv4_range.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4_range);
                  setEnableGroupTargetRange(foo);

                  final TargetIPv4Subnet target_ipv4_subnet = new TargetIPv4Subnet("", bar, bar);
                  target_ipv4_subnet.initialize(gui);
                  foo = target_ipv4_subnet.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4_subnet);
                  setEnableGroupTargetSubnet(foo);
                } catch (final AlgorithmException ex) {
                  log.error("Exception", ex);
                } catch (final UnknownHostException ex) {
                log.error("Exception", ex);
                }
              } else if (net.fenyo.gnetwatch.activities.Queue.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (DataView.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (visual_element.equals(visual_transient)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              }
            }
          }
        }

      }
    });
    tree.setHeaderVisible(true);
    tree_column1 = new TreeColumn(tree, SWT.LEFT);
    tree_column1.setText(getConfig().getString("items"));
    tree_column1.setWidth(150);
    tree_column2 = new TreeColumn(tree, SWT.LEFT);
    tree_column2.setText("Type");
    tree_column2.setWidth(80);
    tree_column3 = new TreeColumn(tree, SWT.RIGHT);
    tree_column3.setText("Description");
    tree_column3.setWidth(100);

    // Tree images
    image_folder = new Image(display, "pictures/folder_violet.png");
    image_oscillo = new Image(display, "pictures/oscilloscope-af.png");
    image_multirow = new Image(display, "pictures/multirow.png");
    image_exec = new Image(display, "pictures/exec.png");
    image_watch = new Image(display, "pictures/yahoo_idle-af.png");
    image_host = new Image(display, "pictures/network_local.png");
    image_host6 = new Image(display, "pictures/network_local-6-af.png");
    image_interface = new Image(display, "pictures/memory.png");
    image_queue = new Image(display, "pictures/jabber_group.png");
    image_network = new Image(display, "pictures/network.png");
    image_host_snmp = new Image(display, "pictures/network_local_snmp.png");
    image_host6_snmp = new Image(display, "pictures/network_local-6-af-snmp.png");

    visual_root = new VisualElement();
    visual_root.setParent(this, tree);
    visual_root.setItem(config.getString("collected_data"));

    visual_queues = new VisualElement();
    visual_queues.setParent(this, tree);
    visual_queues.setItem(config.getString("queues"));

    } // release sync_tree

    // create initial objects only if they do not exist in the database
    synchronized (synchro) {
      synchronized (sync_tree) {
          final Session session = synchro.getSessionFactory().getCurrentSession();
          try {
            session.beginTransaction();
          } catch (final org.hibernate.exception.JDBCConnectionException ex) {
            // c'est ici que ï¿½a se termine quand on ouvre deux fois sur la mï¿½me database maintenue par un fichier par HSQLDB
            final MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            // ï¿½ traduire
            dialog.setText("GNetWatch Fatal Error");
            dialog.setMessage(ex.toString() + " - caused by: " + ex.getCause().toString());
            dialog.open();
            session.getTransaction().rollback();
            System.exit(0);
          }

          try {

          final java.util.List results =
              session.createQuery("from VisualElement as el where el.item = ?")
              .setString(0, config.getString("targets")).list();
          if (results.size() > 1) log.error("database contains multiple root targets");
          
          if (results.size() > 0) {
            visual_transient = (VisualElement) results.get(0);
            visual_transient.initialize(this);
            visual_transient.duplicateTreeItemOnce(visual_root.getTreeItems());
            visual_transient.addParent(visual_root);

            // recursive initialization of every visual element stored in the database
            initFromSynchro(visual_transient);

          } else {

            visual_transient = new VisualElement();
            visual_transient.setParent(this, visual_root);
            visual_transient.setItem(config.getString("targets"));
            session.save(visual_transient);
          }

          for (final VisualElement foo : visual_transient.getChildren()) {
            if (foo.getItem().equals(config.getString("local_host")))
              visual_thishost = (TargetGroup) foo;
            if (foo.getItem().equals(config.getString("every_host")))
              visual_transient_all = (TargetGroup) foo;
            if (foo.getItem().equals(config.getString("every_network")))
              visual_transient_networks = (TargetGroup) foo;
            if (foo.getItem().equals(config.getString("user_defined")))
              user_defined = (TargetGroup) foo;
          }

          if (visual_thishost == null) {
            visual_thishost = new TargetGroup("added by GUI", config.getString("local_host"));
            visual_thishost.setParent(this, visual_transient);
            session.save(visual_thishost);
          }

          if (visual_transient_all == null) {
            visual_transient_all = new TargetGroup("added by GUI", config.getString("every_host"));
            visual_transient_all.setParent(this, visual_transient);
            session.save(visual_transient_all);
          }

          if (getConfig().getProperty("enableeverynetworks") != null &&
              getConfig().getProperty("enableeverynetworks").equals("true")) {

            if (visual_transient_networks == null) {
              visual_transient_networks = new TargetGroup("added by GUI", config.getString("every_network"));
              visual_transient_networks.setParent(this, visual_transient);
              session.save(visual_transient_networks);
            }

          }

          if (user_defined == null) {
            user_defined = new TargetGroup("added by GUI", config.getString("user_defined"));
            user_defined.setParent(this, visual_transient);
            session.save(user_defined);
          }

          // create localhost
          for (final Enumeration nifs = NetworkInterface.getNetworkInterfaces(); nifs.hasMoreElements(); ) {
            final NetworkInterface nif = (NetworkInterface) nifs.nextElement();
            for (final Enumeration addrs = nif.getInetAddresses(); addrs.hasMoreElements(); ) {
              final InetAddress addr = (InetAddress) addrs.nextElement();

              // localhost has IPv4 addresses
              if (Inet4Address.class.isInstance(addr)) {
                final TargetIPv4 foo = new TargetIPv4("transient localhost", (Inet4Address) addr, snmp_manager);
                if (foo == getCanonicalInstance(foo)) {
                  session.save(foo);
                  if (foo.addTarget(this, visual_thishost) == true) foo.checkSNMPAwareness();
                }
              }
     
              // localhost has IPv6 addresses
              if (Inet6Address.class.isInstance(addr)) {
                final TargetIPv6 foo = new TargetIPv6("transient localhost", (Inet6Address) addr, snmp_manager);
                if (foo == getCanonicalInstance(foo)) {
                  session.save(foo);
                  if (foo.addTarget(this, visual_thishost) == true) foo.checkSNMPAwareness();
                }
              }
            }

            // recursively initialize SNMPQuerier instances
            initSNMPQueriers(visual_transient);

            // recursively wakeup actions
            wakeupActions(visual_transient);
          }

          session.getTransaction().commit();
        } catch (final Exception ex) {
          log.error("Exception", ex);
        }
      }
    }

    synchronized (sync_tree) {

      // CTabFolder
      tab_folder = new CTabFolder(horizontal_sash, SWT.BORDER);
      tab_folder.setSimple(false);

      tab_item1 = new CTabItem(tab_folder, SWT.None);
      tab_item1.setText(getConfig().getString("about_the_author"));
      final Browser browser = new Browser(tab_folder, SWT.BORDER | SWT.MULTI);
      browser.setUrl("http://www.fenyo.net");
//   browser.setText("<HTML><BODY BGCOLOR='red'><H1>Data</H1>target: <B>127.0.0.1</B></BODY></HTML>");
      tab_item1.setControl(browser);

      tab_item2 = new CTabItem(tab_folder, SWT.None);
      tab_item2.setText("Documentation");
      final Browser browser2 = new Browser(tab_folder, SWT.BORDER | SWT.MULTI);
      browser2.setUrl("http://gnetwatch.sourceforge.net/docs");
      tab_item2.setControl(browser2);

      // StyledText
      status = new StyledText(shell, SWT.BORDER);
      status_grid_data = new GridData(GridData.FILL_HORIZONTAL);
      status.setLayoutData(status_grid_data);
      status.setEditable(false);
      status.setEnabled(false);
      status.setText("Loading...");
      status.setBackground(shell.getBackground());

      // ProgressBar
      progress_bar = new ProgressBar(shell, SWT.SMOOTH);
      progress_bar.setBounds(10, 10, 200, 32);
      progress_bar.setSelection(0);
      progress_bar_grid_data = new GridData(GridData.FILL_HORIZONTAL);
      progress_bar.setLayoutData(progress_bar_grid_data);

      // instanciate queues
      
      final String [] queue_names = background.getQueues().keySet().toArray(new String [] {});
      Arrays.sort(queue_names);
      for (final String queue_name : queue_names)
        background.getQueues().get(queue_name).setParent(this, visual_queues);

      // set initial selections
      menu_item_credentials.setEnabled(false);
      menu_item_ip_options.setEnabled(false);
      menu_item_http_options.setEnabled(false);
      setEnableGroupTargetSubnet(false);
      setEnableGroupTargetRange(false);
      setEnableGroupTargetHost(false);
      setEnableGroupTargetGroup(false);

    } // release sync_tree

    tree.setBackground(getBackgroundColor());

    // display documentation at startup
    tab_folder.setSelection(tab_item2);

    // final operations
    shell.pack();
    shell.open();

    visual_root.expandTreeItems(true);
    visual_queues.expandTreeItems(true);
    visual_transient.expandTreeItems(true);
    tree.setSelection(visual_root.getTreeItems().get(0));
    tree.setFocus();

    synchronized (GUI_created) {
      GUI_created[0] = true;
      GUI_created.notifyAll();
    }

    // list network interfaces at startup
    text_console_do_not_go_on_top = 1;
    appendNetworkInterfaces();

    awtGUI = new AwtGUI(config);
    awtGUI.createAwtGUI();
}

  // Queue thread
  public void informTargetHasNewEventClass(final Target target, final Class clazz) {
    asyncExec(new Runnable() {
      public void run() {
        try {
          views.refreshDataViews(target, clazz);
        } catch (final Exception ex) {
          log.error("Exception", ex);
        }
      }
    });
  }

  /**
   * Wait for the creation of the GUI objects.
   * Can be called from any thread.
   * main thread.
   * @param none.
   * @return void.
   */
  public void waitForCreation() {
    synchronized (GUI_created) {
      while (GUI_created[0] == false) {
        try {
          GUI_created.wait();
        }
        catch (final InterruptedException e) {
          log.debug("waitForCreation(): " + e);
        }
      }
    }
  }

  /**
   * GUI thread.
   * @param none.
   * @return void.
   */
  public void run() {
    try {
      createGUI();
    } catch (final Exception ex) {
      log.error("Exception", ex);
    }

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }

    try {
      display.dispose();
    } catch (final SWTException ex) {
      log.warn("Exception", ex);
    }
  }

  /**
   * Constructor.
   * main thread.
   * Builds a user interface.
   * main thread.
   * @param none.
   */
  public GUI(final Config config, final Background background, final Main main, final SNMPManager snmp_manager, final Synchro synchro) {
    this.config = config;
    this.synchro = synchro;
    this.background = background;
    this.main = main;
    this.snmp_manager = snmp_manager;
    this.views = new Views(this);
    thread = new Thread(this, "GUI");

    // ((DebugQueue) background.getQueues().get("debug")).setGUI(this);

    thread.start();
  }

  // main thread
  public void end() throws InterruptedException {
    awtGUI.end();
  }

  /**
   * Waits for the main thread to terminate.
   * main thread.
   * @param none.
   * @return void.
   */
  public void join() {
    try {
      thread.join();
    }
    catch (final InterruptedException e) {
      log.debug("GUI thread already terminated.");
    }
  }

  // lock survey : sync_tree << synchro << HERE
  public void updateEnableState() {
    final GUI gui = this;
    asyncExec(new Runnable() {
      public void run() {
        if (tree.getSelectionCount() == 1) {
          TreeItem current_item = tree.getSelection()[0];
          if (current_item != null) {
            synchronized (sync_tree) {
              final VisualElement visual_element = (VisualElement) current_item.getData(VisualElement.class.toString());

              if (visual_element.getProgress() != -1) setProgress(visual_element.getProgress());

              if (previous_selection != null) previous_selection.unselected();
              visual_element.selected();
              previous_selection = visual_element;

              setEnableMenuItemOptions(TargetIPv4.class.isInstance(visual_element) || TargetIPv6.class.isInstance(visual_element));

              menu_item_add_process.setEnabled(visual_element.equals(visual_root) || visual_element.equals(visual_transient));
              menu_item_add_source.setEnabled(visual_element.equals(visual_root) || visual_element.equals(visual_transient));

              if (ActionFlood.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_flood.setEnabled(true);
              } else if (ActionPing.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_ping.setEnabled(true);
              } else if (ActionGenericProcess.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ActionGenericSrc.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ActionSNMP.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
                menu_item_remove_action_explore.setEnabled(true);
              } else if (ActionHTTP.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ActionNmap.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_action.setEnabled(true);
                item_remove_action.setEnabled(true);
              } else if (ReachableView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_ping.setEnabled(true);
              } else if (GenericProcessView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
              } else if (GenericSrcView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
              } else if (FloodView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_flood.setEnabled(true);
              } else if (HTTPView.class.isInstance(visual_element) || HTTPPagesView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
              } else if (BytesReceivedView.class.isInstance(visual_element) || BytesSentView.class.isInstance(visual_element)) {
                setEnableMenuAndTool(false);
                menu_item_remove_target.setEnabled(true);
                item_remove_target.setEnabled(true);
                menu_item_remove_view.setEnabled(true);
                menu_item_remove_view_explore.setEnabled(true);
              } else {
                // visual_element is neither a view nor an action
                setEnableMenuAndTool(true);

                menu_item_generic_options.setEnabled(!(visual_element.equals(visual_root) || visual_element.equals(visual_transient)));
                if (visual_element.getParents().contains(visual_transient))
                  menu_item_generic_options.setEnabled(false);

                if (!visual_element.equals(visual_root) && !visual_element.equals(visual_transient)) {
                  menu_item_add_process.setEnabled(TargetGroup.class.isInstance(visual_element));
                  menu_item_add_source.setEnabled(TargetGroup.class.isInstance(visual_element));
                }

                if (visual_element.getChildren().size() == 0) {
                  // no children
                  menu_item_remove_action.setEnabled(false);
                  item_remove_action.setEnabled(false);
                  menu_item_remove_action_ping.setEnabled(false);
                  menu_item_remove_action_flood.setEnabled(false);
                  menu_item_remove_action_explore.setEnabled(false);
                  menu_item_remove_view.setEnabled(false);
                  menu_item_remove_view_ping.setEnabled(false);
                  menu_item_remove_view_flood.setEnabled(false);
                  menu_item_remove_view_explore.setEnabled(false);
                }
              }

              if (net.fenyo.gnetwatch.actions.Action.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (Target.class.isInstance(visual_element)) {
                try {
                  boolean foo;

                  final TargetGroup target_group = new TargetGroup("", "");
                  target_group.initialize(gui);
                  foo = target_group.canAddTarget(visual_element) && visual_element.canManageThisChild(target_group);
                  setEnableGroupTargetGroup(foo);

                  // No name service is checked for the validity of the address.
                  final Inet4Address bar = (Inet4Address) Inet4Address.getByAddress("bar", new byte [] { 0, 0, 0, 0 });

                  final TargetIPv4 target_ipv4 = new TargetIPv4("", bar, null);
                  target_ipv4.initialize(gui);
                  foo = target_ipv4.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4);
                  setEnableGroupTargetHost(foo);

                  final TargetIPv4Range target_ipv4_range = new TargetIPv4Range("", bar, bar);
                  target_ipv4_range.initialize(gui);
                  foo = target_ipv4_range.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4_range);
                  setEnableGroupTargetRange(foo);

                  final TargetIPv4Subnet target_ipv4_subnet = new TargetIPv4Subnet("", bar, bar);
                  target_ipv4_subnet.initialize(gui);
                  foo = target_ipv4_subnet.canAddTarget(visual_element) && visual_element.canManageThisChild(target_ipv4_subnet);
                  setEnableGroupTargetSubnet(foo);
                } catch (final AlgorithmException ex) {
                  log.error("Exception", ex);
                } catch (final UnknownHostException ex) {
                log.error("Exception", ex);
                }
              } else if (net.fenyo.gnetwatch.activities.Queue.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (DataView.class.isInstance(visual_element)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else if (visual_element.equals(visual_transient)) {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              } else {
                setEnableGroupTargetSubnet(false);
                setEnableGroupTargetRange(false);
                setEnableGroupTargetHost(false);
                setEnableGroupTargetGroup(false);
              }
            }
          }
        }
      }
    }
    );
  }


  /**
   * Enable/disable "add subnet" in the GUI.
   * @param enable enable/disable switch.
   * @return void.
   */
  private void setEnableGroupTargetSubnet(final boolean enable) {
    label1.setEnabled(enable);
    label2.setEnabled(enable);
    group_target_subnet.setEnabled(enable);
    target_subnet_address.setEnabled(enable);
    target_subnet_mask.setEnabled(enable);
    target_subnet_push.setEnabled(enable);
    menu_item_add_network.setEnabled(enable);
    item_add_network.setEnabled(enable);
  }

  /**
   * Enable/disable "add range" in the GUI.
   * @param enable enable/disable switch.
   * @return void.
   */
  private void setEnableGroupTargetRange(final boolean enable) {
    label3.setEnabled(enable);
    label4.setEnabled(enable);
    group_target_range.setEnabled(enable);
    target_range_begin.setEnabled(enable);
    target_range_end.setEnabled(enable);
    target_range_push.setEnabled(enable);
    menu_item_add_range.setEnabled(enable);
  }

  /**
   * Enable/disable "add host" in the GUI.
   * @param enable enable/disable switch.
   * @return void.
   */
  private void setEnableGroupTargetHost(final boolean enable) {
    label5.setEnabled(enable);
    label7.setEnabled(enable);
    group_target_host.setEnabled(enable);
    group_target_host6.setEnabled(enable);
    target_host_value.setEnabled(enable);
    target_host6_value.setEnabled(enable);
    target_host_push.setEnabled(enable);
    target_host6_push.setEnabled(enable);
    menu_item_add_host.setEnabled(enable);
    menu_item_add_host6.setEnabled(enable);
    item_add_host.setEnabled(enable);
  }

  private void setEnableMenuItemOptions(final boolean enable) {
    menu_item_credentials.setEnabled(enable);
    menu_item_ip_options.setEnabled(enable);
    menu_item_http_options.setEnabled(enable);
  }

  private void setEnableMenuAndTool(final boolean enable) {
    menu_item_generic_options.setEnabled(enable);
    menu_item_remove_target.setEnabled(enable);
    item_remove_target.setEnabled(enable);
    menu_item_add_ping.setEnabled(enable);
    menu_item_add_process.setEnabled(enable);
    menu_item_add_source.setEnabled(enable);
    item_add_ping.setEnabled(enable);
    menu_item_add_flood.setEnabled(enable);
    menu_item_explore_snmp.setEnabled(enable);
    menu_item_add_http.setEnabled(enable);
    menu_item_explore_nmap.setEnabled(enable);
    menu_item_remove_action.setEnabled(enable);
    item_remove_action.setEnabled(enable);
    menu_item_remove_action_ping.setEnabled(enable);
    menu_item_remove_action_flood.setEnabled(enable);
    menu_item_remove_action_explore.setEnabled(enable);
    menu_item_remove_view.setEnabled(enable);
    menu_item_remove_view_ping.setEnabled(enable);
    menu_item_remove_view_flood.setEnabled(enable);
    menu_item_remove_view_explore.setEnabled(enable);
  }

  /**
   * Enable/disable "add group" in the GUI.
   * @param enable enable/disable switch.
   * @return void.
   */
  private void setEnableGroupTargetGroup(final boolean enable) {
    label6.setEnabled(enable);
    group_target_group.setEnabled(enable);
    target_group_value.setEnabled(enable);
    target_group_push.setEnabled(enable);
    menu_item_add_group.setEnabled(enable);
  }

  private void initFromSynchro(final VisualElement elt) {
    for (final VisualElement child : elt.getChildren()) {
      child.initialize(this);
      child.duplicateTreeItemOnce(elt.getTreeItems());
      child.addParent(elt);

      if (Target.class.isInstance(child)) {
        // register child as a new canonical instance
        if (child != getCanonicalInstance((Target) child))
          log.error("target already registered, database will not match in-memory objects");
      }
    }

    for (final VisualElement child : elt.getChildren())
      initFromSynchro(child);
  }

  private void wakeupActions(final VisualElement elt) {
    for (final VisualElement child : elt.getChildren()) wakeupActions(child);
    if (net.fenyo.gnetwatch.actions.Action.class.isInstance(elt)) {
      final net.fenyo.gnetwatch.actions.Action action = (net.fenyo.gnetwatch.actions.Action) elt;
      if (action.getBackground() == null) {
        action.setBackground(background);
        try {
          background.addActionQueue(action);
        } catch (GeneralException ex) {
          log.warn("can not restart action:" + action + " - target: " + action.getTarget());
        }
      }
    }
  }

  private void initSNMPQueriers(final VisualElement elt) {
    for (final VisualElement child : elt.getChildren()) initSNMPQueriers(child);
    if (TargetIPv4.class.isInstance(elt)) {
      final TargetIPv4 target = (TargetIPv4) elt;
      target.getSNMPQuerier().setSNMPManager(snmp_manager);
      target.getSNMPQuerier().parametersHaveChanged();
    }

    if (TargetIPv6.class.isInstance(elt)) {
      final TargetIPv6 target = (TargetIPv6) elt;
      target.getSNMPQuerier().setSNMPManager(snmp_manager);
      target.getSNMPQuerier().parametersHaveChanged();
    }
  }
         
/**
   * Parses a configuration file to create initial targets.
   * @param filename configuration file.
   * @return void.
   */
  public void createFromXML(final String filename) {
    final GUI gui = this;

    asyncExec(new Runnable() {
      public void run() {
        synchronized (synchro) {
          final Session session = synchro.getSessionFactory().getCurrentSession();
          session.beginTransaction();

          try {
            final XMLConfiguration initial = new XMLConfiguration(filename);
            initial.setExpressionEngine(new XPathExpressionEngine());

            // limitation de l'implï¿½mentation : on n'autorise que les parents de type groupe

            for (final HierarchicalConfiguration subconf : (java.util.List<HierarchicalConfiguration>)
                initial.configurationsAt("/objects/target")) {
              if (subconf.getProperty("@targetType").equals("group")) {
                final String name = subconf.getString("name");

                final java.util.List<String> parents = (java.util.List<String>) subconf.getList("parent[@parentType='group']");
                if (parents.size() == 0) {
                  final TargetGroup target_group = new TargetGroup("added by initial configuration", name);
                  target_group.addTarget(gui, user_defined);
                } else for (final String parent : parents) {
                    final TargetGroup foo = new TargetGroup("temporary", parent);
                    final TargetGroup target_parent = (TargetGroup) getCanonicalInstance(foo);
                    if (target_parent == foo) log.error("Initial configuration: parent does not exist");
                    else {
                      final TargetGroup target_group = new TargetGroup("added by initial configuration", name);
                      target_group.addTarget(gui, target_parent);
                    }
                  }
              }

              if (subconf.getProperty("@targetType").equals("ipv4")) {
                final String address = subconf.getString("address");

                java.util.List<String> parents = (java.util.List<String>) subconf.getList("parent[@parentType='group']");
                if (parents.size() != 0) for (final String parent : parents) {
                  final TargetGroup foo = new TargetGroup("temporary", parent);
                  final TargetGroup target_parent = (TargetGroup) getCanonicalInstance(foo);
                  if (target_parent == foo) log.error("Initial configuration: parent does not exist");
                  else {
                    final TargetIPv4 target = new TargetIPv4("added by initial configuration", GenericTools.stringToInet4Address(address), snmp_manager);
                    target.addTarget(gui, target_parent);
                    if (subconf.getString("snmp/version") != null) {
                      if (subconf.getString("snmp/version").equals("v1")) target.getSNMPQuerier().setVersion(0);
                      if (subconf.getString("snmp/version").equals("v2c")) target.getSNMPQuerier().setVersion(1);
                      if (subconf.getString("snmp/version").equals("v3")) target.getSNMPQuerier().setVersion(2);
                    }
                    if (subconf.getString("snmp/community") != null)
                      target.getSNMPQuerier().setCommunity(subconf.getString("snmp/community"));

                    // Setting the agent is not possible when creating a target through the GUI
                    if (subconf.getString("snmp/agent") != null)
                      target.getSNMPQuerier().setAddress(GenericTools.stringToInet4Address(subconf.getString("snmp/agent")));

                    if (subconf.getString("snmp/password-auth") != null)
                      target.getSNMPQuerier().setPasswordAuth(subconf.getString("snmp/password-auth"));
                    if (subconf.getString("snmp/password-priv") != null)
                      target.getSNMPQuerier().setPasswordPriv(subconf.getString("snmp/password-priv"));
                    if (subconf.getString("snmp/pdu-max-size") != null)
                      target.getSNMPQuerier().setPDUMaxSize(subconf.getInt("snmp/pdu-max-size"));
                    if (subconf.getString("snmp/port") != null)
                      target.getSNMPQuerier().setPort(subconf.getInt("snmp/port"));
                    if (subconf.getString("snmp/retries") != null)
                      target.getSNMPQuerier().setRetries(subconf.getInt("snmp/retries"));                  
                    if (subconf.getString("snmp/security") != null && subconf.getString("snmp/security").equals("NOAUTH_NOPRIV"))
                      target.getSNMPQuerier().setSec(SecurityLevel.NOAUTH_NOPRIV);
                    if (subconf.getString("snmp/security") != null && subconf.getString("snmp/security").equals("AUTH_NOPRIV"))
                      target.getSNMPQuerier().setSec(SecurityLevel.AUTH_NOPRIV);
                    if (subconf.getString("snmp/security") != null && subconf.getString("snmp/security").equals("AUTH_PRIV"))
                      target.getSNMPQuerier().setSec(SecurityLevel.AUTH_PRIV);                  
                    if (subconf.getString("snmp/timeout") != null)
                      target.getSNMPQuerier().setTimeout(subconf.getInt("snmp/timeout"));
                    target.getSNMPQuerier().update();
                  }
                }
              }

              if (subconf.getProperty("@targetType").equals("ipv6")) {
                final String address = subconf.getString("address");

                java.util.List<String> parents = (java.util.List<String>) subconf.getList("parent[@parentType='group']");
                if (parents.size() != 0) for (final String parent : parents) {
                  final TargetGroup foo = new TargetGroup("temporary", parent);
                  final TargetGroup target_parent = (TargetGroup) getCanonicalInstance(foo);
                  if (target_parent == foo) log.error("Initial configuration: parent does not exist");
                  else {
                    final TargetIPv6 target = new TargetIPv6("added by initial configuration", GenericTools.stringToInet6Address(address), snmp_manager);
                    target.addTarget(gui, target_parent);
                  }
                }
              }

              if (subconf.getProperty("@targetType").equals("ipv4range")) {
                final String begin = subconf.getString("begin");
                final String end = subconf.getString("end");

                java.util.List<String> parents = (java.util.List<String>) subconf.getList("parent[@parentType='group']");
                if (parents.size() != 0) for (final String parent : parents) {
                  final TargetGroup foo = new TargetGroup("temporary", parent);
                  final TargetGroup target_parent = (TargetGroup) getCanonicalInstance(foo);
                  if (target_parent == foo) log.error("Initial configuration: parent does not exist");
                  else {
                    final TargetIPv4Range target = new TargetIPv4Range("added by initial configuration", GenericTools.stringToInet4Address(begin), GenericTools.stringToInet4Address(end));
                    target.addTarget(gui, target_parent);
                  }
                }
              }

              if (subconf.getProperty("@targetType").equals("ipv4subnet")) {
                final String network = subconf.getString("network");
                final String netmask = subconf.getString("netmask");

                java.util.List<String> parents = (java.util.List<String>) subconf.getList("parent[@parentType='group']");
                if (parents.size() != 0) for (final String parent : parents) {
                  final TargetGroup foo = new TargetGroup("temporary", parent);
                  final TargetGroup target_parent = (TargetGroup) getCanonicalInstance(foo);
                  if (target_parent == foo) log.error("Initial configuration: parent does not exist");
                  else {
                    final TargetIPv4Subnet target = new TargetIPv4Subnet("added by initial configuration", GenericTools.stringToInet4Address(network), GenericTools.stringToInet4Address(netmask));
                    target.addTarget(gui, target_parent);
                  }
                }
              }
            }

            session.getTransaction().commit();
          }
          catch(final ConfigurationException ex) {
            log.warn("Exception", ex);
            session.getTransaction().rollback();
          }
          catch(final AlgorithmException ex) {
            log.error("Exception", ex);
            session.getTransaction().rollback();
          }
          catch(final UnknownHostException ex) {
            log.error("Exception", ex);
            session.getTransaction().rollback();
          }
        }
      }
    });
  }

  /**
   * Moves the application to the top of the drawing order.
   * @param none.
   * @return void.
   */
  public void showGUI() {
    asyncExecIfNeeded(new Runnable() {
      public void run() {
        shell.setActive();
      }
    });
  }
}