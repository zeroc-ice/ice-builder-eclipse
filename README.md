# Ice Builder for Eclipse

The Ice Builder for Eclipse provides a plug-in which automates the compilation of [Slice](https://doc.zeroc.com/display/Ice/The+Slice+Language) files using the [Slice-to-Java](https://doc.zeroc.com/display/Ice/slice2java+Command-Line+Options) compiler and manages the resulting generated code.

The Ice Builder for Eclipse plug-in provides the following features:

  - Handles all aspects of translating your Slice files
  - Incrementally recompiles Slice files after modifications
  - Maintains dependencies between Slice files
  - Highlights compilation errors in your source code
  - Manages the generated code to remove obsolete files automatically

## Contents

- [Build Instructions](#build-instructions)
- [Usage](#usage)
  - [Installing the Plug-in](#installing-the-plug-in)
  - [Configuring the Plug-in](#configuring-the-plug-in)
  - [Activating the Plug-in for a Project](#activating-the-plug-in-for-a-project)
  - [Configuring Project Settings](#configuring-project-settings)
    - [Settings in the Source Tab](#settings-in-the-source-tab)
    - [Settings in the Options Tab](#settings-in-the-options-tab)
  - [Configuring File Settings](#configuring-file-settings)
- [When does the Plug-In Recompile Slice Files?](#when-does-the-plug-in-recompile-slice-files)

## Build Instructions

In order to build the plug-in, you will need an installation of Eclipse suitable for plug-in development.

It will be necessary to install the Eclipse Plug-in Development Environment
if it is not already installed. Go to `Help > Eclipse Marketplace` and search
for PDE, then click install to install the Eclipse PDE.

To import the project choose `File > Import > General > Existing Projects into Workspace`
and select the java directory in this repository as the root directory.

To create the plug-in, use `File > Export > Plug-in Development > Deployable plug-ins and fragments`.

## Usage

### Installing the Plug-in

ZeroC hosts an Eclipse plug-in site that you can add to your Eclipse configuration. Follow these steps to install the `Ice Builder` plug-in:

  1. From the `Help` menu, choose `Install New Software`
  2. Click the `Add` button
  3. Enter a name in the `Name` field, such as `ZeroC`
  4. In the `Location` field, enter `http://zeroc.com/download/eclipse`
  5. Click `OK`
  6. Select the `Ice Builder` plug-in and click `Next`
  7. Click `Next` again
  8. If you agree to the license terms, check the box and click `Finish`
  9. Click `OK` if you are warned about unsigned content

Note: If you are upgrading from the old `Slice2Java` plug-in, you should uninstall the old plug-in before installing the `Ice Builder` plug-in. After installation it will be necessary to manually reconfigure the `Ice Builder` settings. The old `Slice2java` settings are not automatically migrated.

### Configuring the Plug-in

Choose `Window -> Preferences`, select `Ice Builder`, and review the default setting for the location of your Ice installation. The property pane will display an error message if the plug-in considers the specified location to be invalid. If necessary, click `Browse...` to pick the top-level directory of your Ice installation and apply your changes.

The plug-in automatically configures a workspace classpath variable named `ICE_JAR_HOME` that refers to the subdirectory containing the Ice JAR files. This variable is primarily intended for use in Android projects.

### Activating the Plug-in for a Project

You can activate the plug-in for your project by right-clicking on the project, choosing `Ice Builder` and clicking `Add Ice Builder`. The plug-in immediately makes several additions to your project:
* Creates a `slice` subdirectory to contain your Slice files. The plug-in automatically compiles any Slice file that you add to this directory.
* Creates a `generated` subdirectory to hold the Java source files that the slice2java translator generates from your Slice files.
* Adds a library reference to the Ice runtime JAR file (`Ice.jar`). The plug-in assumes that the JAR file resides in the `lib` subdirectory of your Ice installation.

### Configuring Project Settings

To configure the project-specific settings, select `Properties` from the `Project` menu or right-click on the name of your project and choose `Properties`. Click on `Ice Builder` to view the plug-in's configuration settings, which are presented in two tabs: Source and Options.

#### Settings in the Source Tab

This tab configures the directories of your Slice files and generated code. The plug-in includes the `slice` subdirectory by default, but you can remove this directory and add other directories if necessary. The plug-in only compiles Slice files that are located in the configured subdirectories.

For the generated code, the plug-in uses the default name `generated` for the subdirectory. If you want to store your generated code in a different directory, you must first create the directory and then click `Browse` to select it. The new directory must be empty; otherwise the plug-in will reject your change. The plug-in also requires exclusive use of this directory; therefore, you must not place other project resources in it.

#### Settings in the Options Tab

This tab is where you configure additional plug-in settings. You can enter a list of include directories corresponding to the compiler's -I option. You can also specify preprocessor macros and metadata definitions in the fields provided. Finally, checkboxes offer additional control over certain features of the plug-in and the Slice compiler. When enabled, the checkboxes have the following semantics:
* __Enable streaming__ generates code to support the dynamic streaming API
* __Enable tie__ generates TIE classes
* __Enable ice__ instructs the compiler to accept Slice symbols that use the ice prefix
* __Enable console__ causes the plug-in to emit diagnostic information about its activities to Eclipse's console
* __Enable underscore__ determines whether underscores are permitted in Slice identifiers (this feature is only supported in Ice 3.4.1 or later)

Options are also provided for adding reference to libraries for the various Ice services, such as IceGrid and Glacier2.

### Configuring File Settings

The project settings described above serve as the default compiler settings for all Slice files in the project. You may also override the compiler settings on a per-file basis by selecting a Slice file in `Package Explorer` and choosing `Properties` from the `File` menu, or by right-clicking on the file and choosing `Properties`. Select `Ice Builder` to configure the Slice compiler settings, which have the same semantics as those in the `Options` tab described earlier.

## When does the Plug-in Recompile Slice Files?

Slice files will be recompiled if either of the following are true:
 * This Slice file or another Slice file in the project included directly or indirectly by this Slice file was updated after the last compilation of the Slice file through the plug-in.
 * The options used to compile this Slice file have changed.

Removing a Slice file from a project will trigger the removal of the corresponding generated `.java` files.
