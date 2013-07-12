package org.jentrata.ebms.cpa.pmode;

/**
 * A property is a data structure that consists of four values: the property
 * name, which can be used as an identifier of the property
 *
 * @author aaronwalker
 */
public class MessageProperty {

    private String name;
    private String value;
    private boolean required = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
