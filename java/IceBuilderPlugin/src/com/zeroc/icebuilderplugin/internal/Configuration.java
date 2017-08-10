// **********************************************************************
//
// Copyright (c) 2008-2017 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.icebuilderplugin.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.zeroc.icebuilderplugin.Activator;
import com.zeroc.icebuilderplugin.preferences.PluginPreferencePage;

public class Configuration
{
    public Configuration(IProject project)
    {
        _project = project;

        _instanceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID + "." + _project.getName());

        _store = new ScopedPreferenceStore(new ProjectScope(project), Activator.PLUGIN_ID);

        // If the project contains old properties
        if(_store.getString(VERSION_KEY) != "4.1")
        {
            updateProperties();
        }

        _store.setDefault(GENERATED_KEY, GENERATED_KEY);
        _store.setDefault(INCLUDES_KEY, "");
        _store.setDefault(EXTRA_ARGUMENTS_KEY, "");
    }

    private void updateProperties()
    {
        String extraArguments = _store.getString(EXTRA_ARGUMENTS_KEY);

        if(_store.getBoolean("ice"))
        {
            extraArguments += " --ice";
        }
        if(_store.getBoolean("tie"))
        {
            extraArguments += " --tie";
        }
        if(_store.getBoolean("underscore"))
        {
            extraArguments += " --underscore";
        }

        if((extraArguments.contains("--tie") || extraArguments.contains("--impl-tie"))
                && !extraArguments.contains("--compat"))
        {
            extraArguments += " --compat";
        }

        String defines = _store.getString("defines");
        if(defines.length() > 0)
        {
            extraArguments += " ";
            for(Iterator<String> p = toList(defines).iterator(); p.hasNext();)
            {
                extraArguments += "-D" + p.next();
                if(p.hasNext())
                {
                    extraArguments += " ";
                }
            }
        }

        String meta = _store.getString("meta");
        if(meta.length() > 0)
        {
            extraArguments += " --meta";
            for(Iterator<String> p = toList(meta).iterator(); p.hasNext();)
            {
                extraArguments += " " + p.next();
            }
        }

        setValue("jars", "");
        setValue("sliceSourceDirs", "");
        setValue("console", "");
        setValue("meta", "");
        setValue("stream", "");
        setValue("iceIncludes", "");
        setValue("ice", "");
        setValue("tie", "");
        setValue("defines", "");
        setValue("addJars", "");
        setValue("underscore", "");

        setValue(EXTRA_ARGUMENTS_KEY, extraArguments);
        setValue(VERSION_KEY, "4.1");

        try
        {
            write();
        }
        catch(IOException e) {}
        catch(CoreException e) {}
    }

    /**
     * Turns list of strings into a single ';' delimited string. ';' in the
     * string values are escaped with a leading '\'. '\' are turned into '\\'.
     *
     * @param l
     *            List of strings.
     * @return Semicolon delimited string.
     */
    static public String fromList(List<String> l)
    {
        StringBuffer sb = new StringBuffer();
        for(Iterator<String> p = l.iterator(); p.hasNext();)
        {
            if(sb.length() > 0)
            {
                sb.append(";");
            }
            sb.append(escape(p.next()));
        }
        return sb.toString();
    }

    /**
     * Turn a semicolon delimited string into a list of strings. Escaped values
     * are preserved (characters prefixed with a '\').
     *
     * @param s
     *            Semicolon delimited string.
     * @return List of strings.
     */
    static public List<String> toList(String s)
    {
        java.util.List<String> l = new ArrayList<String>();
        int curr = 0;
        int end = s.length();
        boolean escape = false;
        StringBuffer sb = new StringBuffer();
        for(curr = 0; curr < end; ++curr)
        {
            char ch = s.charAt(curr);
            if(escape)
            {
                sb.append(ch);
                escape = false;
            }
            else
            {
                if(ch == ';')
                {
                    String tok = sb.toString().trim();
                    sb.setLength(0);
                    if(tok.length() > 0)
                    {
                        l.add(tok);
                    }
                }
                else if(ch == '\\')
                {
                    escape = true;
                }
                else
                {
                    sb.append(ch);
                }
            }
        }
        String tok = sb.toString().trim();
        if(tok.length() > 0)
        {
            l.add(tok);
        }
        return l;
    }

    public boolean write()
        throws CoreException, IOException
    {
        boolean rc = false;
        if(_store.needsSaving())
        {
            _store.save();
            rc = true;
        }
        if(_instanceStore.needsSaving())
        {
            _instanceStore.save();
            rc = true;
        }

        return rc;
    }

    public void initialize()
        throws CoreException
    {

        // Create the generated directory, if necessary.
        IFolder generated = _project.getFolder(getGeneratedDir());
        if(!generated.exists())
        {
            generated.create(false, true, null);
        }

        fixGeneratedCP(null, getGeneratedDir());

        if(!verifyIceHome(getIceHome()))
        {
            PluginPreferencePage.addIceHomeWarnings();
        }
        else
        {
            PluginPreferencePage.removeIceHomeWarnings();
        }
    }

    public void deinstall()
        throws CoreException
    {
        removedGeneratedCP();
        IFolder generatedFolder = _project.getFolder(getGeneratedDir());
        if(generatedFolder != null && generatedFolder.exists())
        {
            generatedFolder.delete(true, null);
        }

        PluginPreferencePage.removeIceHomeWarnings();
    }

    public String getGeneratedDir()
    {
        return _store.getString(GENERATED_KEY);
    }

    public void fixGeneratedCP(String oldG, String newG)
            throws CoreException
    {
        IJavaProject javaProject = JavaCore.create(_project);

        IFolder newGenerated = _project.getFolder(newG);

        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry newEntry = JavaCore.newSourceEntry(newGenerated.getFullPath());

        if(oldG != null)
        {
            IFolder oldGenerated = _project.getFolder(oldG);
            IClasspathEntry oldEntry = JavaCore.newSourceEntry(oldGenerated.getFullPath());
            for(int i = 0; i < entries.length; ++i)
            {
                if(entries[i].equals(oldEntry))
                {
                    entries[i] = newEntry;
                    javaProject.setRawClasspath(entries, null);
                    oldGenerated.delete(true, null);
                    return;
                }
            }
        }

        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 1, entries.length);
        newEntries[0] = newEntry;
        
        newGenerated.setDerived(true, null);

        try
        {
            javaProject.setRawClasspath(newEntries, null);
        }
        catch(JavaModelException e)
        {
            // This can occur if a duplicate CLASSPATH entry is made.
            //
            // throw new CoreException(new Status(IStatus.ERROR,
            // Activator.PLUGIN_ID, e.toString(), null));
        }
    }
    
    public void removedGeneratedCP()
            throws CoreException
    {
        IJavaProject javaProject = JavaCore.create(_project);

        IFolder generated = _project.getFolder(getGeneratedDir());

        IClasspathEntry generatedEntry = JavaCore.newSourceEntry(generated.getFullPath());
        
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length - 1];

        for(int i = 0, j = 0; i < entries.length; i++)
        {
            if(entries[i].equals(generatedEntry))
            {
                continue;
            }
            newEntries[j] = entries[i];
            j++;
        }

        try
        {
            javaProject.setRawClasspath(newEntries, null);
        }
        catch(JavaModelException e)
        {
            // This can occur if a duplicate CLASSPATH entry is made.
            //
            // throw new CoreException(new Status(IStatus.ERROR,
            // Activator.PLUGIN_ID, e.toString(), null));
        }
    }

    public void setGeneratedDir(String generated)
        throws CoreException
    {
        String oldGenerated = getGeneratedDir();
        if(setValue(GENERATED_KEY, generated))
        {
            fixGeneratedCP(oldGenerated, generated);
        }
    }

    public List<String> getCommandLine()
    {
        List<String> cmds = new ArrayList<String>();
        for(Iterator<String> p = getIncludes().iterator(); p.hasNext();)
        {
            cmds.add("-I" + p.next());
        }
        
        StringTokenizer tokens = new StringTokenizer(getExtraArguments());
        while(tokens.hasMoreTokens())
        {  
            cmds.add(tokens.nextToken());  
        }

        return cmds;
    }

    public List<String> getIncludes()
    {
        List<String> s = toList(_store.getString(INCLUDES_KEY));
        
        String iceHome = getIceHome();
        String path = null;
        if(!System.getProperty("os.name").startsWith("Windows") && 
           (iceHome.equals("/usr") || iceHome.equals("/usr/local")))
        {
            String version = getIceVersion();
            if(version != null)
            {
                File f = new File(iceHome + "/share/Ice-" + version + "/slice");
                if(f.exists())
                {
                    path = f.toString();
                }
            }
        }
        if(path == null)
        {
            path = new File(iceHome + File.separator + "slice").toString();
        }
        s.add(path);
        return s;
    }

    // The bare include list.
    public List<String> getBareIncludes()
    {
        return toList(_store.getString(INCLUDES_KEY));
    }

    public void setIncludes(List<String> includes)
    {
        setValue(INCLUDES_KEY, fromList(includes));
    }
    
    public String getExtraArguments()
    {
        return _store.getString(EXTRA_ARGUMENTS_KEY);
    }

    public void setExtraArguments(String arguments)
    {
        setValue(EXTRA_ARGUMENTS_KEY, arguments);
    }

    public static void setupSharedLibraryPath(Map<String, String> env)
    {
        String iceHome = getIceHome();
        String os = System.getProperty("os.name");
        
        if(iceHome.equals("/usr") && os.equals("Linux"))
        {
            return;
        }
        
        String lib32Subdir = "lib";
        String lib64Subdir = "lib64";
        if(os.equals("Linux"))
        {
            if(new File("/usr/lib/i386-linux-gnu").exists())
            {
                lib32Subdir = "lib" + File.separator + "i386-linux-gnu";
            }
            
            if(new File("/usr/lib/x86_64-linux-gnu").exists())
            {
                lib64Subdir = "lib" + File.separator + "x86_64-linux-gnu";
            }
        }
        
        
        
        String libPath;
        boolean srcdist = false;
        if(new File(iceHome + File.separator + "cpp" + File.separator + "bin").exists())
        {
            // iceHome points at a source distribution.
            libPath = new File(iceHome + File.separator + "cpp" + File.separator + "lib").toString();
            srcdist = true;
        }
        else
        {
            libPath = new File(iceHome + File.separator + lib32Subdir).toString();
        }

        String ldLibPathEnv = null;
        String ldLib64PathEnv = null;
        String lib64Path = null;
        
        if(os.equals("Mac OS X"))
        {
            ldLibPathEnv = "DYLD_LIBRARY_PATH";
        }
        else if(os.equals("AIX"))
        {
            ldLibPathEnv = "LIBPATH";
        }
        else if(os.equals("HP-UX"))
        {
            ldLibPathEnv = "SHLIB_PATH";
            ldLib64PathEnv = "LD_LIBRARY_PATH";
            if(srcdist)
            {
                lib64Path = libPath;
            }
            else
            {
                lib64Path = new File(iceHome + File.separator + "lib" + File.separator + "pa20_64").toString();
            }
        }
        else if(os.startsWith("Windows"))
        {
            //
            // No need to change the PATH environment variable on Windows, the
            // DLLs should be found
            // in the translator local directory.
            //
            // ldLibPathEnv = "PATH";
        }
        else if(os.equals("SunOS"))
        {
            ldLibPathEnv = "LD_LIBRARY_PATH";
            ldLib64PathEnv = "LD_LIBRARY_PATH_64";
            String arch = System.getProperty("os.arch");
            if(srcdist)
            {
                lib64Path = libPath;
            }
            else if(arch.equals("x86"))
            {
                lib64Path = new File(iceHome + File.separator + "lib" + File.separator + "amd64").toString();
            }
            else
            // Sparc
            {
                lib64Path = new File(iceHome + File.separator + "lib" + File.separator + "sparcv9").toString();
            }
        }
        else
        {
            ldLibPathEnv = "LD_LIBRARY_PATH";
            ldLib64PathEnv = "LD_LIBRARY_PATH";
            if(srcdist)
            {
                lib64Path = libPath;
            }
            else
            {
                lib64Path = new File(iceHome + File.separator + lib64Subdir).toString();
            }
        }

        if(ldLibPathEnv != null)
        {
            if(ldLibPathEnv.equals(ldLib64PathEnv))
            {
                libPath = libPath + File.pathSeparator + lib64Path;
            }

            String envLibPath = env.get(ldLibPathEnv);
            if(envLibPath != null)
            {
                libPath = libPath + File.pathSeparator + envLibPath;
            }

            env.put(ldLibPathEnv, libPath);
        }

        if(ldLib64PathEnv != null && !ldLib64PathEnv.equals(ldLibPathEnv))
        {
            String envLib64Path = env.get(ldLib64PathEnv);
            if(envLib64Path != null)
            {
                lib64Path = lib64Path + File.pathSeparator + envLib64Path;
            }
            env.put(ldLib64PathEnv, lib64Path);
        }
    }

    public String getTranslator()
    {
        return getTranslatorForHome(getIceHome());
    }

    static public boolean verifyIceHome(String dir)
    {
        return getTranslatorForHome(dir) != null;
    }

    private static String getIceHome()
    {
        return Activator.getDefault().getPreferenceStore().getString(PluginPreferencePage.ICE_HOME);
    }

    // For some reason ScopedPreferenceStore.setValue(String, String)
    // doesn't check to see whether the stored value is the same as
    // the new value.
    private boolean setValue(String key, String value)
    {
        return setValue(_store, key, value);
    }

    private boolean setValue(ScopedPreferenceStore store, String key, String value)
    {
        if(!store.getString(key).equals(value))
        {
            store.setValue(key, value);
            return true;
        }
        return false;
    }

    static private String escape(String s)
    {
        int curr = 0;
        int end = s.length();
        StringBuffer sb = new StringBuffer();
        for(curr = 0; curr < end; ++curr)
        {
            char ch = s.charAt(curr);
            if(ch == '\\' || ch == ';')
            {
                sb.append('\\');
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    // Obtain the Ice version by executing the translator with the -v option.
    private String getIceVersion()
    {
        String iceHome = getIceHome();
        if(_version == null || !iceHome.equals(_iceHome))
        {
            _version = null;
            String exec = getTranslatorForHome(getIceHome());
            if(exec != null)
            {
                try
                {
                    ProcessBuilder b = new ProcessBuilder(exec, "-v");
                    b.redirectErrorStream(true);
                    Map<String, String> env = b.environment();
                    setupSharedLibraryPath(env);
                    Process p = b.start();
                    int status = p.waitFor();
                    if(status == 0)
                    {
                        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line = r.readLine();
                        _version = line.trim();
                        _iceHome = iceHome;
                    }
                }
                catch(Throwable ex)
                {
                    // Ignore.
                }
            }
        }
        return _version;
    }

    private static String getTranslatorForHome(String dir)
    {
        String suffix = "";
        String os = System.getProperty("os.name");
        if(os.startsWith("Windows"))
        {
            suffix = ".exe";
        }
        File f = new File(dir + File.separator + "bin" + File.separator + "slice2java" + suffix);
        if(f.exists())
        {
            return f.toString();
        }
        if(os.startsWith("Windows"))
        {
            f = new File(dir + File.separator + "bin" + File.separator +
                         "x64" + File.separator + "slice2java" + suffix);
            if(f.exists())
            {
                return f.toString();
            }
        }
        f = new File(dir + File.separator + "cpp" + File.separator + "bin" + File.separator + "slice2java" + suffix);
        if(f.exists())
        {
            return f.toString();
        }
        return null;
    }

    private static final String VERSION_KEY = "builderVersion";
    private static final String GENERATED_KEY = "generated";
    private static final String INCLUDES_KEY = "includes";
    private static final String EXTRA_ARGUMENTS_KEY = "extraArguments";

    // Preferences store for items which should go in SCM. This includes things
    // like build flags.
    private ScopedPreferenceStore _store;

    // Preferences store per project items which should not go in SCM, such as
    // the location of the Ice installation.
    private ScopedPreferenceStore _instanceStore;

    private IProject _project;

    private static String _version = null;
    private static String _iceHome = null;
}
