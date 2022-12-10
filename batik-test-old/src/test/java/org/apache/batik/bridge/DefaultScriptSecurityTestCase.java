/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.bridge;

import org.apache.batik.util.ParsedURL;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.batik.util.SVGConstants.SVG_SCRIPT_TYPE_JAVA;

public class DefaultScriptSecurityTestCase {
    @Test
    public void testUrls() {
        ParsedURL docUrl = new ParsedURL("");
        ParsedURL scriptUrl = new ParsedURL("jar:http://192.168.1.10/poc.jar!/");
        String ex = "";
        try {
            new DefaultScriptSecurity(null, scriptUrl, docUrl).checkLoadScript();
        } catch (SecurityException e) {
            ex = e.getMessage();
        }
        Assert.assertEquals(ex, "The document references a script file (jar:http://192.168.1.10/poc.jar!/) " +
                "which comes from different location than the document itself. This is not allowed with the current " +
                "security settings and that script will not be loaded.");
    }

    @Test
    public void testJarFile() {
        ParsedURL docUrl = new ParsedURL("");
        ParsedURL scriptUrl = new ParsedURL("poc.jar");
        String ex = "";
        try {
            new DefaultScriptSecurity(SVG_SCRIPT_TYPE_JAVA, scriptUrl, docUrl).checkLoadScript();
        } catch (SecurityException e) {
            ex = e.getMessage();
        }
        Assert.assertEquals(ex, "Could not access the current document URL when trying to load script file " +
                "file:poc.jar. Script will not be loaded as it is not possible to verify it comes from the same location " +
                "as the document.");
    }
}
