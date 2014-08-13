/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * If applicable, add the following below this MPL 2.0 HEADER, replacing
 * the fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *     Portions Copyright [yyyy] [name of copyright owner]
 *
 *     Copyright 2012-2014 ForgeRock AS
 *
 */

package org.forgerock.doc.maven.post;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.forgerock.doc.maven.AbstractDocbkxMojo;
import org.forgerock.doc.maven.utils.HtmlUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * HTML post-processor for both single-page and chunked HTML formats.
 */
public class Html {

    /**
     * The Mojo that holds configuration and related methods.
     */
    private AbstractDocbkxMojo m;

    /**
     * Constructor setting the Mojo that holds the configuration.
     *
     * @param mojo The Mojo that holds the configuration.
     */
    public Html(final AbstractDocbkxMojo mojo) {
        m = mojo;

        outputDirectories = new String[2];
        outputDirectories[0] = "";
        outputDirectories[1] = File.separator + FilenameUtils.getBaseName(m.getDocumentSrcName());
    }

    /**
     * Post-processes HTML formats.
     *
     * @throws MojoExecutionException Failed to post-process HTML.
     */
    public void execute() throws MojoExecutionException {
        addScript();
        addShScripts();
        addShCss();
        editBuiltHtml(m.getDocbkxOutputDirectory().getPath() + File.separator + "html");
    }

    /**
     * Directories where scripts and CSS are to be added.
     */
    private String[] outputDirectories;

    /**
     * Add JavaScript to include in HTML in each document source directory.
     * See <a href="http://docbook.sourceforge.net/release/xsl/current/doc/html/html.script.html"
     * >html.script</a> for details.
     *
     * @throws MojoExecutionException Failed to add script.
     */
    void addScript() throws MojoExecutionException {

        final URL scriptUrl = getClass().getResource("/js/" + m.getJavaScriptFileName());
        String scriptString;
        try {
            scriptString = IOUtils.toString(scriptUrl);
        } catch (IOException ie) {
            throw new MojoExecutionException("Failed to read " + scriptUrl, ie);
        }

        if (scriptString != null) {
            scriptString = scriptString.replace("PROJECT_NAME", m.getProjectName().toLowerCase());
            scriptString = scriptString.replace("PROJECT_VERSION", m.getProjectVersion());
            scriptString = scriptString.replace("LATEST_JSON", m.getLatestJson());
            scriptString = scriptString.replace("DOCS_SITE", m.getDocsSite());
        } else {
            throw new MojoExecutionException(scriptUrl + " was empty");
        }

        // The html.script parameter should probably take URLs.
        // When local files are referenced,
        // the DocBook XSL stylesheets do not copy the .js files.
        // Instead the files must be copied to the output directories.

        for (final String outputDirectory : outputDirectories) {

            for (final String docName : m.getDocNames()) {

                final File parent = new File(m.getDocbkxOutputDirectory(),
                        "html" + File.separator + docName + outputDirectory);
                final File scriptFile = new File(parent, m.getJavaScriptFileName());

                try {
                    FileUtils.writeStringToFile(scriptFile, scriptString, "UTF-8");
                } catch (IOException ie) {
                    throw new MojoExecutionException(
                            "Failed to write to " + scriptFile.getPath(), ie);
                }
            }
        }
    }

    /**
     * SyntaxHighlighter JavaScript files.
     */
    private final String[] shJavaScriptFiles = {
        "shCore.js",
        "shBrushAci.js",
        "shBrushBash.js",
        "shBrushCsv.js",
        "shBrushHttp.js",
        "shBrushJava.js",
        "shBrushJScript.js",
        "shBrushLDIF.js",
        "shBrushPlain.js",
        "shBrushProperties.js",
        "shBrushXml.js"
    };

    /**
     * Add SyntaxHighlighter JavaScript files in each HTML document source directory.
     *
     * @throws MojoExecutionException Failed to add scripts.
     */
    void addShScripts() throws MojoExecutionException {

        for (String scriptName : shJavaScriptFiles) {
            URL scriptUrl = getClass().getResource("/js/" + scriptName);

            // The html.script parameter should probably take URLs.
            // When local files are referenced,
            // the DocBook XSL stylesheets do not copy the .js files.
            // Instead the files must be copied to the output directories.

            for (final String outputDirectory : outputDirectories) {

                for (final String docName : m.getDocNames()) {

                    final File parent = new File(m.getDocbkxOutputDirectory(),
                            "html" + File.separator + docName + outputDirectory
                                    + File.separator + "sh");
                    final File scriptFile = new File(parent, scriptName);

                    try {
                        FileUtils.copyURLToFile(scriptUrl, scriptFile);
                    } catch (IOException ie) {
                        throw new MojoExecutionException(
                                "Failed to write to " + scriptFile.getPath(), ie);
                    }
                }
            }
        }
    }

