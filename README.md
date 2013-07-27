forge-android-plugin
====================

---------------------

<b>Forge plugin for Android Development</b>

<br>

TODO
====

<table Border="1" cellpadding="2" cellspacing="2" style="text-align:center" width="100%">
	<tr>
		<td style="text-align:center"><b>S NO</font></b></td>
	  	<td style="text-align:center"><b>Work</b></td>
	  	<td style="text-align:center"><b>Assigned to</b></td>
		<td style="text-align:center"><b>Status</b></td>
	</tr>


	<tr>
		<td style="text-align:left"><b>1</b></td>
		<td style="text-align:left"><b>Check ANDROID_HOME on setup</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">In Progress</td>
	</tr>

	<tr>
		<td style="text-align:left"><b>2</b></td>
		<td style="text-align:left"><b>Use freemaker</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">Completed</td>
	</tr>

	<tr>
		<td style="text-align:left"><b>3</b></td>
		<td style="text-align:left"><b>Create resource files</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">Completed</td>
	</tr>

		<tr>
		<td style="text-align:left"><b>4</b></td>
		<td style="text-align:left"><b>Overriding exisiting files</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">In Progress</img></td>
	</tr>

	<tr>
		<td style="text-align:left"><b>5</b></td>
		<td style="text-align:left"><b>Emulator version change</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">Yet to start</td>
	</tr>

	<tr>
		<td style="text-align:left"><b>6</b></td>
		<td style="text-align:left"><b>Property file key and value pair add</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">Yet to start</img></td>
	</tr>

	<tr>
		<td style="text-align:left"><b>7</b></td>
		<td style="text-align:left"><b>Scaffolding</b></td>
		<td style="text-align:left"><b>Kaleeswaran</b></td>
		<td style="text-align:center">Yet to start</td>
	</tr>
</table>


Bug tracker
===========

Have a bug? Rise an issue here on GitHub!
https://github.com/

Forum
=====

For more detailed discussions visit us at 



Mailing List
============

Have a query? Feel free to contact us.
rajmahendra@gmail.com
kaleeswaran14@gmail.com

Contributing
============



Copyright and license
=====================


REFERENCES
==========

http://books.sonatype.com/mvnref-book/reference/android-dev.html


Trouble Shooting
================

[Revision 17 of the Platform tools has split the build tools into a Build-tools package](https://code.google.com/p/maven-android-plugin/issues/detail?id=377&sort=-id&colspec=ID%20Type%20Component%20OpSys%20Status%20Priority%20Milestone%20Owner%20Summary)

[Android Maven Could not find tool 'aapt'](http://stackoverflow.com/questions/16619143/android-maven-could-not-find-tool-aapt)

Help
====
http://deepintojee.wordpress.com/2012/05/09/android-from-scratch-the-basics/
http://deepintojee.wordpress.com/2012/05/27/android-from-scratch-part-2-use-android-maven-plugin/
https://github.com/akquinet/android-archetypes
http://maven-android-plugin-m2site.googlecode.com/svn/run-mojo.html
http://www.vogella.com/articles/AndroidBuildMaven/article.html


mvn3 clean install
mvn3 android:deploy  - (Device id specification)

<plugin>
                <groupId>com.jayway.maven.plugins.android.generation2</groupId>
                <artifactId>android-maven-plugin</artifactId>
                <configuration>
                    <run>
                        <debug>true</debug>
                    </run>
                    <sdk>
                        <platform>4</platform>
                    </sdk>
                    <emulator>
                        <avd>16</avd>
                    </emulator>
                    <undeployBeforeDeploy>true</undeployBeforeDeploy>
                </configuration>
            </plugin>

mvn clean install -Dandroid.device="emulator" -Dandroid.emulator.avd=default -f pom.xml

http://developer.android.com/tools/devices/managing-avds-cmdline.html
http://maven-android-plugin-m2site.googlecode.com/svn/plugin-info.html


