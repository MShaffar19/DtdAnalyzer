/*
 * DtdAnalyzer.java
 */

package gov.ncbi.pmc.dtdanalyzer;

import org.apache.commons.cli.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.apache.xml.resolver.tools.*;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.parsers.*;

/**
 * Creates XML representation of an XML DTD and then transforms it using
 * a provided stylesheet. This is a bare-bones application intended for
 * demonstration and debugging.
 */
public class DtdAnalyzer implements OptionHandler {
    
    private static App app;
    
    /**
     * Main execution point. Checks arguments, then converts the DTD into XML.
     * This application currently uses Xerces and
     * Saxon because these are known to work well and will be bundled with this
     * distribution. However, other implementations can be specified through the
     * System properties.
     */
    public static void main (String[] args) {

        String[] optList = {
            "help", "version", "doc", "system", "public", "catalog", "xslt", "title", 
            "roots", "docproc", "markdown", "param"
        };
        app = new App(args, optList, true,
            "dtdanalyzer [-d <xml-file> | -s <system-id> | -p <public-id>] " +
            "[-c <catalog>] [-x <xslt>] [-t <title>] [<out>]",
            "\nThis utility analyzes a DTD and writes an XML output file."
        );
        Options options = app.getActiveOpts();

        // Get the parsed command line arguments
        CommandLine line = app.getLine();


        // This parses the DTD, and corrals the data into a model:
        ModelBuilder model = new ModelBuilder(app.getDtdSpec(), app.getRoots(), app.getResolver());

        XMLWriter writer = new XMLWriter(model);


        // Now run the XSLT transformation.  This defaults to the identity transform, if
        // no XSLT was specified.

        try {
            InputStreamReader reader = writer.getXML();
            
            Transformer xslt = app.getXslt();
            
            String[] xsltParams = app.getXsltParams();
            int numXsltParams = xsltParams.length / 2;
            if (numXsltParams > 0) {
                for (int i = 0; i < numXsltParams; ++i) {
                    xslt.setParameter(xsltParams[2*i], xsltParams[2*i+1]);
                }
            }
            
            // Use this constructor because Saxon always 
            // looks for a system id even when a reader is used as the source  
            // If no string is provided for the sysId, we get a null pointer exception
            Source xmlSource = new StreamSource(reader, "");
            xslt.transform(xmlSource, app.getOutput());
        }

        catch (Exception e){ 
            System.err.println("Could not run the transformation: " + e.getMessage());
            e.printStackTrace(System.out);
        }     
    }

    /**
     * This method will be invoked for each of the command-line options that was given.
     * If it is a custom option, handle it here, otherwise, kick it back to App.
     */
    public void handleOption(Option opt) {
        app.handleOption(opt);
    }
}
