// **********************************************************************
//
// Copyright (c) 2008-2017 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.preferences;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.zeroc.icebuilderplugin.Activator;
import com.zeroc.icebuilderplugin.builder.Slice2JavaNature;
import com.zeroc.icebuilderplugin.internal.Configuration;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    public static final String ICE_HOME = "pathPreference";
    public static final String REBUILD_AUTO = "rebuildAutomatically";
    public static final String ICE_BUILD_PATH_PROBLEM = "com.zeroc.IceBuilderPlugin.marker.BuildProblemMarker";

    public PluginPreferencePage()
    {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors()
    {
        Group iceHomeGroup = new Group(getFieldEditorParent(), SWT.NONE);
        iceHomeGroup.setText("Ice Home");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        iceHomeGroup.setLayout(gridLayout);
        iceHomeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite composite = new Composite(iceHomeGroup, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addField(new IceHomeDirectoryFieldEditor(ICE_HOME, "&", composite));
        addField(new BooleanFieldEditor(REBUILD_AUTO, "&Rebuild automatically", getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    private static class IceHomeDirectoryFieldEditor extends DirectoryFieldEditor
    {

        public IceHomeDirectoryFieldEditor(String name, String labelText, Composite parent)
        {
            super(name, labelText, parent);
        }

        /**
         * Method declared on StringFieldEditor and overridden in
         * DirectoryFieldEditor. Checks whether the text input field contains a
         * valid directory.
         * 
         * @return True if the apply/ok button should be enabled in the pref
         *         panel
         */
        @Override
        protected boolean doCheckState()
        {
            String dir = getTextControl().getText();
            dir = dir.trim();

            if(!Configuration.verifyIceHome(dir))
            {
                if(!"Invalid Ice Home Directory".equals(getPage().getMessage()))
                {
                    getPage().setMessage("Invalid Ice Home Directory", IMessageProvider.ERROR);
                    addIceHomeWarnings();
                }
            }
            else
            {
                if("Invalid Ice Home Directory".equals(getPage().getMessage()))
                {
                    clearMessage();
                    removeIceHomeWarnings();
                }
            }
            return true;
        }

        @Override
        public Text getTextControl(Composite parent)
        {
            setValidateStrategy(VALIDATE_ON_KEY_STROKE);
            return super.getTextControl(parent);
        }
    }

    public static void addIceHomeWarnings()
    {
        for(IProject project : org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProjects(IProject.INCLUDE_HIDDEN))
        {
            try
            {
                if(project.hasNature(Slice2JavaNature.NATURE_ID))
                {
                    IMarker marker = project.createMarker(ICE_BUILD_PATH_PROBLEM);
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
                    marker.setAttribute(IMarker.LOCATION, "Ice Build Path");
                    marker.setAttribute(IMarker.MESSAGE, "Cannot locate Slice2Java compiler");
                }
            }
            catch(CoreException e) {} // Ignored
        }
    }

    public static void removeIceHomeWarnings()
    {
        for(IProject project : org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProjects(IProject.INCLUDE_HIDDEN))
        {
            try
            {
                if(project.hasNature(Slice2JavaNature.NATURE_ID))
                {
                    project.deleteMarkers(ICE_BUILD_PATH_PROBLEM, false, IProject.DEPTH_ZERO);
                }
            }
            catch(CoreException e) {} // Ignored
        }
    }
}
