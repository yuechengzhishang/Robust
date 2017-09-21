package com.meituan.robust.tools.aapt;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ref https://developer.android.com/guide/topics/resources/complex-xml-resources.html
 * split bundle resources
 * AAPT will generate resource files and names for all of the inline resources. Applications built using this inline format are compatible with all versions of Android.
 * split bundle resource manual because of apply public.
 * bunnyblue
 */
public class BundleResourcesSpliter {
    //    private static final XPathExpression AAPT_ATTR_ALL = createExpression("//animated-vector/aapt:attr[@name='android:drawable']/vector/*/clip-path|" +
//            "/animated-vector//aapt:attr[@name='android:drawable']/vector/*/path" +
//            "|//animated-vector/aapt:attr[@name='android:drawable']/vector/path|" +
//            "//animated-vector/aapt:attr[@name='android:drawable']/vector/clip-path");
    private static final XPathExpression AAPT_ATTR = createExpression("/animated-vector/aapt:attr/vector");
    private static final XPathExpression AAPT_TARGET = createExpression(" /animated-vector/target");
    int indexMain = 0;
    String baseName;
    Document mDocument;
    File folder;
    File inputFile;

    public BundleResourcesSpliter(File resFile) {
        inputFile = resFile;
        folder = resFile.getParentFile();
        baseName = resFile.getName().replaceAll(".xml", "");
    }

    private static XPathExpression createExpression(String expressionStr) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    if (prefix.equals("aapt")) {
                        return "http://schemas.android.com/aapt";
                    }
                    throw new UnsupportedOperationException("");
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return "http://schemas.android.com/aapt";
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    return null;
                }
            });
            return xPath.compile(expressionStr);
        } catch (XPathExpressionException e) {
            e.printStackTrace();

        }
        return null;
    }

    public boolean tyrSplitResources() {
        if (!inputFile.getName().endsWith(".xml")) {
            return false;
        }
        parse(inputFile);
        try {
            return tryValidAndSplitBundleResources();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean tryValidAndSplitBundleResources() throws IOException, XPathExpressionException {

//        NodeList nodesWithIds = (NodeList)
//                AAPT_ATTR.evaluate(document, XPathConstants.NODESET);\
        NodeList nodesWithIds = (NodeList)
                AAPT_ATTR.evaluate(mDocument, XPathConstants.NODESET);
        if (nodesWithIds.getLength() == 0) {
            //  System.err.println("tryValidAndSplitBundleResources- not found---" + mDocument.toString());
            return false;
        }

        Map<String, String> rootMap = new HashMap<>();
        for (int i = 0; i < nodesWithIds.getLength(); i++) {
            Node resourceName1 = nodesWithIds.item(0);
            indexMain++;
            String typeName = resourceName1.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            String val = typeName.replaceAll("android:", "@") + "/" + baseName + "_" + indexMain;
            rootMap.put(typeName, val);
            Document rootDoc = getEmptyDocument();
            Node fake = rootDoc.importNode(resourceName1, true);
            rootDoc.appendChild(fake);
            writeXml(rootDoc, new File(folder, baseName + "_" + indexMain + ".xml"));

        }
        ArrayList<HashMap<String, String>> mapArrayList = fixAAPTTarget();
        buildMainResourceXml(rootMap, mapArrayList);
        return true;


    }

    public ArrayList<HashMap<String, String>> fixAAPTTarget() throws IOException, XPathExpressionException {

        ArrayList<HashMap<String, String>> mapArrayList = new ArrayList<>();
        NodeList nodesWithIds = (NodeList)
                AAPT_TARGET.evaluate(mDocument, XPathConstants.NODESET);
        for (int index = 0; index < nodesWithIds.getLength(); index++) {

            String androidTargetName = nodesWithIds.item(index).getAttributes().getNamedItem("android:name").getNodeValue();//eye_mask
            // System.err.println("androidTargetName==" + androidTargetName);
            NodeList nodeList = (nodesWithIds).item(index).getChildNodes();
            HashMap<String, String> kayValue = new HashMap<>();
            kayValue.put("android:name", androidTargetName);
            mapArrayList.add(kayValue);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    String attrName = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();

                    // System.err.println("attrName===" + attrName);
                    NodeList nodeListLast = nodeList.item(i).getChildNodes();
                    indexMain++;
                    String file = baseName + "_" + indexMain;
                    kayValue.put(attrName, "@drawable/" + file);
                    for (int j = 0; j < nodeListLast.getLength(); j++) {
                        if (nodeListLast.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Node node = nodeListLast.item(j);
                            //  System.err.println("node-" + node.toString());
                            Document documentNew = getEmptyDocument();
                            Node inported = documentNew.importNode(node, true);
                            documentNew.appendChild(inported);

                            writeXml(documentNew, new File(folder, file + ".xml"));
                        }
                    }

                }
            }

        }
        return mapArrayList;

    }

    /**
     * <animated-vector android:drawable="@drawable/avd_1"
     * xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt">
     * <target android:name="eye_mask" android:animation="@drawable/avd_2" />
     * <target android:name="strike_through" android:animation="@drawable/avd_3" />
     * </animated-vector>
     */
    public void buildMainResourceXml(Map<String, String> rootMap, ArrayList<HashMap<String, String>> targets) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append("\n");
        stringBuilder.append("<animated-vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        Iterator<Map.Entry<String, String>> iterator = rootMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            stringBuilder.append(entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        stringBuilder.append(">\n");
        for (Map<String, String> map : targets) {
            appendTarget(stringBuilder, map);
        }
        stringBuilder.append("</animated-vector>\n");
        FileUtils.write(inputFile, stringBuilder.toString());
    }

    public static void appendTarget(StringBuilder stringBuilder, Map<String, String> datas) {
        stringBuilder.append("<target ");
        Iterator<Map.Entry<String, String>> iterator = datas.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            stringBuilder.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"\n");
        }
        stringBuilder.append(" />\n");
    }


    private static DocumentBuilder getDocumentBuilder() {
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentBuilder;
    }

    public static Document getEmptyDocument() {
        Document document = null;
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            document = documentBuilder.newDocument();
            document.normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    /**
     * parse
     *
     * @param filename
     * @return Document
     */
    private final void parse(final File filename) {
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            mDocument = documentBuilder.parse(filename);
            mDocument.normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void writeXml(Document doc, File path) {
        Source source = new DOMSource(doc);

        Result result = new StreamResult(path);

        Transformer xformer = null;
        try {
            xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    public static void splitAllBundleResources(File rootDir) {
        LinkedList<File> xmlFiles = new LinkedList<>();
        File[] fileRC = rootDir.listFiles();
        for (File file : fileRC) {
            String name = file.getName();
            if (name.startsWith("drawable-") && name.contains("v2")) {
                xmlFiles.addAll(FileUtils.listFiles(file, new String[]{"xml"}, true));
            }
        }
        int doCount = 0;
        for (File tmp : xmlFiles) {
            String name = tmp.getParentFile().getName();
            if (name.startsWith("drawable-") && name.contains("v2")) {
                BundleResourcesSpliter bundleResourcesSpliter = new BundleResourcesSpliter(tmp);
                if (bundleResourcesSpliter.tyrSplitResources()) {
                    doCount++;
                }
            }
        }
        System.err.println("robust[res] do split bundle resource " + doCount);
    }
}
