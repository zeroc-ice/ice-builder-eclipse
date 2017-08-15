// **********************************************************************
//
// Copyright (c) 2008-2017 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.preferences;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.zeroc.icebuilderplugin.Activator;
import com.zeroc.icebuilderplugin.util.StreamReader;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    private String getDefaultHome()
    {
        String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        if(os.startsWith("windows")) //$NON-NLS-1$
        {
            return getWindowsIceHome();
        }
        if(os.startsWith("mac")) //$NON-NLS-1$
        {
            return "/usr/local";
        }
        if((os.contains("nix")) || (os.contains("nux"))) //$NON-NLS-1$
        {
            return "/usr";
        }
        return ""; //$NON-NLS-1$
    }

    //
    // Query the registry and return the path of the latest Ice version available.
    //
    private String getWindowsIceHome()
    {
        StringBuffer out = new StringBuffer();
        StringBuffer err = new StringBuffer();

        try
        {
            Process proc = new ProcessBuilder("reg", "query", "HKLM\\Software\\ZeroC").start();

            StreamReader outThread = new StreamReader(proc.getInputStream(), out);
            outThread.start();
            StreamReader errThread = new StreamReader(proc.getErrorStream(), err);
            errThread.start();

            int status = proc.waitFor();

            outThread.join();
            errThread.join();

            if(status != 0)
            {
                return "";
            }

            String[] installations = out.toString().split("\\r?\\n");
            Arrays.sort(installations, new VersionComparator());

            for(String entry : installations)
            {
                if(entry.indexOf("HKEY_LOCAL_MACHINE\\Software\\ZeroC\\Ice") != -1)
                {
                    String installDir = getWindowsInstallDir(entry);
                    if(installDir != null)
                    {
                        return ((installDir.endsWith("\\"))? (installDir.substring(0, installDir.length() - 1)) : installDir);
                    }
                }
            }
        } catch(Exception e)
        {
            // Ignored
        }

        return "";
    }

    private String getWindowsInstallDir(String key)
    {
        StringBuffer out = new StringBuffer();
        StringBuffer err = new StringBuffer();
        try
        {
            Process proc = new ProcessBuilder("reg", "query", key, "/v", "InstallDir").start();

            StreamReader outThread = new StreamReader(proc.getInputStream(), out);
            outThread.start();
            StreamReader errThread = new StreamReader(proc.getErrorStream(), err);
            errThread.start();

            int status = proc.waitFor();

            outThread.join();
            errThread.join();

            if(status != 0)
            {
                return null;
            }

            return out.toString().split("    ")[3].trim();
        }
        catch(Exception e)
        {
            // Ignored
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        
        store.setDefault(PluginPreferencePage.ICE_HOME, getDefaultHome());
        store.setDefault(PluginPreferencePage.BUILD_AUTO, true);
    }

    private static final class VersionComparator implements Comparator<String>
    {
        public int compare(String s1, String s2)
        {
            String[] version1 = s1.replaceAll("[^.0-9]", "").split("\\.");
            String[] version2 = s2.replaceAll("[^.0-9]", "").split("\\.");

            for(int i = 0; i < Math.min(version1.length, version2.length); i++)
            {
                int i1 = ((version1[i].length() > 0)? Integer.parseInt(version1[i]) : Integer.MIN_VALUE);
                int i2 = ((version2[i].length() > 0)? Integer.parseInt(version2[i]) : Integer.MIN_VALUE);
                if(i1 > i2)
                {
                    return -1;
                }
                if(i1 < i2)
                {
                    return 1;
                }
            }

            return (int)Math.signum(version2.length - version1.length);
        }
    }
}
