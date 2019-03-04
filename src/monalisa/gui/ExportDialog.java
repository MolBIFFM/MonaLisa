/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.gui;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import monalisa.Project;
import monalisa.data.Pair;
import monalisa.gui.components.TaggedTreeNode;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Result;
import monalisa.tools.Tool;
import monalisa.tools.Tools;
import monalisa.util.FileUtils;
import monalisa.util.MonaLisaFileChooser;

public final class ExportDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = -28986325585509046L;
    private static final String ACTION_CANCEL = "ACTION_CANCEL";
    private static final String ACTION_EXPORT = "ACTION_EXPORT";
    private static final String ACTION_SAME_PATH_FOR_ALL = "ACTION_SAME_PATH_FOR_ALL";
    private static final String ACTION_PATH = "ACTION_PATH";

    public static final String DISTANCE_MATRIX = "distanceMatrix";
    public static final String CLUSTER = "cluster";

    private CheckboxTree tree;
    private JScrollPane scrollPane;
    private JCheckBox samePathForAll;
    private JTextField pathField;
    private JButton pathButton;
    private JButton cancelButton;
    private JButton exportButton;
    private boolean cancelled = true;
    private Map<Pair<Class<? extends Tool>, Configuration>, String> exportPaths;
    private final Project project;
    private final List<Class<? extends Tool>> exklusivExport;

    private static final StringResources strings =
        ResourceManager.instance().getDefaultStrings();

    public ExportDialog(JFrame owner, Project project) {
        super(owner, true);
        this.project = project;
        this.exklusivExport = null;
        initComponent();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public ExportDialog(Project project, List<Class<? extends Tool>> exklusivExport) {
        super(new JFrame(), true);
        this.project = project;
        this.exklusivExport = exklusivExport;
        initComponent();
        setVisible(true);
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public Map<Pair<Class<? extends Tool>, Configuration>, String> exportPaths() {
        return Collections.unmodifiableMap(exportPaths);
    }

    private void initComponent() {
        this.setSize(new Dimension(650,350));
        this.setPreferredSize(new Dimension(650,350));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        for (Class<? extends Tool> toolType : Tools.toolTypes()) {
            String displayTag = strings.get("Export" + toolType.getSimpleName());
            TaggedTreeNode toolNode = new TaggedTreeNode(toolType, displayTag);
            
            for (Configuration config : project.getResults(toolType).keySet()) {
                if(!config.isExportable())
                    continue;
                if(exklusivExport != null)
                    if(!exklusivExport.contains(toolType))
                        continue;
                TaggedTreeNode configNode = new TaggedTreeNode(config, config.toString(strings));
                toolNode.add(configNode);
            }
            
            if (toolNode.getChildCount() > 0)
                root.add(toolNode);
        }
        
        tree = new CheckboxTree(root);
        tree.setRootVisible(false);
        tree.getCheckingModel().setCheckingMode(CheckingMode.PROPAGATE_PRESERVING_CHECK);

        scrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(getWidth(), 100));

        samePathForAll = new JCheckBox(strings.get("SamePathForAll"));
        samePathForAll.setActionCommand(ACTION_SAME_PATH_FOR_ALL);
        samePathForAll.addActionListener(this);
        
        pathField = new JTextField();
        pathField.setEnabled(false);
        
        pathButton = new JButton("…");
        pathButton.setActionCommand(ACTION_PATH);
        pathButton.addActionListener(this);
        pathButton.setEnabled(false);
        
        cancelButton = new JButton(strings.get("Cancel"));
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);

        exportButton = new JButton(strings.get("Export"));
        exportButton.setActionCommand(ACTION_EXPORT);
        exportButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane)
            .addComponent(samePathForAll)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pathField)
                .addComponent(pathButton))
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(cancelButton)
                .addComponent(exportButton)));
        
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(scrollPane)
            .addComponent(samePathForAll)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(pathField)
                .addComponent(pathButton))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(cancelButton)
                .addComponent(exportButton)));
        
        layout.linkSize(SwingConstants.VERTICAL, pathField, pathButton);
        
        getRootPane().setDefaultButton(exportButton);
        
        setTitle(strings.get("ExportDialogTitle"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String action = e.getActionCommand();
        switch (action) {
            case ACTION_CANCEL:
                cancel();
                break;
            case ACTION_EXPORT:
                export();
                break;
            case ACTION_SAME_PATH_FOR_ALL:
                changePathEnabled();
                break;
            case ACTION_PATH:
                selectPath();
                break;
        }
    }
    
    private void cancel() {
        setVisible(false);
    }
    
    private void selectPath() {
        MonaLisaFileChooser chooser = new MonaLisaFileChooser(pathField.getText());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File file = chooser.getSelectedFile();
        String filename = stripExtension(file);
        pathField.setText(filename);
    }
    
    private String stripExtension(File file) {
        return file.getAbsolutePath().replaceAll("\\..*$", "");
    }
    
    private void changePathEnabled() {
        boolean enabled = samePathForAll.isSelected();
        pathField.setEnabled(enabled);
        pathButton.setEnabled(enabled);
        if (enabled && "".equals(pathField.getText())) {
            pathField.setText(project.getPath().getParent());
        }
    }

    @SuppressWarnings("unchecked")
    private void export() {
        List<Pair<Class<? extends Tool>, Configuration>> exports = new ArrayList<>();
        exportPaths = new HashMap<>();
        cancelled = false;

        for (TreePath path : tree.getCheckingPaths()) {
            // Ignore tools node (1st layer), only consider configurations (2nd layer).
            if (path.getPathCount() != 3)
                continue;

            DefaultMutableTreeNode toolNode = (DefaultMutableTreeNode) path.getPathComponent(1);
            DefaultMutableTreeNode configNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            Class<? extends Tool> toolType = (Class<? extends Tool>) toolNode.getUserObject();
            Configuration config = (Configuration) configNode.getUserObject();
            Pair<Class<? extends Tool>, Configuration> pair = new Pair<Class<? extends Tool>, Configuration>(toolType, config);
            exports.add(pair);
        }

        if (exports.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                strings.get("NoExportsSelectedMessage"),
                strings.get("NoExportsSelectedTitle"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (samePathForAll.isSelected()) {
            // Two different cases:
            //  1. The user has chosen a directory (ends with a slash, or exists).
            //     See whether it exists, create, if it doesn't.
            //     Create all files inside the directory.
            //  2. The user has chosen a (non existing?) file
            //     Create all files by extending file name accordingly.
            File basePath = new File(pathField.getText());
            boolean isDir = pathField.getText().endsWith(System.getProperty("file.separator")) || basePath.isDirectory();
            
            File containingFolder = basePath.getParentFile();
            
            if (!pathField.getText().contains(File.separator)) {
                // Assume that the user wants to use the current working directory.
                containingFolder = new File("").getAbsoluteFile();
                basePath = new File(containingFolder, pathField.getText());
            }
            else if (!containingFolder.exists()) {
                // Invalid folder location, abort.
                JOptionPane.showMessageDialog(
                    this,
                    strings.get("InvalidExportPathMessage"),
                    strings.get("InvalidExportPathTitle"),
                    JOptionPane.ERROR_MESSAGE);
                
                pathField.grabFocus();
                pathField.setSelectionStart(0);
                pathField.setSelectionEnd(pathField.getText().length());
                return;
            }

            if (isDir && !basePath.exists()) {
                // Folder (case 1) doesn't exist -- create it?
                int result = JOptionPane.showConfirmDialog(
                    this,
                    strings.get("CreateExportDirectoryMessage", containingFolder.getName()),
                    strings.get("CreateExportDirectoryTitle"),
                    JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION)
                    return;
                
                if(!containingFolder.mkdir()) {
                    JOptionPane.showMessageDialog(this,
                                strings.get("CreateDirectoryErrorMessage"),
                                strings.get("CreateDirectoryErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);

                    pathField.grabFocus();
                    pathField.setSelectionStart(0);
                    pathField.setSelectionEnd(pathField.getText().length());
                    return;
                }
            }
            
            String delimiter = isDir ? File.separator : "-";
            
            for (Pair<Class<? extends Tool>, Configuration> export : exports) {
                Result result = project.getResult(export.first(), export.second());
                String path = basePath.getAbsolutePath() + delimiter
                    + escapePathName(export.second().toString()) + "."
                    + result.filenameExtension();
                exportPaths.put(export, path);
            }
        }
        else {
            // Gather export file names manually.
            
            for (Pair<Class<? extends Tool>, Configuration> export : exports) {
                final Result result = project.getResult(export.first(), export.second());
                MonaLisaFileChooser fileChooser = new MonaLisaFileChooser(project.getPath());
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory()
                            || result.filenameExtension().equalsIgnoreCase(
                                FileUtils.getExtension(f));
                    }

                    @Override
                    public String getDescription() {
                        return strings.get(result.getClass().getSimpleName() + "File");
                    }
                });
                
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                    return;
                
                String filename = result.filenameExtension().equalsIgnoreCase(
                        FileUtils.getExtension(fileChooser.getSelectedFile())) ?
                    fileChooser.getSelectedFile().getAbsolutePath() :
                    fileChooser.getSelectedFile().getAbsolutePath()
                        + "." + result.filenameExtension();
                
                exportPaths.put(export, filename);
            }
        }
        
        setVisible(false);
    }
    
    private static String escapePathName(String name) {
        // Confidently allow Unicode letters, ASCII digits, hyphen and space.
        // Forbid all else.
        return name.replaceAll("[^\\p{L}\\p{Digit} -]", "_");
    }
    
}
