/*
 * To change this license header, choose License Headers in  Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import monalisa.data.Pair;
import monalisa.data.input.TInputHandlers;
import monalisa.gui.components.CollapsiblePanel;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.results.Configuration;
import monalisa.results.Result;
import monalisa.results.TInvariantsConfiguration;
import monalisa.tools.BooleanChangeEvent;
import monalisa.tools.BooleanChangeListener;
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.ToolPanel;
import monalisa.tools.Tools;
import monalisa.tools.cluster.ClusterPanel;
import monalisa.tools.knockout.KnockoutPanel;
import monalisa.tools.mcs.McsPanel;
import monalisa.tools.mcts.MctsPanel;
import monalisa.tools.minv.MInvariantPanel;
import monalisa.tools.pinv.PInvariantPanel;
import monalisa.tools.tinv.TInvariantPanel;
import monalisa.tools.tinv.TInvariantTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel Gehrmann
 */
public class ToolUI implements BooleanChangeListener {

    private final ToolManager toolMan;
    private final Project project;
    transient private Map<Class<? extends Tool>, CollapsiblePanel> collapsibleToolPanels;
    transient private Map<Class<? extends Tool>, ToolPanel> toolPanels;
    private final static Logger LOGGER = LogManager.getLogger(ToolUI.class);
    transient List<ToolStatusUpdateListener> toolStatusUpdateListeners;
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    private static final Color COMPLETE_COLOR = Settings.getAsColor("completeResult");
    private static final Color HAS_RESULT_COLOR = Settings.getAsColor("hasResults");
    private static final Color WARNING_COLOR = Settings.getAsColor("warning");
    private static final Color ERROR_COLOR = Settings.getAsColor("error");
    private static final Color NOT_FINISHED_COLOR = Settings.getAsColor("notFinished");

    // Defines order of collapsible panels
    private static final List<Class<? extends ToolPanel>> toolPanTypes
            = Arrays.<Class<? extends ToolPanel>>asList(
                    TInvariantPanel.class,
                    MInvariantPanel.class,
                    PInvariantPanel.class,
                    MctsPanel.class,
                    ClusterPanel.class,
                    KnockoutPanel.class,
                    McsPanel.class
            );

    public ToolUI(ToolManager toolMan, Project project) {
        this.toolMan = toolMan;
        this.project = project;
    }

    public List<Class<? extends ToolPanel>> getToolPanels() {
        return Collections.unmodifiableList(toolPanTypes);
    }

    public ToolPanel createPanel(Class<? extends ToolPanel> type) {
        LOGGER.info("Creating new panel: " + type.getSimpleName());
        if (type.equals(ClusterPanel.class)) {
            return new ClusterPanel(project);
        } else if (type.equals(KnockoutPanel.class)) {
            return new KnockoutPanel(project);
        } else if (type.equals(McsPanel.class)) {
            return new McsPanel(project);
        } else if (type.equals(MctsPanel.class)) {
            return new MctsPanel(project);
        } else if (type.equals(MInvariantPanel.class)) {
            return new MInvariantPanel(project);
        } else if (type.equals(PInvariantPanel.class)) {
            return new PInvariantPanel(project);
        } else if (type.equals(TInvariantPanel.class)) {
            return new TInvariantPanel(project);
        } else {
            return null;
        }
    }

