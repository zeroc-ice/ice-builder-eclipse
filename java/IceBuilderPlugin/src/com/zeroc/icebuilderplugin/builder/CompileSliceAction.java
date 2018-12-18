// **********************************************************************
//
// Copyright (c) 2008-present ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.builder;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.zeroc.icebuilderplugin.Activator;

public class CompileSliceAction implements IObjectActionDelegate
{
    @Override
    public void run(IAction action)
    {
        if(_selection instanceof IStructuredSelection)
        {
            for(Iterator<?> it = ((IStructuredSelection) _selection).iterator(); it.hasNext();)
            {
                Object element = it.next();
                final IProject project;
                if(element instanceof IProject)
                {
                    project = (IProject) element;
                }
                else if(element instanceof IAdaptable)
                {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                else
                {
                    project = null;
                }
                if(project != null)
                {
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
        }
    }
    
    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
        _selection = selection;
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
    }
    
    private ISelection _selection;
}
