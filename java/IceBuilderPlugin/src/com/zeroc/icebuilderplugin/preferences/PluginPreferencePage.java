// **********************************************************************
//
// Copyright (c) 2008-2018 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.preferences;

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
    public static final String BUILD_AUTO = "buildAutomatically";

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
        Composite parent = getFieldEditorParent();

        Group iceHomeGroup = new Group(parent, SWT.NONE);
        iceHomeGroup.setText("Ice Home");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        iceHomeGroup.setLayout(gridLayout);
        iceHomeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite composite = new Composite(iceHomeGroup, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        DirectoryFieldEditor iceHome = new IceHomeDirectoryFieldEditor(ICE_HOME, "", composite);
        iceHome.getTextControl(composite).setToolTipText("The directory containing your Ice installation.");
        addField(iceHome);

        BooleanFieldEditor buildAuto = new BooleanFieldEditor(BUILD_AUTO, "&Build automatically", parent);
        buildAuto.getDescriptionControl(parent).setToolTipText("Sets if the project should be built after saving changes to it.");
        addField(buildAuto);
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
                getPage().setMessage("Invalid Ice Home Directory", IMessageProvider.ERROR);
            }
            else
            {
                clearMessage();
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
}
