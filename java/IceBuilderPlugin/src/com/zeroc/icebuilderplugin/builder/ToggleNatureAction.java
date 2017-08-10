// **********************************************************************
//
// Copyright (c) 2008-2017 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.builder;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.zeroc.icebuilderplugin.internal.Configuration;

public class ToggleNatureAction implements IObjectActionDelegate
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */

    public void run(IAction action)
    {
        if(_selection instanceof IStructuredSelection)
        {
            for(Iterator<?> it = ((IStructuredSelection) _selection).iterator(); it.hasNext();)
            {
                Object element = it.next();
                IProject project = null;
                if(element instanceof IProject)
                {
                    project = (IProject) element;
                }
                else if(element instanceof IAdaptable)
                {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                if(project != null)
                {
                    toggleNature(project);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        this._selection = selection;
        //action.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
     * action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        //action.setEnabled(false);
    }

    /**
     * Toggles sample nature on a project
     * 
     * @param project
     *            to have sample nature added or removed
     */
    private void toggleNature(IProject project)
    {
        try
        {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();

            for(int i = 0; i < natures.length; ++i)
            {
                if(Slice2JavaNature.NATURE_ID.equals(natures[i]))
                {
                    // Remove the nature
                    String[] newNatures = new String[natures.length - 1];
                    System.arraycopy(natures, 0, newNatures, 0, i);
                    System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
                    description.setNatureIds(newNatures);
                    project.setDescription(description, null);
                    
                    Configuration.getConfiguration(project).deinstall();
             
                    return;
                }
            }

            // Add the nature
            String[] newNatures = new String[natures.length + 1];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = Slice2JavaNature.NATURE_ID;
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        }
        catch(CoreException e)
        {
        }
    }

    private ISelection _selection;
}