    /**
     * Creates the GUI for the Analyze Frame.
     *
     * @param container
     * @param strings
     */
    public void createAnalyzeFrame(final JComponent container, StringResources strings) {
        LOGGER.info("Creating UI for Analyze Frame.");
        GroupLayout containerLayout = (GroupLayout) container.getLayout();
        GroupLayout.ParallelGroup horizontal = containerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertical = containerLayout.createSequentialGroup();
        List<Tool> tools = toolMan.getTools();
        collapsibleToolPanels = new HashMap<>();
        toolPanels = new HashMap<>();
        if (tools != null) {
            LOGGER.info("Laying out tools.");
            JPanel[] allPanels = new JPanel[tools.size()];
            int panelIndex = 0;
            boolean first = false;
            // Alternate Variant
            for (Class<? extends ToolPanel> toolPanel : toolPanTypes) {
                if (!first) {
                    first = true;
                } else {
                    Component space = Box.createRigidArea(new Dimension(0, 3));
                    horizontal.addComponent(space);
                    vertical.addComponent(space);
                }
                ToolPanel tp = createPanel(toolPanel);
                Tool tool = toolMan.getTool(tp.getToolType());
                CollapsiblePanel panel = new CollapsiblePanel(strings.get(toolMan.getToolName(tool)));
                GroupLayout layout = new GroupLayout(panel);
                panel.setLayout(layout);

                layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent((Component) tp));
                layout.setVerticalGroup(layout.createSequentialGroup().addComponent((Component) tp));
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setPreferredSize(new Dimension(container.getWidth(), panel.getHeight()));
                horizontal.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
                vertical.addComponent(panel);
                allPanels[panelIndex++] = panel;
                collapsibleToolPanels.put(tool.getClass(), panel);
                toolPanels.put(tool.getClass(), tp);
                if (!tool.getClass().getSimpleName().equalsIgnoreCase("TInvariantTool")) {
                    panel.setCollapsed(true);
                }
                if (tp.finishedState(toolMan)) {
                    panel.setCollapsed(true);
                    setComplete(tool);
                } else if (toolMan.hasResults(tool)) {
                    setHasResults(tool);
                }
                tp.addActivityChangedListener(this);
            }
            containerLayout.setHorizontalGroup(horizontal);
            containerLayout.setVerticalGroup(vertical);
            containerLayout.linkSize(SwingConstants.HORIZONTAL, allPanels);
            LOGGER.info("Finished laying out tools.");
        }
        LOGGER.info("Finished creating UI for Analyze Frame.");
    }

    /**
     * Run a single tool.
     *
     * @param tool
     */
    private void runSingleTool(Tool tool) {
        if (toolPanels.get(tool.getClass()).isActive()) {
            if (!checkToolRequirements(tool)) {
                setFailed(tool, new ErrorLog());
                return;
            }
            // Get the config from toolPanels somehow
            ErrorLog log = toolMan.runSingleTool(tool, toolPanels.get(tool.getClass()).getConfig(), project);
            if (log.has(ErrorLog.Severity.ERROR)) {
                setFailed(tool, log);
            } else if (log.has(ErrorLog.Severity.WARNING)) {
                setCompleteWithWarnings(tool, log);
            } else {
                setHasResults(tool);
            }
            if (toolPanels.get(tool.getClass()).finishedState(toolMan)) {
                setComplete(tool);
            }
        }
    }

    /**
     * Checks if all requirements for a given tool are fulfilled.
     *
     * @param tool The Tool to be checked
     * @return <code> true </code> if all requirements are fulfilled, otherwise <code> false
     * </code>
     */
    private boolean checkToolRequirements(Tool tool) { // UI
        LOGGER.info("Checking requirements for tool " + strings.get(Tools.name(tool)));
        List<Pair<Class<? extends Tool>, Configuration>> requirements = toolPanels.get(tool.getClass()).getRequirements();
        boolean requirementsSatisfied = true;
        for (Pair<Class<? extends Tool>, Configuration> requirement : requirements) {
            if (!toolMan.hasResult(requirement.first(), requirement.second())) {
                requirementsSatisfied = false;
                break;
            }
        }
        if (!requirementsSatisfied) {
            String toolName = strings.get(Tools.name(tool));
            LOGGER.warn("Requirements for tool " + toolName + " not met");
            JOptionPane.showMessageDialog(MonaLisa.appMainWindow(), strings.get("NotAllRequirementsSatisfiedMessage", toolName), strings.get("NotAllRequirementsSatisfiedTitle", toolName), JOptionPane.ERROR_MESSAGE);
        } else {
            LOGGER.info("Requirements for tool " + strings.get(Tools.name(tool)) + " met");
        }
        return requirementsSatisfied;
    }

    // runGivenTools and runSelectedTools are somewhat problematic and definitely need testing.
    /**
     * Starts all given tools.
     *
     * @param givenTool Tools to run.
     */
    public void runGivenTools(final List<String> givenTool) { // Called from NetViewer
        LOGGER.info("Running given tools.");
        toolMan.resetToolMessages();
        toolMan.toolsThread = new Thread() {
            @Override
            public void run() {
                for (Tool tool : toolMan.getTools()) {
                    if (givenTool.contains(tool.getClass().getName())) {
                        toolPanels.get(tool.getClass()).setActive(true);
                        if (toolMan.toolsThread.isInterrupted()) {
                            return;
                        }
                        runSingleTool(tool);
                    }
                }
                toolMan.fireToolStatusUpdate(new ToolStatusUpdateEvent(this, null, ToolStatusUpdateEvent.Status.FINISHED_ALL));
            }
        };
        toolMan.toolsThread.start();
    }

    /**
     * Starts all selected Tools.
     */
    public void runSelectedTools() { // Called from MainDialog
        LOGGER.info("Running selected tools.");
        toolMan.resetToolMessages();
        toolMan.toolsThread = new Thread() {
            @Override
            public void run() {
                for (Tool tool : toolMan.getTools()) {
                    if (toolMan.toolsThread.isInterrupted()) {
                        return;
                    }
                    runSingleTool(tool);
                }
                toolMan.fireToolStatusUpdate(new ToolStatusUpdateEvent(this, null, ToolStatusUpdateEvent.Status.FINISHED_ALL));
            }
        };
        toolMan.toolsThread.start();
    }

    /**
     * Sets the reults of the toolManager to the given results
     *
     * @param results
     */
    public void setResults(Map<Class<? extends Tool>, Map<Configuration, Result>> results) { // Calls UI (setHasResults, setHasNoResults)
        // Check for existing results and mark these tools as calculated
        Tool t;
        for (Class<? extends Tool> toolClass : Tools.toolTypes()) {
            t = toolMan.getTool(toolClass);
            if (toolMan.hasResults(t)) {
                setHasResults(t);
            } else {
                setHasNoResults(t);
            }
        }
    }

    /**
     * Marks a given tool with the "failed" tag
     *
     * @param tool The tool to be marked
     */
    private void setFailed(Tool tool, ErrorLog log) { // UI
        LOGGER.warn("Setting tool as failed for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = collapsibleToolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolFailure"));
        panel.setTitleShade(ERROR_COLOR);
    }

    /**
     * Marks a given tool with the "has results" tag
     *
     * @param tool The tool to be marked
     */
    private void setHasResults(Tool tool) { // UI
        LOGGER.info("Setting tool results for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = collapsibleToolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolHasResults"));
        panel.setTitleShade(HAS_RESULT_COLOR);
    }

    /**
     * Marks a given tool with the "has no results" tag
     *
     * @param tool The tool to be marked
     */
    private void setHasNoResults(Tool tool) { // UI
        LOGGER.info("Setting lack of tool results for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = collapsibleToolPanels.get(tool.getClass());
        panel.setComment("");
        panel.setTitleShade(NOT_FINISHED_COLOR);
    }

    /**
     * Marks a given tool with the "complete" tag
     *
     * @param tool The tool to be marked
     */
    private void setComplete(Tool tool) {  // UI
        LOGGER.info("Setting tool complete for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = collapsibleToolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolComplete"));
        panel.setTitleShade(COMPLETE_COLOR);
    }

    /**
     * Marks a given tool with the "complete with warnings" tag
     *
     * @param tool The tool to be marked
     */
    private void setCompleteWithWarnings(Tool tool, ErrorLog log) { // UI
        LOGGER.warn("Setting tool as complete with warnings for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = collapsibleToolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolCompleteWithWarnings"));
        panel.setTitleShade(WARNING_COLOR);
    }

    /**
     * Load the given file and tries to construct given invariants and matches
     * them to the Petri net.
     *
     * @param TFile
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadTFile(File TFile) throws IOException, InterruptedException {
        LOGGER.info("Loading from given TFile.");
        Result loadTFileResults = TInputHandlers.load(TFile, project.getPetriNet());
        Configuration loadTFileConfig = new TInvariantsConfiguration();
        Tool tool = new TInvariantTool();
        toolMan.putResult(tool, loadTFileConfig, loadTFileResults);
        toolPanels.get(tool.getClass()).finishedState(toolMan);
        setComplete(tool);
        LOGGER.info("Finished loading from given TFile.");
    }

    @Override
    public void changed(BooleanChangeEvent e) {
        ToolPanel tp = (ToolPanel) e.getSource();
        Tool tool = toolMan.getTool(tp.getToolType());
        LOGGER.info("Change in requirements for '" + strings.get(Tools.name(tool)) + "'");
        if (e.getNewValue()) {
            LOGGER.info("Selecting requirements for tool configuration of '" + strings.get(Tools.name(tool)) + "' that are not yet calculated");
            // Select all requirements for the tool configuration that are not
            // yet calculated.
            List<Pair<Class<? extends Tool>, Configuration>> requirements = toolPanels.get(tool.getClass()).getRequirements();
            for (Pair<Class<? extends Tool>, Configuration> requirement : requirements) {
                if (toolMan.hasResult(requirement.first(), requirement.second())) {
                    continue;
                }
                toolPanels.get(toolMan.getTool(requirement.first()).getClass()).setActive(requirement.second());
            }
        } else {
            LOGGER.info("Deselecting all tools that are dependent on " + strings.get(Tools.name(tool)));
            // Deselect all tools that depend on this tool.
            for (Tool otherTool : toolMan.getTools()) {
                List<Pair<Class<? extends Tool>, Configuration>> requirements = toolPanels.get(otherTool.getClass()).getRequirements();
                for (Pair<Class<? extends Tool>, ?> requirement : requirements) {
                    if (requirement.first().isInstance(tool)) {
                        toolPanels.get(otherTool.getClass()).setActive(false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the assoicated ToolManager
     *
     * @return
     */
    public ToolManager getToolManager() {
        return toolMan;
    }
}
