/*
 * Copyright (c) 2002, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 4720957 5020118 8026567 8038976
 * @summary Test to make sure that -link and -linkoffline link to
 * right files, and URLs with and without trailing slash are accepted.
 * @author jamieh
 * @library ../lib/
 * @build JavadocTester TestLinkOption
 * @run main TestLinkOption
 */

import java.io.File;

public class TestLinkOption extends JavadocTester {

    //Generate the documentation using -linkoffline and a URL as the first parameter.
    private static final String[] ARGS1 = new String[] {
        "-d", OUTPUT_DIR + "-1", "-sourcepath", SRC_DIR,
        "-linkoffline", "http://java.sun.com/j2se/1.4/docs/api/",
        SRC_DIR, "-package", "pkg", "java.lang"
    };

    private static final String[][] TEST1 = {
        { "pkg/C.html",
            "<a href=\"http://java.sun.com/j2se/1.4/docs/api/java/lang/String.html?is-external=true\" " +
            "title=\"class or interface in java.lang\"><code>Link to String Class</code></a>"
        },
        //Make sure the parameters are indented properly when the -link option is used.
        { "pkg/C.html",
            "(int&nbsp;p1,\n" +
            "      int&nbsp;p2,\n" +
            "      int&nbsp;p3)"
        },
        { "pkg/C.html",
            "(int&nbsp;p1,\n" +
            "      int&nbsp;p2,\n" +
            "      <a href=\"http://java.sun.com/j2se/1.4/docs/api/java/lang/Object.html?is-external=true\" title=\"class or interface in java.lang\">" +
            "Object</a>&nbsp;p3)"
        },
        { "java/lang/StringBuilderChild.html",
            "<pre>public abstract class <span class=\"typeNameLabel\">StringBuilderChild</span>\n" +
                "extends <a href=\"http://java.sun.com/j2se/1.4/docs/api/java/lang/Object.html?is-external=true\" " +
            "title=\"class or interface in java.lang\">Object</a></pre>"
        },

    };
    private static final String[][] NEGATED_TEST1 = NO_TEST;

    //Generate the documentation using -linkoffline and a relative path as the first parameter.
    //We will try linking to the docs generated in test 1 with a relative path.
    private static final String[] ARGS2 = new String[] {
        "-d", OUTPUT_DIR + "-2", "-sourcepath", SRC_DIR,
        "-linkoffline", "../" + OUTPUT_DIR + "-1",
        OUTPUT_DIR + "-1", "-package", "pkg2"
    };

    private static final String[][] TEST2 = {
        { "pkg2/C2.html",
            "This is a link to <a href=\"../../" + OUTPUT_DIR +
            "-1/pkg/C.html?is-external=true\" " +
            "title=\"class or interface in pkg\"><code>Class C</code></a>."
        }
    };
    /*
     * Create the documentation using the -link option, vary the behavior with
     * both trailing and no trailing slash. We are only interested in ensuring
     * that the command executes with no errors or related warnings.
     */
    static String[] createArguments(boolean withTrailingSlash) {
        String packagePath = new File(OUTPUT_DIR + "-1").getAbsolutePath();
        String outputDirName = OUTPUT_DIR;
        if (withTrailingSlash) {
            // add the trailing slash, if it is not present!
            if (!packagePath.endsWith(FS)) {
                packagePath = packagePath + FS;
            }
            outputDirName = outputDirName + "-3";
        } else {
            // remove the trailing slash, if it is present!
            if (packagePath.endsWith(FS)) {
                packagePath = packagePath.substring(0, packagePath.length() - 1);
            }
            outputDirName = outputDirName + "-4";
        }
        String args[] = {
            "-d", outputDirName, "-sourcepath", SRC_DIR,
            "-link", "file:///" + packagePath, "-package", "pkg2"
        };
        System.out.println("packagePath: " + packagePath);
        return args;
    }
    /**
     * The entry point of the test.
     * @param args the array of command line arguments.
     */
    public static void main(String[] args) {
        TestLinkOption tester = new TestLinkOption();
        tester.run(ARGS1, TEST1, NEGATED_TEST1);
        tester.run(ARGS1, TEST1, NEGATED_TEST1);
        tester.run(ARGS2, TEST2, NO_TEST);
        tester.runJavadoc(createArguments(true));  // with trailing slash
        tester.runJavadoc(createArguments(false)); // without trailing slash
        tester.printSummary();
        if (tester.getWarningOutput().contains("warning - Error fetching URL")) {
            throw new Error("URL rejected ?");
        }
        tester.printSummary();
    }
}