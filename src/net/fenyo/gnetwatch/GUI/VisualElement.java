
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

package net.fenyo.gnetwatch.GUI;

import net.fenyo.gnetwatch.*;
import net.fenyo.gnetwatch.activities.*;
import net.fenyo.gnetwatch.targets.*;
import net.fenyo.gnetwatch.data.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

/**
 * Class derivated from VisualElement can be displayed in the main tree of the GNetWatch GUI.
 * @author Alexandre Fenyo
 * @version $Id: VisualElement.java,v 1.63 2008/08/03 20:32:58 fenyo Exp $
 */

// pour la synchro : ne pas s'appuyer sur le fait qu'on fait qqchose dans thread GUI car il y a pas
// forcément un thread GUI

public class VisualElement {
  private static Log log = LogFactory.getLog(VisualElement.class);

  // persistent
  private Long id;

  // Since many classes may run in a VM without a GUI (GNetWatch server mode),
  // we do not instanciate at startup the private fields to save memory & CPU.

  // initialized protects against two initializations at the same time
  private Boolean initialized = false;
  private GUI gui = null;

  private Boolean disposed;

  private java.util.List<TreeItem> treeItems = new LinkedList<TreeItem>();
  private java.util.List<VisualElement> parents = new LinkedList<VisualElement>();

  // persistent
  private java.util.List<VisualElement> children = new LinkedList<VisualElement>();
  // persistent
  private String item = null;

  // persistent
  private String type = null;

  // persistent
  private String description = null;

  private Image image = null;
  private boolean is_selected = false;
  private int progress = 0;

  /**
   * Constructor.
   * @param none.
   */
  // GUI & main thread
  public VisualElement() {}

  public Long getId() {
    return id;
  }

  protected void setId(final Long id) {
    this.id = id;
  }

  public void setChildren(final java.util.List<VisualElement> children) {
    this.children = children;
  }

  /**
   * Initializes this element.
   * @param gui current GUI instance.
   * @return void.
   */
  // We do not make this job at construction time since an object that is derived
  // from a VisualElement may never be seen (background mode without GUI). 
  // main, Queue & GUI threads
  // sync path: initialize() << initialized
  protected void initialize(final GUI gui) {
    synchronized (initialized) {
      if (initialized == false) {
        this.gui = gui;
        initialized = true;
        disposed = false;
        if (getItem() == null) setItem("uninitialized");
        if (getType() == null) setType("");
        if (getDescription() == null) setDescription("");
      }
      // needed when the text fields are set before a GUI is defined
      else if (this.gui == null) this.gui = gui;
    }
  }

  /**
   * Expands or merge the associated tree items.
   * @param doit true to expand.
   * @return void.
   */
  public void expandTreeItems(final boolean doit) {
    for (final TreeItem item : treeItems) item.setExpanded(doit);
  }

  /**
   * Checks that this element is disposed.
   * @param none.
   * @return boolean true if this element is disposed.
   */
  public boolean isDisposed() {
    return disposed;
  }

  /**
   * Returns the current GUI instance.
   * @param none.
   * @return GUI current GUI instance.
   */
  // Queue & GUI threads
  // could be any thread
  final protected GUI getGUI() {
    return gui;
  }

  final public void setGUI(final GUI gui) {
    this.gui = gui;
  }

  /**
   * Sets the "exec" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageExec() {
    setImage(getGUI().getImageExec());
  }

  /**
   * Sets the "folder" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageFolder() {
    setImage(getGUI().getImageFolder());
  }

  /**
   * Sets the "oscilloscope" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageOscillo() {
    setImage(getGUI().getImageOscillo());
  }

  /**
   * Sets the "multirow" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageMultiRow() {
    setImage(getGUI().getImageMultiRow());
  }

  /**
   * Sets the "watch" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageWatch() {
    setImage(getGUI().getImageWatch());
  }

  /**
   * Sets the "IPv4 host" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageHost() {
    setImage(getGUI().getImageHost()); // bcl sans fil qui arrive parfois au lancement (4)
  }

  /**
   * Sets the "IPv6 host" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageHost6() {
    setImage(getGUI().getImageHost6());
  }

  /**
   * Sets the "inteface" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageInterface() {
    setImage(getGUI().getImageInterface());
  }

  /**
   * Sets the "queue" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageQueue() {
    setImage(getGUI().getImageQueue());
  }

  /**
   * Sets the "network" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageNetwork() {
    setImage(getGUI().getImageNetwork());
  }

  /**
   * Sets the "snmp ipv4 host" icon to this element.
   * @param none.
   * @return void.
   */
  // lock survey : synchro << sync_tree << HERE
  //               << HERE
  public void setImageHostSNMP() {
    setImage(getGUI().getImageHostSNMP());
  }

