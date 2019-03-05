/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa;

import java.util.EventObject;

import monalisa.tools.Tool;

/**
 * An event object holding a {@link Tool} execution status update.
 * @author Konrad Rudolph
 */
public class ToolStatusUpdateEvent extends EventObject {
    private static final long serialVersionUID = -4360345569897963921L;

    /**
     * The kind of status update.
     * @author Konrad Rudolph
     */
    public enum Status {
        STARTED,
        FINISHED,
        PROGRESS,
        ABORTED,
        FINISHED_ALL,
    }
    
    private final String toolName;
    private final Status status;
    private final int progress;
    
    public ToolStatusUpdateEvent(Object source, String toolName, int progress) {
        this(source, toolName, Status.PROGRESS, progress);
    }

    public ToolStatusUpdateEvent(Object source, String toolName, Status status) {
        this(source, toolName, status, -1);
    }

    private ToolStatusUpdateEvent(Object source, String toolName, Status status, int progress) {
        super(source);
        this.toolName = toolName;
        this.status = status;
        this.progress = progress;
    }

    public String getToolName() {
        return toolName;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public int getProgress() {
        return progress;
    }
}
