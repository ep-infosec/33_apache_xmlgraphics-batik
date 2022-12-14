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
package org.apache.batik.bridge.svg12;

import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class to handle the XPath Pattern syntax for XBL content elements.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public class XPathPatternContentSelector extends AbstractContentSelector {

    /**
     * The Xalan prefix resolver.
     */
    protected NSPrefixResolver prefixResolver = new NSPrefixResolver();

    /**
     * The XPath expression.
     */
    protected XPathExpression xpath;

    /**
     * The selected nodes.
     */
    protected SelectedNodes selectedContent;

    /**
     * The expression string.
     */
    protected String expression;

    /**
     * Creates a new XPathPatternContentSelector.
     */
    public XPathPatternContentSelector(ContentManager cm,
                                       XBLOMContentElement content,
                                       Element bound,
                                       String selector) {
        super(cm, content, bound);
        expression = selector;
        parse();
    }

    /**
     * Parses the XPath selector.
     */
    protected void parse() {
        try {
            XPath xPathAPI = XPathFactory.newInstance().newXPath();
            xPathAPI.setNamespaceContext(prefixResolver);
            xpath = xPathAPI.compile(expression);
        } catch (XPathExpressionException te) {
            AbstractDocument doc
                = (AbstractDocument) contentElement.getOwnerDocument();
            throw doc.createXPathException
                (XPathException.INVALID_EXPRESSION_ERR,
                 "xpath.invalid.expression",
                 new Object[] { expression, te.getMessage() });
        }
    }

    /**
     * Returns a list of nodes that were matched by the given selector
     * string.
     */
    public NodeList getSelectedContent() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
        }
        return selectedContent;
    }

    /**
     * Forces this selector to update its selected nodes list.
     * Returns true if the selected node list needed updating.
     * This assumes that the previous content elements in this
     * shadow tree (in document order) have up-to-date selectedContent
     * lists.
     */
    boolean update() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
            return true;
        }
        parse();
        return selectedContent.update();
    }

    /**
     * Implementation of NodeList that contains the nodes that matched
     * this selector.
     */
    protected class SelectedNodes implements NodeList {

        /**
         * The selected nodes.
         */
        protected ArrayList nodes = new ArrayList(10);

        /**
         * Creates a new SelectedNodes object.
         */
        public SelectedNodes() {
            update();
        }

        protected boolean update() {
            ArrayList oldNodes = (ArrayList) nodes.clone();
            nodes.clear();
            for (Node n = boundElement.getFirstChild();
                    n != null;
                    n = n.getNextSibling()) {
                update(n);
            }
            int nodesSize = nodes.size();
            if (oldNodes.size() != nodesSize) {
                return true;
            }
            for (int i = 0; i < nodesSize; i++) {
                if (oldNodes.get(i) != nodes.get(i)) {
                    return true;
                }
            }
            return false;
        }

        protected boolean descendantSelected(Node n) {
            n = n.getFirstChild();
            while (n != null) {
                if (isSelected(n) || descendantSelected(n)) {
                    return true;
                }
                n = n.getNextSibling();
            }
            return false;
        }

        protected void update(Node n) {
            if (!isSelected(n)) {
                try {
                    Double matchScore = (Double) xpath.evaluate(n, XPathConstants.NUMBER);
                    if (matchScore != null) {
                        if (!descendantSelected(n)) {
                            nodes.add(n);
                        }
                    } else {
                        n = n.getFirstChild();
                        while (n != null) {
                            update(n);
                            n = n.getNextSibling();
                        }
                    }
                } catch (XPathExpressionException te) {
                    AbstractDocument doc
                        = (AbstractDocument) contentElement.getOwnerDocument();
                    throw doc.createXPathException
                        (XPathException.INVALID_EXPRESSION_ERR,
                         "xpath.error",
                         new Object[] { expression, te.getMessage() });
                }
            }
        }

        /**
         * <b>DOM</b>: Implements {@link org.w3c.dom.NodeList#item(int)}.
         */
        public Node item(int index) {
            if (index < 0 || index >= nodes.size()) {
                return null;
            }
            return (Node) nodes.get(index);
        }

        /**
         * <b>DOM</b>: Implements {@link org.w3c.dom.NodeList#getLength()}.
         */
        public int getLength() {
            return nodes.size();
        }
    }
    /**
     * Xalan prefix resolver.
     */
    protected class NSPrefixResolver implements NamespaceContext {
        /**
         * Resolves the given namespace prefix.
         */
        public String getNamespaceURI(String prefix) {
            return contentElement.lookupNamespaceURI(prefix);
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
