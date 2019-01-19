//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.zeroc.icebuilderplugin.builder.Slice2JavaBuilder;
import com.zeroc.icebuilderplugin.builder.Slice2JavaNature;
import com.zeroc.icebuilderplugin.internal.Configuration;
import com.zeroc.icebuilderplugin.preferences.PluginPreferencePage;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

    // The plug-in ID
    public static final String PLUGIN_ID = "com.zeroc.IceBuilderPlugin";
    
    /**
     * The constructor
     */
    public Activator()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context)
        throws Exception
    {
        super.start(context);
        _plugin = this;
        
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        // set the listener for the preference change
        prefs.addPreferenceChangeListener(new IPreferenceChangeListener()
        {
            public List<IJavaProject> getSlice2JavaProjects(IJavaModel javaModel)
            {
                ArrayList<IJavaProject> pl = new ArrayList<IJavaProject>();
                try
                {
                    for(IJavaProject p : javaModel.getJavaProjects())
                    {
                        try
                        {
                            if(p.getProject().hasNature(Slice2JavaNature.NATURE_ID))
                            {
                                pl.add(p);
                            }
                        }
                        catch(CoreException e)
                        {
                            // The project isn't opened, or does not exist.
                        }
                    }
                }
                catch(JavaModelException jme)
                {
                }

                return pl;
            }
            
            public void preferenceChange(PreferenceChangeEvent event)
            {
                String property = event.getKey();
                if(PluginPreferencePage.ICE_HOME.equals(property) && getPreferenceStore().getBoolean(PluginPreferencePage.BUILD_AUTO))
                {
                    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                    IJavaModel javaModel = JavaCore.create(workspaceRoot);
                    List<IJavaProject> projects = getSlice2JavaProjects(javaModel);

                    // Need to trigger a clean build of the projects.
                    for(final IJavaProject p : projects)
                    {
                        Job job = new Job("Rebuild")
                        {
                            protected IStatus run(IProgressMonitor monitor)
                            {
                                try
                                {
                                    p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, Slice2JavaBuilder.BUILDER_ID, null,
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
        });

        // Add a listener for when the workbench is refreshed
        ICommandService commandService = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
        if(commandService != null)
        {
            commandService.addExecutionListener(new IExecutionListener()
            {
                @Override
                public void notHandled(String commandId, NotHandledException exception)
                {
                }

                @Override
                public void postExecuteFailure(String commandId, ExecutionException exception)
                {
                }

                @Override
                public void postExecuteSuccess(String commandId, Object returnValue)
                {
                    if("org.eclipse.ui.file.refresh".equals(commandId))
                    {
                        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
                        {
                            public void run()
                            {
                                Configuration.verifyIceHome(Configuration.getIceHome());
                            }
                        });
                    }
                }

                @Override
                public void preExecute(String commandId, ExecutionEvent event)
                {
                }
            });
        }

        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                Configuration.verifyIceHome(Configuration.getIceHome());
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context)
        throws Exception
    {
        _plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return _plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    // The shared instance
    private static Activator _plugin;
}
