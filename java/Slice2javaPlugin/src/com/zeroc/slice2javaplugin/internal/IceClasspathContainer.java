// **********************************************************************
//
// Copyright (c) 2008-2015 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.slice2javaplugin.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

public class IceClasspathContainer implements IClasspathContainer
{
    private IClasspathEntry[] _cpEntry;
    private IPath _path;

    IceClasspathContainer(IClasspathEntry[] entries, IPath path)
    {
        _cpEntry = entries;
        _path = path;
    }

    public IClasspathEntry[] getClasspathEntries()
    {
        return _cpEntry;
    }

    public String getDescription()
    {
        return "Ice Library";
    }

    public int getKind()
    {
        return IClasspathContainer.K_APPLICATION;
    }

    public IPath getPath()
    {
        return _path;
    }
}
