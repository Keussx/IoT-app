package com.ksopha.thanetearth.retrofit;

/**
 * Created by Kelvin Sopha on 02/04/18.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "country_code",
        "name",
        "lon",
        "id",
        "al",
        "time_zone",
        "zones",
        "lat"
})
public class Site {

    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("name")
    private String name;
    @JsonProperty("lon")
    private Double lon;
    @JsonProperty("id")
    private String id;
    @JsonProperty("al")
    private Double al;
    @JsonProperty("time_zone")
    private String timeZone;
    @JsonProperty("zones")
    private List<Zone> zones = null;
    @JsonProperty("lat")
    private Double lat;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("country_code")
    public String getCountryCode() {
        return countryCode;
    }

    @JsonProperty("country_code")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("lon")
    public Double getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public void setLon(Double lon) {
        this.lon = lon;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("al")
    public Double getAl() {
        return al;
    }

    @JsonProperty("al")
    public void setAl(Double al) {
        this.al = al;
    }

    @JsonProperty("time_zone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("time_zone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("zones")
    public List<Zone> getZones() {
        return zones;
    }

    @JsonProperty("zones")
    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    @JsonProperty("lat")
    public Double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(Double lat) {
        this.lat = lat;
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
