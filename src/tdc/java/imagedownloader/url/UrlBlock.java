package tdc.java.imagedownloader.url;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Cuong Truong
 */
public class UrlBlock {
  private static final Logger LOGGER = Logger.getLogger(UrlBlock.class);
  
  private String block;
  private boolean pattern;
  private String type;
  private int index;
  private int initialValue;
  private int lowerBound;
  private int upperBound;
  private int maxStringSize;
  private boolean asc = true;
  private Set<String> foundKeys;
  
  public UrlBlock(String block, boolean pattern) {
    this.foundKeys = new HashSet<String>();
    this.block = block;
    this.pattern = pattern;
    
    if(this.isPattern()) {
      this.initialize();
    }
  }
  
  public boolean isPattern() {
    return this.pattern;
  }
  
  private void initialize() {
    StringBuilder sb = new StringBuilder();
    boolean lookingForK = true;
    String k = null;
    String v = null;
    
    for(int i = 0; i < this.block.length(); i++) {
      if(this.block.charAt(i) == '=' && lookingForK) {
        lookingForK = false;
        k = sb.toString();
        sb.delete(0, sb.length());
      }
      else if(this.block.charAt(i) == ',' && !lookingForK) {
        lookingForK = true;
        v = sb.toString();
        sb.delete(0, sb.length());
        this.initializeFor(k, v);
      }
      else {
        sb.append(this.block.charAt(i));
      }
    }
    
    if(sb.length() > 0 && !lookingForK) {
      v = sb.toString();
      this.initializeFor(k, v);
    }
    
    if(this.type == null) {
      this.type = UrlPatternParameter.TYPE.getPossibleValues().iterator().next();
    }
  }
  
  private void initializeFor(String k, String v) {
    UrlPatternParameter parameter = UrlPatternParameter.getEnumByKey(k);
    
    if(parameter != null) {
      switch(parameter) {
        case TYPE:
          if(parameter.getPossibleValues().contains(v)) {
            this.type = v;
          }
          else {
            LOGGER.error("Unknown value: " + v);
            return;
          }
          
          break;
        case INDEX:
          this.index = Integer.parseInt(v);
          break;
        case INITIAL_VALUE:
          this.initialValue = Integer.parseInt(v);
          break;
        case LOWER_BOUND:
          this.lowerBound = Integer.parseInt(v);
          break;
        case UPPER_BOUND:
          this.upperBound = Integer.parseInt(v);
          break;
        case ORDER:
          if(parameter.getPossibleValues().contains(v)) {
            if(v.equals("asc")) {
              this.asc = true;
            }
            else {
              this.asc = false;
            }
          }
          
          break;
        case MAX_STRING_SIZE:
          this.maxStringSize = Integer.parseInt(v);
      }
      
      this.foundKeys.add(parameter.getKey());
    }
    else {
      LOGGER.error("Unknown key: " + k);
    }
  }
  
  public int getIndex() {
    return this.index;
  }
  
  public String getType() {
    return this.type;
  }
  
  public int getInitialValue() {
    return this.initialValue;
  }
  
  public int getLowerBound() {
    return this.lowerBound;
  }
  
  public int getUpperBound() {
    return this.upperBound;
  }
  
  public int getMaxStringSize() {
    return this.maxStringSize;
  }
  
  public boolean isAsc() {
    return this.asc;
  }
  
  public String getBlock() {
    return this.block;
  }
  
  public boolean containsKey(String key) {
    return this.foundKeys.contains(key);
  }
}
