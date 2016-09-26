package osh.openweathermap.current;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "3h" })
public class Rain implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = 4208232287028059786L;
	@JsonProperty("3h")
	private Integer _3h;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The _3h
	 */
	@JsonProperty("3h")
	public Integer get3h() {
		return _3h;
	}

	/**
	 *
	 * @param _3h
	 *            The 3h
	 */
	@JsonProperty("3h")
	public void set3h(Integer _3h) {
		this._3h = _3h;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}