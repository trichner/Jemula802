/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package emulator;

import io.JEIO;
import kernel.JETime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class JE802Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("This is Jemula802. Please provide path and name of the XML scenario file.");
		} else {
			String aScenarioFile = new String(args[0].toString());
			Document configuration = parseDocument(aScenarioFile);
			boolean resume = parseForResume(configuration);
			boolean showGui = parseForShowGui(configuration);
			JE802Control control = null;
			// resume from a previous simulation
			if (resume) {
				control = resumeControl(configuration);
				if (control == null) {
					System.err.println("Resume failed");
					System.exit(0);
				}

				// start a new simulation
			} else {

				control = new JE802Control(configuration, showGui);
				// copy scenario XML file into target directory, for archiving
				// and/or later analysis
				filecopy(aScenarioFile, control.getPath2Results());
			}
			control.startSimulation();
			if (!showGui) {
				// JEIO.save(control, control.getPath2Results());
			}
		}
	}

	private static Document parseDocument(String aScenarioFilename) {
		Document anXMLdoc = null;
		File theScenarioFile = new File(aScenarioFilename); // The file to parse
		if (!theScenarioFile.exists()) {// the XML scenario file does not exist
										// or is not accessible
			System.err.println("This is Jemula802. Error: could not open the XML scenario description file " + theScenarioFile);
			System.exit(0);
		} else {
			System.out.println("This is Jemula802. XML scenario file: " + theScenarioFile.getName());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // create
																					// a
																					// factory
																					// object
																					// for
																					// creating
																					// DOM
																					// parsers
			DocumentBuilder parser;
			try {
				parser = factory.newDocumentBuilder();
				// parse the file and build anXMLdoc tree to represent its
				// content
				anXMLdoc = parser.parse(theScenarioFile);
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return anXMLdoc;
	}

	private static String parseForHibernationFile(Document aDocument) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//JE802Control/@resumeFile";
		try {
			Node fileNameNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
			if (fileNameNode != null) {
				return fileNameNode.getNodeValue();
			}
			return null;

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean parseForResume(Document aDocument) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//JE802Control/@resume";
		try {
			Node resumeNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
			if (resumeNode != null) {
				return new Boolean(resumeNode.getNodeValue());
			}
			return false;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean parseForShowGui(Document configuration) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//JE802Control/@showGui";
		try {
			Node guiNode = (Node) xpath.evaluate(expression, configuration, XPathConstants.NODE);
			return new Boolean(guiNode.getNodeValue());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static long parseForRandomSeed(Document configuration) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//JE802StatEval/@seed";
		try {
			Node seedNode = (Node) xpath.evaluate(expression, configuration, XPathConstants.NODE);
			return new Long(seedNode.getNodeValue());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static JE802Control resumeControl(Document aDocument) {
		String hibernationFileName = parseForHibernationFile(aDocument);
		if (hibernationFileName == null) {
			System.err.println("No hibernation file specified," + " insert attribute  \"resumeFile\" in tag JE802Control");
			return null;
		}
		JE802Control control = (JE802Control) JEIO.load(hibernationFileName);
		// new simulation end is current time plus duration
		JETime emulationEnd = new JETime(parseForDuration(aDocument)).plus(control.getSimulationTime());
		control.setSimulationEnd(emulationEnd);
		long randomSeed = parseForRandomSeed(aDocument);
		control.setRandomSeed(randomSeed);
		return control;
	}

	private static double parseForDuration(Document aDocument) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//JE802Control/@EmulationDuration_ms";
		try {
			Node emulationDurationNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
			return new Double(emulationDurationNode.getNodeValue());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return 0.0;
	}

    public static void filecopy(File source, String destDirectory){
        File destination = new File(destDirectory + File.separatorChar + source.getName());

        File directory = new File(destDirectory);
        if (!directory.exists()) { // directory does not exist
            try {
                System.out.println("creating new destination directory " + destDirectory);
                directory.mkdirs();
            } catch (Exception e) {
                System.err.println("could not create the destination directory " + destDirectory);
            }
        }

        FileChannel in = null, out = null;
        try {
            if (source.isDirectory()) {
                File newDirectory = new File(destination.getAbsolutePath());
                newDirectory.createNewFile();
                for (File file : source.listFiles()) {
                    if (!file.getName().startsWith(".svn")) {
                        filecopy(file.getAbsolutePath(), destDirectory);
                    }
                }
            } else {
                in = new FileInputStream(source).getChannel();
                out = new FileOutputStream(destination).getChannel();

                long size = in.size();
                MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
                out.write(buf);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public static void filecopy(String sourceFileName, String destDirectory) {
		File source = new File(sourceFileName);
		filecopy(source,destDirectory);
	}
}