  /**
   * Sets the "snmp ipv6 host" icon to this element.
   * @param none.
   * @return void.
   */
  public void setImageHost6SNMP() {
    setImage(getGUI().getImageHost6SNMP());
  }

  /**
   * Returns the list of tree items that represent this element.
   * @param none.
   * @return java.util.List<TreeItem> list of tree items.
   */
  // GUI thread
  // remettre protected apres debug
  public java.util.List<TreeItem> getTreeItems() {
    return treeItems;
  }

  /**
   * Checks that a tree item represents this element.
   * @param item tree item.
   * @return boolean true if this tree item represents this element.
   */
  protected boolean isThisOurTreeItem(final TreeItem item) {
    return treeItems.contains(item);
  }

  /**
   * Adds a tree item to represent this element.
   * @param treeItem tree item to add.
   * @return void.
   */
  // GUI thread
  private void addTreeItem(final TreeItem treeItem) {
    treeItem.setData(VisualElement.class.toString(), this);
    if (image != null) treeItem.setImage(image);
    else treeItem.setImage(gui.getImageFolder());
    treeItems.add(treeItem);
    treeItem.setText(new String [] { item, type, description });
  }

  /**
   * Recursively duplicates this element and its descendants under each of the destination tree items.
   * @param destination_tree_items destination tree items.
   * @return void.
   */
  // lock survey : synchro << sync_tree << HERE
  private void duplicateTreeItem(final java.util.List<TreeItem> destination_tree_items) {
    final java.util.List<TreeItem> new_tree_items = new LinkedList<TreeItem>();

    for (final TreeItem destination_tree_item : destination_tree_items) {
      final TreeItem my_tree_item_copy = new TreeItem(destination_tree_item, SWT.NONE);
      new_tree_items.add(my_tree_item_copy);
      addTreeItem(my_tree_item_copy);
    }

    for (final VisualElement child : children) child.duplicateTreeItem(new_tree_items);
  }

  /**
   * Duplicates this element under each of the destination tree items.
   * @param destination_tree_items destination tree items.
   * @return void.
   */
  public void duplicateTreeItemOnce(final java.util.List<TreeItem> destination_tree_items) {
    for (final TreeItem destination_tree_item : destination_tree_items) {
      boolean alreadydone = false;

      for (final TreeItem foo : destination_tree_item.getItems())
        if (foo.getData(VisualElement.class.toString()) == this) alreadydone = true;

      if (alreadydone == false)
        addTreeItem(new TreeItem(destination_tree_item, SWT.NONE));
    }
  }

  /**
   * Attaches this element to the root of a tree.
   * @param gui current GUI instance.
   * @param parent root of the destination tree.
   * @return void.
   */
  // GUI thread
  protected void setParent(final GUI gui, final Tree parent) {
    initialize(gui);
    addTreeItem(new TreeItem(parent, SWT.NONE));
  }

  /**
   * Attaches this item under another element.
   * @param gui current GUI instance.
   * @param parent parent element.
   * @return void.
   */
  // GUI thread
  // lock survey : synchro << sync_tree << HERE
  public void setParent(final GUI gui, final VisualElement parent) {
    initialize(gui);

    duplicateTreeItem(parent.getTreeItems());

    parents.add(parent);
    parent.addChild(this);

    if (gui != null) gui.updateEnableState();
  }

  /**
   * Recursively detaches an item from this element and its descendants.
   * @param item item to detach.
   * @return void.
   */
  private void disposeSubItems(final TreeItem item) {
    for (final VisualElement child : children)
      for (final TreeItem child_item : new LinkedList<TreeItem>(child.getTreeItems()))
        if (item.equals(child_item.getParentItem()))
          child.disposeSubItems(child_item);
    item.dispose();
    treeItems.remove(item);
  }

