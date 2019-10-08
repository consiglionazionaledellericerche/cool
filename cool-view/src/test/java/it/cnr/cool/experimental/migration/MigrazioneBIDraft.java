/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.experimental.migration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class MigrazioneBIDraft {

    public static void main(String[] args) {

        String baseDir = "src/test/resources/bulkInfo";
        String baseDirOut = "bulkInfo_new";
        if (args.length > 0) {
            baseDir = args[0];
        }
        if (args.length > 1) {
            baseDirOut = args[1];
        }

        File dir = new File(baseDir);
        File dirOut = new File(baseDirOut);
        dirOut.mkdir();

        String[] files = dir.list();

        for (String filename : files) {

            System.out.println();
            System.out.println("Processing File " + filename);
            System.out.println();

            File inFile = new File(baseDir + "/" + filename);

            if (!inFile.isDirectory() && filename.endsWith(".xml")) {

                String filenameOut = dirOut.getName() + "/" + inFile.getName();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                try {
                    DocumentBuilder builder = dbf.newDocumentBuilder();
                    Document document = builder.parse(inFile);

                    transformNodeInfo(document, document);

                    // write the content into xml file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult(new File(filenameOut));

                    // Output to console for testing
                    // StreamResult result = new StreamResult(System.out);

                    transformer.transform(source, result);

                    System.out.println("File saved!");

                } catch (SAXException sxe) {
                    Exception x = sxe;
                    if (sxe.getException() != null)
                        x = sxe.getException();
                    x.printStackTrace();

                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (TransformerConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (TransformerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Stampa le info sui nodi, in modo ricorsivo
     *
     * @param currentNode il nodo corrente
     */
    public static void transformNodeInfo(Node currentNode, Document document) {
        short sNodeType = currentNode.getNodeType();

        //Se e' di tipo Element ricavo le informazioni e le stampo
        if (sNodeType == Node.ELEMENT_NODE) {
            String sNodeName = currentNode.getNodeName();
            NamedNodeMap nnmAttributes = currentNode.getAttributes();
            System.out.println("Elemento: " + sNodeName);

            // set new xsd version
            if (currentNode.getNodeName() != null && currentNode.getNodeName().equals("bulkInfo")) {
                Element rootElement = (Element) currentNode;
                rootElement.setAttribute("xmlns", "http://www.cnr.it/schema/BulkInfo_v2");
                rootElement.setAttribute("xsi:schemaLocation", "http://www.cnr.it/schema/BulkInfo_v2 BulkInfo_v2");
            }

            transformIfJson(nnmAttributes, currentNode, document);

        }
        int iChildNumber = currentNode.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (currentNode.hasChildNodes()) {
            NodeList nlChilds = currentNode.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                transformNodeInfo(nlChilds.item(iChild), document);
            }
        }
    }


    private static void transformIfJson(NamedNodeMap nnm, Node xmlNode, Document document) {
        if (!(xmlNode instanceof Element)) {
            System.out.println("ERROR: NODE NOT ELEMENT");
        }

        if (nnm != null && nnm.getLength() > 0) {
            java.util.List<String> attributesToRemove = new ArrayList<String>();

            for (int iAttr = 0; iAttr < nnm.getLength(); iAttr++) {
                System.out.println("\tParsing attribute " + iAttr + ": " + nnm.item(iAttr).getNodeName());
                if (nnm.item(iAttr).getNodeName().startsWith("json")) {
                    System.out.println("\tAttribute accepted for transformation");
                    attributesToRemove.add(nnm.item(iAttr).getNodeName());

                    String jsonString = nnm.item(iAttr).getNodeValue();
                    try {
                        System.out.println("JsonString " + jsonString);
                        JSONObject json = new JSONObject(jsonString);
                        //
                        Element xmlElement = (Element) xmlNode;
                        Element xmlChild = document.createElement(nnm.item(iAttr).getNodeName());

                        xmlElement.appendChild(xmlChild);

                        getXMLElementFromJSONObject(json, xmlChild, document);

                    } catch (Exception e) {
                        //e.printStackTrace();

                        try {
                            JSONArray jsonArray = new JSONArray(jsonString);

                            Element xmlElement = (Element) xmlNode;
                            Element xmlChild = document.createElement(nnm.item(iAttr).getNodeName());

                            xmlElement.appendChild(xmlChild);

                            getXmlElementFromJsonArray(nnm.item(iAttr).getNodeName(), jsonArray, xmlChild, document);

                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
            for (int i = 0; i < attributesToRemove.size(); i++) {
                Element xmlElement = (Element) xmlNode;
                xmlElement.removeAttribute(attributesToRemove.get(i));
            }

        }
    }

    private static void getXMLElementFromJSONObject(JSONObject jsonObject, Node xmlNode, Document document) {
        JSONObject current = jsonObject;
        Iterator<String> keys = current.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = current.get(key);

            if (value instanceof JSONArray) {

                Element xmlElement = (Element) xmlNode;
                Element xmlChild = document.createElement(key);

                xmlElement.appendChild(xmlChild);

                getXmlElementFromJsonArray(key, ((JSONArray) value), xmlChild, document);

            } else if (value instanceof JSONObject) {

                Element xmlElement = (Element) xmlNode;
                Element xmlChild = document.createElement(key);

                xmlElement.appendChild(xmlChild);
                getXMLElementFromJSONObject((JSONObject) value, xmlChild, document);

            } else {
                ((Element) xmlNode).setAttribute(key, value.toString());
            }

        }
    }

    /**
     * Side effects
     *
     * @param arr
     * @param xmlElement
     */
    private static void getXmlElementFromJsonArray(String key, JSONArray arr, Node xmlNode, Document document) {


        int length = arr.length();

        for (int i = 0; i < length; i++) {
            Object obj = arr.get(i);
            if (obj instanceof JSONArray) {

                Element xmlElement = (Element) xmlNode;
                Element xmlChild = document.createElement("pippo");

                xmlElement.appendChild(xmlChild);

                getXmlElementFromJsonArray("listElement", (JSONArray) obj, xmlNode, document);
            } else if (obj instanceof JSONObject) {

                Element xmlElement = (Element) xmlNode;
                Element xmlChild = document.createElement("listElement");

                xmlElement.appendChild(xmlChild);
                getXMLElementFromJSONObject((JSONObject) obj, xmlChild, document);

            } else {
                System.out.println("ERROR, outcome non previsto");
            }
        }
    }

    /**
     * Side effects
     *
     * @param key
     * @param xmlNode
     * @return
     */
    @Deprecated
    private static void parseAttribute(String key, String value, Node xmlNode) {
        Element xmlElement = (Element) xmlNode;

        xmlElement.setAttribute(key, value);
    }

    @Test
    public void success() {
    }

}
