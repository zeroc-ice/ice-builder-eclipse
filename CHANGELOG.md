### Changes for Ice Builder for Eclipse

This file describes the change history for the Ice Builder for Eclipse
plug-in.

## Changes since version 4.1.0.20170816
- Fixed a bug where ICE_HOME was incorrectly validated on Linux platforms

## Changes since version 4.0.0.20150721

- Removed intrusive error popups, now all errors are printed directly
  to the console.

- Added support for parsing compiler output in Ice 3.7.0

- Added a preference to control whether or not Slice files should
  be automatically compiled by eclipse as changes are saved.

- Added new problem markers specifically for Ice related errors.

- Now all Slice files within a project are compiled, instead of only
  those inside specially designated Slice source folders.

- Added a button in the "Ice Builder" menu for manually compiling
  Slice files.

- Removed the ability to set compiler options for individual Slice
  files.

- Removed checkboxes for automatically including Ice jar files, as
  they are no longer shipped with Ice installations.

- Simplified the project properties UI.

- Fixed a bug where the Ice Builder could be added to non-java
  Eclipse projects.

- Fixed a bug where errors in Slice files weren't highlighted.

- Fixed a bug where incomplete checksum maps could be generated.

## Changes since version 3.5.1.20131004

- Renamed the Slice2Java Plugin to Ice Builder for Eclipse. The
  plug-in is no longer tied to a particular version of Ice.

- Support for jar name format changes introduced in Ice 3.6.0

- Fixed a bug where setting stream on Slice file would set it for
  the entire project instead.

## Changes since version 3.5.0.20130308

- Fixed a bug in the code that locates the Ice JAR files via the
  ICE_JAR_HOME classpath variable.

## Changes since version 3.5.0.20121212

- Fixed the plug-in to report better errors when the Slice translator
  fails to run.

- Renamed ICE_HOME classpath variable as ICE_JAR_HOME. The new
  variable points to the directory where Ice JAR files are installed.
  It allows the project to refer to Ice.jar symbolically as
  "ICE_JAR_HOME/Ice.jar".

## Changes since version 3.4.2.20111017

- Support for Ice 3.5b.

## Changes since version 3.4.1.20110201

- The plug-in now allows you to set file-specific options.

- For Android projects, Ice Library is now automatically configured.

- Added an Extra Arguments field that allows you to pass extra
  arguments to the Slice compiler.

- Added decorators to Slice and Slice2java-generated folders to make
  it easy to identify them as such.

- Removed the tab to configure library. It is now an extra section
  in the options tab.

## Changes since version 3.3.1.20100706

- For Android projects, the plug-in no longer creates the Ice Library
  and also disables the Libraries tab in the Slice2Java properties.
  For non-Android projects, enabling the Slice2Java builder
  automatically configures the Ice Library as before.

- The plug-in now creates the workspace classpath variable ICE_HOME,
  whose value always reflects the Ice installation directory that
  you set in Eclipse preferences. This variable is primarily intended
  for use in Android projects, as it allows the project to refer to
  Ice.jar symbolically as "ICE_HOME/lib/Ice.jar". As with earlier
  versions of the plug-in, Android users still have to add this
  reference manually as an "external JAR file" in their projects.

## Changes since version 3.3.1.20100304

- Added support for the new translator option --underscore that was
  introduced in Ice 3.4.1.

- Fixed a bug in which a Slice file was not recompiled when one of its
  dependencies changed.

## Changes since version 3.3.1.20091005

- Added a check box to the Libraries tab in the project properties.
  This setting determines whether the plug-in adds the "Ice Library"
  entry to the project's build path.

- Fixed a bug that would cause a build failure in a project that was
  configured to use a nested subdirectory for the generated files.

## Changes since version 3.3.1.20090330

- Added a content type definition for Slice files. Previously, double-
  clicking on a Slice file in the Package Explorer would cause Eclipse
  to open the file using the system's default editor for *.ice files
  (if one was defined). With this change, Eclipse opens a Slice file
  using its internal text editor by default.

## Changes since version 0.1.1

- Changed version numbering scheme to reflect the minimum required Ice
  version, therefore the version for this release is 3.3.1.

- Fixed the plug-in so that Ice.jar is automatically added to the
  class path when executing an application.

- Fixed the path names of default Ice installation directories.

## Changes since version 0.1.0

- Improved the Slice-to-Java translation process through tighter
  integration with the compiler. The main benefit is that errors in
  Slice files are now reported more accurately.

- Fixed the plug-in to give its builder higher precedence than the
  Java builder. This corrects several issues, such as Eclipse not
  recompiling the generated code immediately after a change to a Slice
  file, and errors resulting from Eclipse compiling application code
  before Slice translation has occurred.
