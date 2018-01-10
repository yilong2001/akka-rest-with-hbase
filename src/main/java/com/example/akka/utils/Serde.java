package com.example.akka.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONException;

/**
 * Created by yilong on 2018/1/8.
 */
public class Serde {
    public static <T> T xml2Object(String xmlStr, Class<T> c) {
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            T t = (T) unmarshaller.unmarshal(new StringReader(xmlStr));
            return t;
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String object2Xml(Object object) {
        try {
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshal = context.createMarshaller();

            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // 格式化输出
            marshal.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// 编码格式,默认为utf-8
            marshal.setProperty(Marshaller.JAXB_FRAGMENT, false);// 是否省略xml头信息
            marshal.setProperty("jaxb.encoding", "utf-8");
            marshal.marshal(object, writer);
            return new String(writer.getBuffer());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String xmlToJson(String xml) {
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xml);
            return xmlJSONObj.toString(0);
        } catch (JSONException je) {
            je.printStackTrace();
            throw new RuntimeException(je);
        }
    }
}
