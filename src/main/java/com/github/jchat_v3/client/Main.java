package main.java.com.github.jchat_v3.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class Main extends JFrame {
    public static void main(String[] args) {
        new Main();
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String POISON = Double.toString(Double.MAX_VALUE);
    private static final int MAXTABS = 10;

    private String name;
    private HashMap<String, Tab> tabs;
    private JTabbedPane tabbedPane;
    private JMenu deleteTabMenu;
    private JMenuItem newTab;
    private JMenuItem changeName;

    public Main() {
        super("Java Chatroom V3");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon("src\\main\\rescources\\java_chatroom_icon.png");
        setIconImage(icon.getImage());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error: ", exception);
        }

        tabs = new HashMap<>();
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        JMenuBar top = new JMenuBar();
        newTab = new JMenuItem("New tab");
        newTab.setSize(this.getWidth() / 3, top.getHeight());
        newTab.addActionListener(new ClickHandler());
        deleteTabMenu = new JMenu("Delete tab");
        deleteTabMenu.setSize(this.getWidth() / 3, top.getHeight());
        changeName = new JMenuItem("Change name");
        changeName.addActionListener(new ClickHandler());
        changeName.setSize(this.getWidth() / 3, top.getHeight());

        top.add(changeName);
        top.add(newTab);
        top.add(deleteTabMenu);

        deleteTabMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent event) {
                if (event.getSource() == deleteTabMenu) {
                    updateMenuContents();
                }
            }

            // #region
            @Override
            public void menuDeselected(MenuEvent event) {
                /**/}

            @Override
            public void menuCanceled(MenuEvent event) {
                /**/}
            // #endregion
        });

        add(top, BorderLayout.NORTH);
        setVisible(true);
    }

    public void updateName() {
        for (Tab tab : tabs.values()) {
            tab.setNameVar(this.name);
        }
    }

    public void updateMenuContents() {
        deleteTabMenu.removeAll();
        for (String index : tabs.keySet()) {
            JMenuItem item = new JMenuItem(index);
            item.addActionListener(new ClickHandler());
            deleteTabMenu.add(item);
        }
    }

    public void addTab() {
        if (tabs.size() >= MAXTABS) {
            JOptionPane.showMessageDialog(this, "Max limit of tabs is " + MAXTABS, "Tab limit reached.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = "Tab " + (tabs.size() + 1);
        Tab toAdd = new Tab(getConnectionDetails());

        tabbedPane.addTab(title, toAdd);
        tabs.put(title, toAdd);

        if (this.name == null) {
            changeName();
        } else {
            toAdd.getConnection().setNameVar(this.name);
        }
    }

    public void deleteTab(String key) {
        if (tabs.get(key).getConnection().getOutput() != null)
            tabs.get(key).send(POISON);

        tabs.get(key).getThread().setActive(false);
        tabbedPane.remove(tabs.get(key));
        tabs.remove(key);
    }

    public Connection getConnectionDetails() {
        Login login = new Login(this);

        try {
            while (!login.hasInput()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Thread interrupted.", exception);
            Thread.currentThread().interrupt();
        }

        return new Connection(login.getPort(), login.getHost(), this);
    }

    public void changeName() {
        ChangeName namePopup = new ChangeName(this);

        try {
            while (!namePopup.hasInput()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Thread interrupted.", exception);
            Thread.currentThread().interrupt();
        }

        this.name = namePopup.getNameVar();
        updateName();
    }

    private class ClickHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == newTab)
                addTab();

            if (tabs.containsKey(event.getSource().toString().split("text=")[1].split("]")[0]))
                deleteTab(event.getSource().toString().split("text=")[1].split("]")[0]);

            if (event.getSource() == changeName)
                changeName();

        }
    }
}