    /**
     * SyntaxHighlighter CSS files.
     */
    private final String[] shCssFiles =
    {"shCore.css", "shCoreEclipse.css", "shThemeEclipse.css"};

    /**
     * Add SyntaxHighlighter CSS files in each HTML document source directory.
     *
     * @throws MojoExecutionException Failed to add scripts.
     */
    void addShCss() throws MojoExecutionException {

        for (String styleSheetName : shCssFiles) {
            URL styleSheetUrl = getClass().getResource("/css/" + styleSheetName);

            // The html.stylesheet parameter should probably take URLs.
            // When local files are referenced,
            // the DocBook XSL stylesheets do not copy the .css files.
            // Instead the files must be copied to the output directories.

            for (final String outputDirectory : outputDirectories) {

                for (final String docName : m.getDocNames()) {

                    final File parent = new File(m.getDocbkxOutputDirectory(),
                            "html" + File.separator + docName + outputDirectory
                                    + File.separator + "sh");
                    final File styleSheetFile = new File(parent, styleSheetName);

                    try {
                        FileUtils.copyURLToFile(styleSheetUrl, styleSheetFile);
                    } catch (IOException ie) {
                        throw new MojoExecutionException(
                                "Failed to write to " + styleSheetFile.getPath(), ie);
                    }
                }
            }
        }
    }

    /**
     * Edit build single-page and chunked HTML.
     *
     * <p>
     *
     * The HTML built by docbkx-tools does not currently include the following,
     * which this method adds.
     *
     * <ul>
     * <li>A DOCTYPE declaration (needed by Internet Explorer to interpret CSS</li>
     * <li>JavaScript to to call the SyntaxHighlighter brushes</li>
     * <li>A favicon link</li>
     * <li>A paragraph about logging issues with a link to JIRA</li>
     * <li>JavaScript used by Google Analytics</li>
     * </ul>
     *
     * @param htmlDir Directory under which to find HTML output
     * @throws MojoExecutionException Something went wrong when updating HTML.
     */
    final void editBuiltHtml(final String htmlDir) throws MojoExecutionException {
        try {
            HashMap<String, String> replacements = new HashMap<String, String>();

            String doctype = IOUtils.toString(
                    getClass().getResourceAsStream("/starthtml-doctype.txt"), "UTF-8");
            replacements.put("<html>", doctype);

            String favicon = IOUtils.toString(
                    getClass().getResourceAsStream("/endhead-favicon.txt"), "UTF-8");
            favicon = favicon.replace("FAVICON-LINK", m.getFaviconLink());
            replacements.put("</head>", favicon);

            String linkToJira = getLinkToJira();

            String gascript = IOUtils.toString(
                    getClass().getResourceAsStream("/endbody-ga.txt"), "UTF-8");
            gascript = gascript.replace("ANALYTICS-ID", m.getGoogleAnalyticsId());
            replacements.put("</body>", linkToJira + "\n" + gascript);

            HtmlUtils.updateHtml(htmlDir, replacements);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Failed to update output HTML correctly: " + e.getMessage());
        }
    }

    /**
     * Return a &lt;p&gt; containing a link to log a bug in JIRA, depending on the project.
     * The string is not localized.
     *
     * @return &lt;p&gt; containing a link to log a bug in JIRA.
     */
    final String getLinkToJira() {
        String link = "<p>&nbsp;</p><div id=\"footer\"><p>Something wrong on this page? "
                + "<a href=\"JIRA-URL\">Log a documentation bug.</a></p></div>";

        // https://confluence.atlassian.com/display/JIRA/Creating+Issues+via+direct+HTML+links
        String jiraURL = "https://bugster.forgerock.org/jira/secure/CreateIssueDetails!init.jspa";

        if (m.getProjectName().equalsIgnoreCase("OpenAM")) {
            jiraURL += "?pid=10000&components=10007&issuetype=1";
        }
        if (m.getProjectName().equalsIgnoreCase("OpenDJ")) {
            jiraURL += "?pid=10040&components=10132&issuetype=1";
        }
        if (m.getProjectName().equalsIgnoreCase("OpenICF")) {
            jiraURL += "?pid=10041&components=10170&issuetype=1";
        }
        if (m.getProjectName().equalsIgnoreCase("OpenIDM")) {
            jiraURL += "?pid=10020&components=10164&issuetype=1";
        }
        if (m.getProjectName().equalsIgnoreCase("OpenIG")) {
            jiraURL += "?pid=10060&components=10220&issuetype=1";
        }
        if (m.getProjectName().equalsIgnoreCase("ForgeRock")) { // Just testing
            jiraURL += "?pid=10010&issuetype=1";
        }

        if (!jiraURL.contains("pid")) {
            link = "";
        } else {
            link = link.replaceFirst("JIRA-URL", jiraURL);
        }
        return link;
    }
}