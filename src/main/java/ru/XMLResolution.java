package ru;


import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class XMLResolution {

    public static Logger logger = Logger.getLogger(XMLResolution.class.getName());

    public static void main(String[] args) {
        String inXml = args[0];
        String xsd = args[1];
        String xslt = args[2];
        String outXml = args[3];
        boolean validIn = false;
        try {
            validIn = validateXMLSchema(xsd, inXml);
            xslConverter(inXml, xslt, outXml);
            validateXMLSchema(xsd, outXml);
        } catch (FileNotFoundException | TransformerException ex) {
            logger.info("Converting problem");
        } catch (SAXException | IOException ex) {
            String message = validIn ? "FileOut validation problem" : "FileIn validation problem";
            logger.info(message);
        }
    }

    public static boolean validateXMLSchema(String xsdPath, String xmlPath) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(xsdPath));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new File(xmlPath)));
        return true;
    }

    public static void xslConverter(String xmlInFile, String xslFile, String xmlOutFile) throws FileNotFoundException, TransformerException {
        StreamSource xmlSource = new StreamSource(new FileInputStream(xmlInFile));
        StreamSource styleSource = new StreamSource(new FileInputStream(xslFile));
        StreamResult xmlOutput = new StreamResult(new FileOutputStream(xmlOutFile));
        Transformer transformer = TransformerFactory.newInstance().newTransformer(styleSource);
        transformer.transform(xmlSource, xmlOutput);
    }
}
