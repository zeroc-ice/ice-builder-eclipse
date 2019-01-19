//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin.properties;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.zeroc.icebuilderplugin.Activator;
import com.zeroc.icebuilderplugin.builder.Slice2JavaBuilder;
import com.zeroc.icebuilderplugin.internal.Configuration;
import com.zeroc.icebuilderplugin.preferences.PluginPreferencePage;

public class ProjectProperties extends PropertyPage
{
    public ProjectProperties()
    {
        setTitle("Slice2Java Settings");
        noDefaultAndApplyButton();
    }

    public void performApply()
    {
        super.performApply();
    }

    public boolean performOk()
    {
        final IProject project = getProject();

        try
        {
            _config.setGeneratedDir(_generatedDir.getText());
            _config.setIncludes(Arrays.asList(_includes.getItems()));
            _config.setExtraArguments(_extraArguments.getText());

            if(_config.write() && Activator.getDefault().getPreferenceStore().getBoolean(PluginPreferencePage.BUILD_AUTO))
            {
                // The configuration properties were changed. We need to rebuild the slice files.
                Job job = new Job("Rebuild")
                {
                    protected IStatus run(IProgressMonitor monitor)
                    {
                        try
                        {
                            project.build(IncrementalProjectBuilder.FULL_BUILD, Slice2JavaBuilder.BUILDER_ID, null,
                                    monitor);
                        }
                        catch(CoreException e)
                        {
                            return new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "rebuild failed", e);
                        }
                        return Status.OK_STATUS;
                    }
                };
                job.setPriority(Job.BUILD);
                job.schedule(); // start as soon as possible
            }
        }
        catch(CoreException e)
        {
            return false;
        }
        catch(IOException e)
        {
            ErrorDialog.openError(getShell(), "Error", "Error saving preferences", new Status(Status.ERROR,
                    Activator.PLUGIN_ID, 0, null, e));
            return false;
        }
        return true;
    }

    protected void createPreOptions(Composite parent)
    {
        Group gclGroup = new Group(parent, SWT.NONE);
        gclGroup.setText("Generated Code Directory");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gclGroup.setLayout(gridLayout);
        gclGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tc = new Composite(gclGroup, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        tc.setLayout(gridLayout);
        tc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite c = new Composite(tc, SWT.NONE);

        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        gridLayout2.marginLeft = 0;
        gridLayout2.marginTop = 0;
        gridLayout2.marginBottom = 0;
        c.setLayout(gridLayout2);

        c.setLayoutData(new GridData(GridData.FILL_BOTH));

        _generatedDir = new Text(c, SWT.BORDER | SWT.READ_ONLY);
        _generatedDir.setToolTipText("Directory where generated files are created.");
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        // gridData.horizontalSpan = 2;
        _generatedDir.setLayoutData(gridData);

        Button but3 = new Button(c, SWT.PUSH);
        but3.setText("Browse");
        but3.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                IProject project = getProject();

                DirectorySelectionDialog dialog = new DirectorySelectionDialog(getShell(), project,
                        "Select the Generated Code Directory");
                if(dialog.open() == ContainerSelectionDialog.OK)
                {
                    Object[] selection = dialog.getResult();
                    if(selection.length == 1)
                    {
                        IFolder path = (IFolder) selection[0];
                        String oldPath = _generatedDir.getText();
                        String newPath = path.getProjectRelativePath().toString();
                        if(oldPath.equals(newPath))
                        {
                            return;
                        }
                        try
                        {
                            if(path.members().length > 0)
                            {
                                ErrorDialog.openError(getShell(), "Error",
                                        "The generated code directory should be an empty folder", new Status(Status.ERROR,
                                                Activator.PLUGIN_ID, "The chosen directory '"
                                                        + path.getFullPath().toOSString() + "' is not empty."));
                                return;
                            }
                        }
                        catch(CoreException ex)
                        {
                            ErrorDialog.openError(getShell(), "Error", ex.toString(), new Status(Status.ERROR,
                                    Activator.PLUGIN_ID, 0, "Failed to set the generated code directory.", ex));
                            return;
                        }
                        _generatedDir.setText(newPath);
                    }
                }
            }
        });
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        Control source = createOptions(parent);

        loadPrefs();

        return source;
    }

    private void loadPrefs()
    {
        _config = Configuration.getConfiguration(getProject());

        _generatedDir.setText(_config.getGeneratedDir());
        for(Iterator<String> iter = _config.getBareIncludes().iterator(); iter.hasNext();)
        {
            _includes.add(iter.next());
        }
        _extraArguments.setText(_config.getExtraArguments());

        checkValid();
    }

    private void checkValid()
    {
        IProject project = getProject();
        IFolder folder = project.getFolder(_generatedDir.getText());
        if(!folder.exists())
        {
            setErrorMessage("Generated folder does not exist");
            setValid(false);
            return;
        }
        setValid(true);
        setErrorMessage(null);
    }

    protected Configuration _config;

    private Text _generatedDir;
}
