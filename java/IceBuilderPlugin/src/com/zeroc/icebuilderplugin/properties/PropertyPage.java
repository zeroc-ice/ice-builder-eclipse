//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin.properties;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public abstract class PropertyPage extends org.eclipse.ui.dialogs.PropertyPage
{
    public PropertyPage()
    {
        noDefaultAndApplyButton();
    }

    public void performApply()
    {
        super.performApply();
    }
    
    protected Control createIncludes(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        _includes = new List(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
        _includes.setToolTipText("Specifies directories to be added to the Slice include path.");
        _includes.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite c2 = new Composite(composite, SWT.NONE);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        c2.setLayout(gridLayout);

        Button but1 = new Button(c2, SWT.PUSH);
        but1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        but1.setText("Add");
        but1.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                IProject project = getProject();
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                String dir = dialog.open();
                if(dir != null)
                {
                    IPath projectLocation = project.getLocation();
                    IPath includeLocation = new Path(dir);

                    // If the directory is located within the project,
                    // Convert the absolute path to a relative path
                    if(projectLocation.isPrefixOf(includeLocation))
                    {
                        int n = projectLocation.matchingFirstSegments(includeLocation);
                        includeLocation = includeLocation.removeFirstSegments(n).setDevice(null);
                    }

                    _includes.add(includeLocation.toString());
                }
            }
        });
        Button but2 = new Button(c2, SWT.PUSH);
        but2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        but2.setText("Remove");
        but2.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                _includes.remove(_includes.getSelectionIndices());
            }
        });
        Button but3 = new Button(c2, SWT.PUSH);
        but3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        but3.setText("Up");
        but3.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                int index = _includes.getSelectionIndex();
                if(index > 0)
                {
                    String[] items = _includes.getItems();
                    String tmp = items[index-1];
                    items[index-1] = items[index];
                    items[index] = tmp;
                    _includes.setItems(items);
                    _includes.setSelection(index-1);
                }
            }
        });
        Button but4 = new Button(c2, SWT.PUSH);
        but4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        but4.setText("Down");
        but4.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                int index = _includes.getSelectionIndex();
                if(index != -1)
                {
                    String[] items = _includes.getItems();
                    if(index != items.length-1)
                    {
                        String tmp = items[index+1];
                        items[index+1] = items[index];
                        items[index] = tmp;
                        _includes.setItems(items);
                        _includes.setSelection(index+1);
                    }
                }
            }
        });

        return composite;
    }
    
    protected void createPreOptions(Composite parent)
    {
    }
    
    protected void createPostOptions(Composite parent)
    {
    }

    protected Control createOptions(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
        
        createPreOptions(composite);

        Group includesGroup = new Group(composite, SWT.NONE);
        includesGroup.setText("Include Directories");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        includesGroup.setLayout(gridLayout);
        includesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        createIncludes(includesGroup);
        
        Group extraArgumentsGroup = new Group(composite, SWT.NONE);
        extraArgumentsGroup.setText("Additional Options");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        extraArgumentsGroup.setLayout(gridLayout);
        extraArgumentsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        createExtraArguments(extraArgumentsGroup);
        
        createPostOptions(composite);
        
        return composite;
    }
    
    public Control createExtraArguments(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginLeft = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Link l = new Link(composite, SWT.WRAP);
        l.setText("");
        l.setText("For a list of all the supported Slice2Java compiler options consult the "
                 +"<a>Ice Manual</a>.");
        l.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                try{
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(
                            "https://doc.zeroc.com/display/Ice/slice2java+Command-Line+Options"));
                }
                catch(Exception e)
                {
                    // Ignored
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event)
            {
                widgetSelected(event);
            }
        });
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 400;
        l.setLayoutData(gridData);

        _extraArguments = new Text(composite, SWT.BORDER);
        _extraArguments.setToolTipText("Additional command line options to pass to the Slice compiler.");
        _extraArguments.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    protected IProject getProject()
    {
        IAdaptable a = getElement();
        if(a instanceof IProject)
        {
            return (IProject)a;
        }
        else if(a instanceof IJavaProject)
        {
            return ((IJavaProject) a).getProject();
        }
        else if(a instanceof IResource)
        {
            return ((IResource)a).getProject();
        }
        else
        {
            assert(false);
            return null;
        }
    }

    protected List _includes;
    protected Text _extraArguments;
}
