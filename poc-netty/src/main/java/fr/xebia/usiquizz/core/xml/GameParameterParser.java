package fr.xebia.usiquizz.core.xml;

import com.usi.ObjectFactory;
import com.usi.Sessiontype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;

public class GameParameterParser {

    private static final Logger logger = LoggerFactory.getLogger(GameParameterParser.class);

    private ObjectFactory factory = new ObjectFactory();

    public Sessiontype parseXmlParameter(String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.usi");
            Unmarshaller u = jc.createUnmarshaller();
            StringBuffer xmlStr = new StringBuffer(xml);
            Object o = u.unmarshal(new StringReader(xmlStr.toString()));
            return ((JAXBElement<Sessiontype>) o).getValue();
        }
        catch (JAXBException e) {
            logger.error("Game parameter file not valid", e);
            throw new InvalidParameterFileException("Game parameter file not valid", e);
        }
    }

    public String formatXmlParameter(Sessiontype sessiontype) {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.usi");
            Marshaller u = jc.createMarshaller();
            StringWriter sw = new StringWriter();
            u.marshal(factory.createGamesession(sessiontype), sw);
            sw.flush();
            return sw.toString();
        }
        catch (JAXBException e) {
            logger.error("Game parameter file not valid", e);
            throw new InvalidParameterFileException("Game parameter file not valid", e);
        }
    }

}
