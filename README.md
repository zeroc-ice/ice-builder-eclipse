# Ice Builder for Eclipse

The Ice Builder for Eclipse provides an eclipse Slice2Java plug-in which manages compilation of [Slice](https://doc.zeroc.com/display/Ice/The+Slice+Language) files using the Slice-to-Java compiler.

# Build Instructions

In order to build the plug-in, you will need an installation of Eclipse Helios
(3.6) or Indigo (3.7) suitable for plug-in development.

It will be necessary to install the Eclipse Plug-in Development Environment
if it is not already installed. Go to Help > Eclipse Marketplace and search
for PDE, then click install to install the Eclipse PDE.

To import the project choose File > Import > General > Existing Projects into Workspace
and select the java directory in this repository as the root directory.

To create the plugin use File > Export > Plug-in Development > Deployable plug-ins and fragments.

# Usage

## Configuring the Plug-in

Choose _Window -> Preferences_, select _Slice2Java_, and review the default setting for the location of your Ice installation. The property pane will display an error message if the plug-in considers the specified location to be invalid. If necessary, click _Browse..._ to pick the top-level directory of your Ice installation and apply your changes.

The plug-in automatically configures a workspace classpath variable named _ICE_JAR_HOME_ that refers to the subdirectory containing the Ice JAR files. This variable is primarily intended for use in Android projects.

## Activating the Plug-in for a Project

You can activate the plug-in for your project by right-clicking on the project, choosing _Slice2Java_ and clicking _Add Slice2Java builder_. The plug-in immediately makes several additions to your project:
* Creates a _slice_ subdirectory to contain your Slice files. The plug-in automatically compiles any Slice file that you add to this directory.
* Creates a _generated_ subdirectory to hold the Java source files that the slice2java translator generates from your Slice files.
* Adds a library reference to the Ice run time JAR file (_Ice.jar_). The plug-in assumes that the JAR file resides in the _lib_ subdirectory of your Ice installation.

## Configuring Project Settings

To configure the project-specific settings, select _Properties_ from the _Project_ menu or right-click on the name of your project and choose _Properties_. Click on _Slice2Java Properties_ to view the plug-in's configuration settings, which are presented in two tabs: Source and Options.

### Settings in the Source Tab

This tab configures the directories of your Slice files and generated code. The plug-in includes the _slice_ subdirectory by default, but you can remove this directory and add other directories if necessary. The plug-in only compiles Slice files that are located in the configured subdirectories.

For the generated code, the plug-in uses the default name _generated_ for the subdirectory. If you want to store your generated code in a different directory, you must first create the directory and then click _Browse_ to select it. The new directory must be empty otherwise the plug-in will reject your change. The plug-in also requires exclusive use of this directory, therefore you must not place other project resources in it.

### Settings in the Options Tab

This tab is where you configure additional plug-in settings. You can enter a list of include directories corresponding to the compiler's -I option. You can also specify preprocessor macros and metadata definitions in the fields provided. Finally, checkboxes offer additional control over certain features of the plug-in and the Slice compiler. When enabled, the checkboxes have the following semantics:
* __Enable streaming__ generates code to support the dynamic streaming API
* __Enable tie__ generates TIE classes
* __Enable ice__ instructs the compiler to accept Slice symbols that use the ice prefix
* __Enable console__ causes the plug-in to emit diagnostic information about its activities to Eclipse's console
* __Enable underscore__ determines whether underscores are permitted in Slice identifiers (this feature is only supported in Ice 3.4.1 or later)

Options are also provided for adding reference to libraries for the various Ice services, such as IceGrid and Glacier2.

## Configuring File Settings

The project settings described above serve as the default compiler settings for all Slice files in the project. You may also override the compiler settings on a per-file basis by selecting a Slice file in _Package Explorer_ and choosing _Properties_ from the _File_ menu, or by right-clicking on the file and choosing _Properties_. Select _Slice2Java Properties_ to configure the Slice compiler settings, which have the same semantics as those in the _Options_ tab described earlier.
