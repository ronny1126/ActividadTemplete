package oscarblancarte.ipd.templetemethod.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import oscarblancarte.ipd.templetemethod.util.OnMemoryDataBase;
import java.io.File;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rjaimea
 */
public class ProcesarArchivoCargos extends AbstractFileProcessTemplete {

    private String log = "";

    public ProcesarArchivoCargos(File file, String logPath, String movePath) {
        super(file, logPath, movePath);
    }

    @Override
    protected void validateName() throws Exception {
        String fileName = file.getName();
        if (!fileName.endsWith(".xml")) {
            throw new Exception("Invalid file name"
                    + ", must end with .xml");
        }

        if (fileName.length() != 10) {
            System.out.println("longitug: " + fileName.length());
            throw new Exception("Invalid document format");
        }
    }

    @Override
    protected void processFile() throws Exception {
        FileInputStream input = new FileInputStream(file);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(input);
            document.getDocumentElement().normalize();
            NodeList listaCargos = document.getElementsByTagName("cargo");

            for (int numRegistro = 0; numRegistro < listaCargos.getLength(); numRegistro++) {
                Node nodo = listaCargos.item(numRegistro);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nodo;
                    Integer idCargo = Integer.parseInt(element.getAttribute("idCargo"));
                    Integer codigoCliente = Integer.parseInt(element.getElementsByTagName("codigoCliente").item(0).getTextContent());
                    String tipoTarjeta = element.getElementsByTagName("tipoTarjeta").item(0).getTextContent();
                    String numeroTarjeta = element.getElementsByTagName("codigoTarjeta").item(0).getTextContent();
                    Double valorCargo = Double.parseDouble(element.getElementsByTagName("valorCargo").item(0).getTextContent());
                    String fechaCargo = element.getElementsByTagName("fechaCargo").item(0).getTextContent();
                    boolean exist = OnMemoryDataBase.customerExist(codigoCliente);
                    
                    if (!exist) {
                        log += idCargo + " C" + codigoCliente + "\t\t" + tipoTarjeta + "\t\t" + fechaCargo
                                + " Cliente no existe.\n";
                    } else if (!"CR".equals(tipoTarjeta) && !"DB".equals(tipoTarjeta)) {
                        log += idCargo + " C" + codigoCliente + "\t\t" + tipoTarjeta + "\t\t" + fechaCargo
                                + " Tipo de tarjeta incorrecta.\n";
                    } else {

                        log += idCargo + " C" + codigoCliente + "\t\t" + tipoTarjeta + "\t\t" + fechaCargo
                                + " Cargo aplicado correctamente.\n";
                    }
                }
            }

        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void createLog() throws Exception {
        FileOutputStream out = null;
        try {
            File outFile = new File(logPath + "/" + file.getName());
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            out = new FileOutputStream(outFile, false);
            out.write(log.getBytes());
            out.flush();
        } finally {
            out.close();
        }
    }

}
