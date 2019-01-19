//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin.decorator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.zeroc.icebuilderplugin.internal.Configuration;

public class GeneratedDecorator implements ILabelDecorator
{

    public void addListener(ILabelProviderListener arg0)
    {
    }

    public void dispose()
    {
    }

    public boolean isLabelProperty(Object arg0, String arg1)
    {
        return false;
    }

    public void removeListener(ILabelProviderListener arg0)
    {
    }

    public Image decorateImage(Image arg0, Object arg1)
    {
        return null;
    }

    public String decorateText(String label, Object object)
    {
        IResource resource = (IResource) object;
        if(resource.getType() != IResource.FOLDER)
        {
          // Only folders are decorated.
          return null;
        }
        IProject project = resource.getProject();
        Configuration configuration = Configuration.getConfiguration(project);
        IFolder generated = project.getFolder(configuration.getGeneratedDir());
        if(!generated.getLocation().toOSString().equals(resource.getLocation().toOSString()))
        {
            // We just need to decorate the slice2java generated folder.
            return null;
        }

        return label + " [Generated slice2java sources]"; 
    }

}
