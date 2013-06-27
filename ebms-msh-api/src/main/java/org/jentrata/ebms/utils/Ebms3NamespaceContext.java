package org.jentrata.ebms.utils;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds all the ebMS 3 namespaces
 *
 * @author aaronwalker
 */
public class Ebms3NamespaceContext implements NamespaceContext {

    private static Ebms3NamespaceContext _instance = new Ebms3NamespaceContext();

    public static final Ebms3NamespaceContext instance() {
        return _instance;
    }

    private final Map<String,String> namespaces = new HashMap<>();

    private Ebms3NamespaceContext() {
        namespaces.put("eb3","http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/");
        namespaces.put("S11","http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("S12","http://www.w3.org/2003/05/soap-envelope");
        namespaces.put("wsse","http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

    }

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaces.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for(String key : namespaces.keySet()) {
            if(namespaces.get(key).equals(namespaceURI)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
