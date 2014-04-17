package tdc.java.imagedownloader.url;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Cuong Truong
 */
public enum UrlPatternParameter {
  TYPE("type", "int"),
  INDEX("index"),
  INITIAL_VALUE("initialValue"),
  LOWER_BOUND("lowerBound"),
  UPPER_BOUND("upperBound"),
  MAX_STRING_SIZE("maxStringSize"),
  ORDER("order", "asc", "desc");
  
  private String key;
  private Set<String> possibleValues;
  
  private UrlPatternParameter(String key, String ... possibleValues) {
    this.key = key;
    
    if(possibleValues.length > 0) {
      this.possibleValues = new HashSet<String>();
      
      for(String possibleValue : possibleValues) {
        this.possibleValues.add(possibleValue);
      }
    }
  }
  
  public String getKey() {
    return this.key;
  }
  
  public Set<String> getPossibleValues() {
    return this.possibleValues;
  }
  
  public static UrlPatternParameter getEnumByKey(String key) {
    for(UrlPatternParameter parameter : UrlPatternParameter.values()) {
      if(parameter.getKey().equals(key)) {
        return parameter;
      }
    }
    
    return null;
  }
}