  /**
   * Detaches this element from one of its parents.
   * @param parent parent.
   * @return void.
   */
  // lock survey : sync_tree << synchro << HERE
  private void unsetParent(final VisualElement parent) {
    final org.hibernate.Session session = getGUI().getSynchro().getSessionFactory().getCurrentSession();
    session.update(parent);
    parent.getChildren().remove(this);
    parents.remove(parent);

    for (final TreeItem item : new LinkedList<TreeItem>(treeItems))
      if (parent.isThisOurTreeItem(item.getParentItem()))
        disposeSubItems(item);

    if (parents.size() == 0) {
      disposed();

      // delete associated EventList instance and events in case we are removing a DataView instance
      if (DataView.class.isInstance(this)) {
        // delete events
        final EventList el = ((Target) parent).eventLists.get(((DataView) this).browserEventClass().toString());
        session
        .createQuery("delete EventGeneric where eventList = :eventList")
        .setString("eventList", el.getId().toString())
        .executeUpdate();

        // delete EventList instance
        ((Target) parent).eventLists.remove(((DataView) this).browserEventClass().toString());
        session.delete(el);
      }

      // remove this instance
      session.delete(this);

      gui.updateEnableState();
    }
  }

  /**
   * Adds a sub element.
   * @param child element to add.
   * @return void.
   */
  // GUI thread
  // lock survey : synchro << sync_tree << HERE
  private void addChild(VisualElement child) {
    children.add(child);
  }

  /**
   * Returns children elements.
   * @param none.
   * @return java.util.List<VisualElement> children.
   */
  // GUI thread
  // lock survey : synchro << sync_tree << HERE
  public java.util.List<VisualElement> getChildren() {
    return children;
  }

  /**
   * Returns parents of this element.
   * @return java.util.List<VisualElement> parents.
   */
  public java.util.List<VisualElement> getParents() {
    return parents;
  }

  /**
   * Adds a parent.
   * @param parent new parent.
   * @return void.
   */
  public void addParent(final VisualElement parent) {
    if (!parents.contains(parent)) parents.add(parent);
  }

  /**
   * Checks that the parameter is a children.
   * @param elt element to check.
   * @return true if the parameter is a children.
   */
  // GUI thread
  public boolean contains(final VisualElement elt) {
    return children.contains(elt);
  }

  /**
   * Updates the displayed text (item name) of this element.
   * @param none.
   * @return void.
   */
  // main, Queue & GUI threads
  // lock survey : synchro << sync_tree << HERE
  //               synchro << HERE
  private void updateText() {
    initialize(null);

    final Runnable r = new Runnable() {
      public void run() {
        try {
          final Object sync = (gui != null) ? gui.sync_tree : new Object();
          synchronized (sync) { // bloqué ici sur synchro 25/4/08
            for (final TreeItem tree_item : treeItems)
              if (tree_item != null)
                tree_item.setText(new String [] { item, type, description });
          }
        } catch (final SWTException ex) {
          // widget is disposed
        }
      }
    };

    if (gui != null) gui.asyncExec(r);
    else r.run();
  }

  /**
   * Updates the displayed icon of this element.
   * @param none.
   * @return void.
   */
  // GUI thread
  private void updateImage() {
    initialize(null);

    final Runnable r = new Runnable() {
      public void run() {
        try {
          synchronized (gui.sync_tree) { // probablement sync_tree qui bloque
            for (final TreeItem tree_item : treeItems) // bcl sans fil qui arrive parfois au lancement (7)
              if (tree_item != null)
                tree_item.setImage(image);
          }
        } catch (final SWTException ex) {
          // widget is disposed
        }
      }
    };

    if (gui != null) gui.asyncExec(r); // bcl sans fil qui arrive parfois au lancement (6)
    else r.run();
  }

  /**
   * Sets the item name (displayed text).
   * @param item item name.
   * @return void.
   */
  // main, Queue & GUI threads
  protected void setItem(final String item) {
    this.item = item;
    updateText();
  }

