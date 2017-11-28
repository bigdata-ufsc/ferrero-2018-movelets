
package br.ufsc.trajectoryclassification.model.vo.description;

import java.util.HashMap;
import java.util.Map;

public class ReadsDesc {

    private Integer order;
    private String type;
    private String text;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
