//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.zeroc.icebuilderplugin.internal.Configuration;

public class Slice2JavaNature implements IProjectNature
{
    /**
     * ID of this project nature
     */
    public static final String NATURE_ID = "com.zeroc.IceBuilderPlugin.Slice2JavaNature";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#configure()
     */
    public void configure()
        throws CoreException
    {
        IProjectDescription desc = _project.getDescription();
        ICommand[] commands = desc.getBuildSpec();

        for(int i = 0; i < commands.length; ++i)
        {
            if(commands[i].getBuilderName().equals(Slice2JavaBuilder.BUILDER_ID))
            {
                return;
            }
        }

        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 1, commands.length);
        ICommand command = desc.newCommand();
        command.setBuilderName(Slice2JavaBuilder.BUILDER_ID);
        newCommands[0] = command;
        desc.setBuildSpec(newCommands);
        
        _project.setDescription(desc, null);
        
        // Initialize a new configuration for this project.
        Configuration.getConfiguration(_project).initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure()
        throws CoreException
    {
        IProjectDescription description = getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        for(int i = 0; i < commands.length; ++i)
        {
            if(commands[i].getBuilderName().equals(Slice2JavaBuilder.BUILDER_ID))
            {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                return;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#getProject()
     */
    public IProject getProject()
    {
        return _project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
     * .resources.IProject)
     */
    public void setProject(IProject project)
    {
        _project = project;
    }


    private IProject _project;
}