  /**
   * Returns the item name.
   * @param none.
   * @return void.
   */
  public String getItem() {
    return item;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Sets the displayed type.
   * @param type type to display.
   * @return void.
   */
  // main, Queue & GUI threads
  // lock survey : synchro << sync_tree << HERE
  //               synchro << HERE
  public void setType(final String type) {
    this.type = type;
    updateText();
  }

  /**
   * Sets the description.
   * @param description description.
   * @return void.
   */
  // main, Queue & GUI threads
  public void setDescription(final String description) {
    this.description = description;
    updateText();
  }

  /**
   * Sets the icon.
   * @param image icon.
   * @return void.
   */
  // GUI thread
  protected void setImage(final Image image) {
    initialize(null);
    this.image = image;
    updateImage();
  }

  /**
   * Called when selected by the user.
   * @param none.
   * @return void.
   */
  // GUI thread
  public void informSelected() {}

  /**
   * Called when disposed.
   * @param none.
   * @return void.
   */
  // GUI thread
  protected void disposed() {
    disposed = true;
  }

  /**
   * Gets sub elements of a given type.
   * @param clazz type.
   * @param elts list that will be updated.
   * @return void.
   */
  private void getSubElements(final Class clazz, final java.util.List<VisualElement> elts) {
    for (final VisualElement elt : getChildren()) elt.getSubElements(clazz, elts);
    if (clazz.isInstance(this) && !elts.contains(this)) elts.add(this);
  }

  /**
   * Gets sub elements of a given type.
   * @param clazz type.
   * @return java.util.List<VisualElement> elts list that will be updated.
   */
  // les éléments sont retournés tels que les plus bas dans l'arbre sont après ceux plus haut
  public java.util.List<VisualElement> getSubElements(final Class clazz) {
    final java.util.List<VisualElement> elts = new java.util.LinkedList<VisualElement>();
    getSubElements(clazz, elts);
    return elts;
  }

  /**
   * Gets sub elements of a given type.
   * @param clazz type.
   * @return java.util.List<VisualElement> elts list that will be updated.
   */
  // lock survey : synchro << sync_tree << HERE
  // les éléments sont retournés tels que les plus bas dans l'arbre sont après ceux plus haut
  static public java.util.List<VisualElement> getSubElements(final TreeItem item, final Class clazz) {
    return ((VisualElement) item.getData(VisualElement.class.toString())).getSubElements(clazz);
  }

  /**
   * Returns every ascendant of this element, restricted to a given type.
   * @param clazz type.
   * @param elts elts list that will be updated.
   */
  private void getAllParents(final Class clazz, final java.util.List<VisualElement> elts) {
    for (final VisualElement elt : getParents()) elt.getAllParents(clazz, elts);
    if (clazz.isInstance(this) && !elts.contains(this)) elts.add(this);
  }

  /**
   * Returns every ascendant of this element, restricted to a given type.
   * @param clazz type.
   * @return java.util.List<VisualElement> elts list that will be updated to give the results.
   */
  public java.util.List<VisualElement> getAllParents(final Class clazz) {
    final java.util.List<VisualElement> elts = new java.util.LinkedList<VisualElement>();
    getAllParents(clazz, elts);
    return elts;
  }

  /**
   * Checks that the parameter can be attached to this element.
   * @param visual_element parameter to check.
   * @return true if the parameter can be attached to this element.
   */
  public boolean canManageThisChild(final VisualElement visual_element) {
    return true;
  }

  /**
   * Builds the subgraph rooted at the parent parameter.
   * @param parent root of the subgraph.
   * @param graph empty graph that will be updated to give the results.
   * @return void.
   */
  private void getSubGraph(final VisualElement parent, final java.util.List<Pair<VisualElement, VisualElement>> graph) {
    for (final VisualElement child : children) child.getSubGraph(this, graph);
    final Pair<VisualElement, VisualElement> link = new Pair<VisualElement, VisualElement>(this, parent);
    if (!graph.contains(link)) graph.add(new Pair<VisualElement, VisualElement>(this, parent));
  }

  /**
   * Builds the subgraph rooted at the parent parameter.
   * @param parent root of the subgraph.
   * @return java.util.List<Pair<VisualElement, VisualElement>> resulting graph.
   */
  private java.util.List<Pair<VisualElement, VisualElement>> getSubGraph(final VisualElement parent) {
    final java.util.List<Pair<VisualElement, VisualElement>> graph = new LinkedList<Pair<VisualElement, VisualElement>>();

    getSubGraph(parent, graph);
    return graph;
  }

  /**
   * Builds the subgraph rooted at the parent parameter, but not containing some links of another graph.
   * @param parent root of the subgraph.
   * @param graph empty graph that will be updated to give the results.
   * @param except_links another graph.
   * @return void.
   */
  private void getSubGraphExceptLinks(final VisualElement parent, final java.util.List<Pair<VisualElement, VisualElement>> graph, final java.util.List<Pair<VisualElement, VisualElement>> except_links) {
    for (final VisualElement child : children)
      if (!except_links.contains(new Pair<VisualElement, VisualElement>(child, this)))
        child.getSubGraphExceptLinks(this, graph, except_links);

    final Pair<VisualElement, VisualElement> link = new Pair<VisualElement, VisualElement>(this, parent);
    if (!graph.contains(link)) graph.add(link);
  }

  /**
   * Builds the subgraph rooted at the parent parameter, but not containing some links of another graph.
   * @param parent root of the subgraph.
   * @param graph empty graph that will be updated to give the results.
   * @return java.util.List<Pair<VisualElement, VisualElement>> resulting graph.
   */
  private java.util.List<Pair<VisualElement, VisualElement>> getSubGraphExceptLinks(final VisualElement parent, final java.util.List<Pair<VisualElement, VisualElement>> except_links) {
    final java.util.List<Pair<VisualElement, VisualElement>> graph = new LinkedList<Pair<VisualElement, VisualElement>>();

    getSubGraphExceptLinks(parent, graph, except_links);
    return graph;
  }

  /**
   * Detaches this element from one of its parents and removes children that have become orphan.
   * @param visual_parent parent.
   * @return void.
   */
  // lock survey : synchro << sync_tree << HERE
  public void removeVisualElements(final VisualElement visual_parent) {
    removeVisualElements(visual_parent, false);
  }

  /**
   * Detaches this element from one of its parents and removes children that have become orphan.
   * @param visual_parent parent.
   * @param bypass bypass some verifications.
   * @return void.
   */
  // lock survey : synchro << sync_tree << HERE
  private void removeVisualElements(final VisualElement visual_parent, final boolean bypass) {
    if (bypass == false) {
      if (equals(gui.getVisualTransient())) return;
      if (equals(gui.getVisualThisHost())) return;
      if (equals(gui.getVisualTransientAll())) return;
      if (equals(gui.getVisualTransientNetworks())) return;
      if (visual_parent.equals(gui.getVisualThisHost())) return;
      if (visual_parent.equals(gui.getVisualTransientAll())) return;
      if (visual_parent.equals(gui.getVisualTransientNetworks())) return;
    }

    final java.util.List<Pair<VisualElement, VisualElement>> subgraph = getSubGraph(visual_parent);

    final java.util.List<Pair<VisualElement, VisualElement>> except_links =
      new LinkedList<Pair<VisualElement, VisualElement>>();
    except_links.add(new Pair<VisualElement, VisualElement>(this, visual_parent));

    final java.util.List<Pair<VisualElement, VisualElement>> graph =
      gui.getVisualTransient().getSubGraphExceptLinks(null, except_links);

    GenericTools.substractGraph(subgraph, graph);

    for (final Pair<VisualElement, VisualElement> p : subgraph)
      p.former().unsetParent(p.latter());

    // removes under "every host"
    for (final VisualElement host : new LinkedList<VisualElement>(gui.getVisualTransientAll().getChildren()))
      if (host.getParents().size() == 1) host.removeVisualElements(gui.getVisualTransientAll(), true);

    // TODO : removes under "every network"
  }

  /**
   * Sets the progress bar position.
   * @param progress position.
   * @return void.
   */
  public void setProgress(final int progress) {
    this.progress = progress;
    if (is_selected == true && gui != null) gui.setProgress(progress);
  }

  /**
   * Gets the progress bar position for this visual element.
   * @param none.
   * @return progress position.
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Called when this element has been selected.
   * @param none.
   * @return void.
   */
  public void selected() {
    is_selected = true;
  }

  /**
   * Called when this element has been unselected.
   * @param none.
   * @return void.
   */
  public void unselected() {
    is_selected = false;
  }
}