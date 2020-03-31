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
import monalisa.tools.ErrorLog;
import monalisa.tools.Tool;
import monalisa.tools.Tools;
import monalisa.tools.tinv.TInvariantTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel Gehrmann
 */
public class ToolUI {
    
    private final ToolManager toolMan;
    transient private Map<Class<? extends Tool>, CollapsiblePanel> toolPanels;
    
    private final static Logger LOGGER = LogManager.getLogger(ToolUI.class);
    transient List<ToolStatusUpdateListener> toolStatusUpdateListeners;
    Map<Class<? extends Tool>, Map<Configuration, Result>> results;
    private static final StringResources strings = ResourceManager.instance().getDefaultStrings();
 
    private static final Color COMPLETE_COLOR = Settings.getAsColor("completeResult");
    private static final Color HAS_RESULT_COLOR = Settings.getAsColor("hasResults");
    private static final Color WARNING_COLOR = Settings.getAsColor("warning");
    private static final Color ERROR_COLOR = Settings.getAsColor("error");
    private static final Color NOT_FINISHED_COLOR = Settings.getAsColor("notFinished");
    
    public ToolUI(ToolManager toolMan){
        this.toolMan = toolMan;
    }

    /**
     * Creates the GUI for the Analyze Frame.
     * @param container
     * @param strings
     */
    public void createAnalyzeFrame(final JComponent container, StringResources strings) {
        LOGGER.info("Creating UI for Analyze Frame.");
        GroupLayout containerLayout = (GroupLayout) container.getLayout();
        GroupLayout.ParallelGroup horizontal = containerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertical = containerLayout.createSequentialGroup();
        List<Tool> tools = toolMan.getTools();
        toolPanels = new HashMap<>();
        if (tools != null) {
            LOGGER.info("Laying out tools.");
            JPanel[] allPanels = new JPanel[tools.size()];
            int panelIndex = 0;
            boolean first = false;
            for (Tool tool : tools) {
                if (!first) {
                    first = true;
                } else {
                    Component space = Box.createRigidArea(new Dimension(0, 3));
                    horizontal.addComponent(space);
                    vertical.addComponent(space);
                }
                CollapsiblePanel panel = new CollapsiblePanel(strings.get(toolMan.getToolName(tool)));
                GroupLayout layout = new GroupLayout(panel);
                panel.setLayout(layout);
                layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(tool.getUI(toolMan.getProject(), strings)));
                layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tool.getUI(toolMan.getProject(), strings)));
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setPreferredSize(new Dimension(container.getWidth(), panel.getHeight()));
                horizontal.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
                vertical.addComponent(panel);
                allPanels[panelIndex++] = panel;
                toolPanels.put(tool.getClass(), panel);
                if (!tool.getClass().getSimpleName().equalsIgnoreCase("TInvariantTool")) {
                    panel.setCollapsed(true);
                }
                if (tool.finishedState(toolMan)) {
                    panel.setCollapsed(true);
                    setComplete(tool);
                } else if (toolMan.hasResults(tool)) {
                    setHasResults(tool);
                }
                tool.addActivityChangedListener(toolMan); // This line here is functionality for sure, but how would I change it? Just do it on startup of the toolMan?
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
     * @param tool
     */
    private void runSingleTool(Tool tool) { // Also needs to remain here in part
        if (tool.isActive()) {
            if (!checkToolRequirements(tool)) {
                setFailed(tool, new ErrorLog());
                return;
            }
            ErrorLog log = toolMan.runSingleTool(tool);
            if (log.has(ErrorLog.Severity.ERROR)) {
                setFailed(tool, log);
            } else if (log.has(ErrorLog.Severity.WARNING)) {
                setCompleteWithWarnings(tool, log);
            } else {
                setHasResults(tool);
            }
            if (tool.finishedState(toolMan)) {
                setComplete(tool);
            }
        }
    }

    /**
     * Checks if all requirements for a given tool are fulfilled.
     * @param tool The Tool to be checked
     * @return <code> true </code> if all requirements are fulfilled, otherwise <code> false </code>
     */
    private boolean checkToolRequirements(Tool tool) { // UI
        LOGGER.info("Checking requirements for tool " + strings.get(Tools.name(tool)));
        List<Pair<Class<? extends Tool>, Configuration>> requirements = tool.getRequirements();
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
                        tool.setActive(true);
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
     * Sets the reults of the project to the given results
     * @param results
     */
    public void setResults(Map<Class<? extends Tool>, Map<Configuration, Result>> results) { // Calls UI (setHasResults, setHasNoResults)
        this.results = results;
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
     *  Marks a given tool with the "failed" tag
     * @param tool The tool to be marked
     */
    private void setFailed(Tool tool, ErrorLog log) { // UI
        LOGGER.warn("Setting tool as failed for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolFailure"));
        panel.setTitleShade(ERROR_COLOR);
    }

    /**
     * Marks a given tool with the "has results" tag
     * @param tool The tool to be marked
     */
    private void setHasResults(Tool tool) { // UI
        LOGGER.info("Setting tool results for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolHasResults"));
        panel.setTitleShade(HAS_RESULT_COLOR);
    }

    /**
     * Marks a given tool with the "has no results" tag
     * @param tool The tool to be marked
     */
    private void setHasNoResults(Tool tool) { // UI
        LOGGER.info("Setting lack of tool results for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment("");
        panel.setTitleShade(NOT_FINISHED_COLOR);
    }

    /**
     *  Marks a given tool with the "complete" tag
     * @param tool The tool to be marked
     */
    private void setComplete(Tool tool) {  // UI
        LOGGER.info("Setting tool complete for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolComplete"));
        panel.setTitleShade(COMPLETE_COLOR);
    }

    /**
     *  Marks a given tool with the "complete with warnings" tag
     * @param tool The tool to be marked
     */
    private void setCompleteWithWarnings(Tool tool, ErrorLog log) { // UI
        LOGGER.warn("Setting tool as complete with warnings for " + strings.get(Tools.name(tool)));
        CollapsiblePanel panel = toolPanels.get(tool.getClass());
        panel.setComment(strings.get("ToolCompleteWithWarnings"));
        panel.setTitleShade(WARNING_COLOR);
    }

    /**
     * Load the given file and tries to construct given invariants and matches them to the Petri net.
     * @param TFile
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadTFile(File TFile) throws IOException, InterruptedException {
        LOGGER.info("Loading from given TFile.");
        Result loadTFileResults = TInputHandlers.load(TFile, toolMan.getProject().getPetriNet());
        Configuration loadTFileConfig = new TInvariantsConfiguration();
        Tool tool = new TInvariantTool();
        toolMan.putResult(tool, loadTFileConfig, loadTFileResults);
        tool.finishedState(toolMan);
        setComplete(tool);
        LOGGER.info("Finished loading from given TFile.");
    }

    /**
     * Returns the assoicated ToolManager
     * @return
     */
    public ToolManager getToolManager(){
        return toolMan;
    }
}