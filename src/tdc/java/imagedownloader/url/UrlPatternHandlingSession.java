package tdc.java.imagedownloader.url;

import java.util.ArrayList;
import java.util.List;

import tdc.java.utility.common.IntWrapper;

/**
 * @author Cuong Truong
 */
public class UrlPatternHandlingSession {
  private String urlPattern;
  private List<UrlBlock> blocks;
  private List<UrlBlock> indexedPatternBlocks;
  private List<IntWrapper> cursors;
  private boolean sessionEnded;
  private String currentUrl;
  
  public UrlPatternHandlingSession(String urlPattern) {
    if(urlPattern == null || urlPattern.length() == 0) {
      throw new IllegalArgumentException("urlPattern cannot be null or empty");
    }
    
    this.sessionEnded = false;
    this.urlPattern = urlPattern;
    this.initialize();
  }
  
  private void initialize() {
    this.blocks = new ArrayList<UrlBlock>();
    this.indexedPatternBlocks = new ArrayList<UrlBlock>();
    this.cursors = new ArrayList<IntWrapper>();
    boolean patternFound = false;
    StringBuilder sb = new StringBuilder();
    
    for(int i = 0; i < this.urlPattern.length(); i++) {
      if(this.urlPattern.charAt(i) == '{' && !patternFound) {
        patternFound = true;
        
        if(sb.length() > 0) {
          this.blocks.add(new UrlBlock(sb.toString(), false));
          sb.delete(0, sb.length());
        }
      }
      else if(this.urlPattern.charAt(i) == '}' && patternFound) {
        patternFound = false;
        
        if(sb.length() > 0) {
          UrlBlock patternBlock = new UrlBlock(sb.toString(), true);
          this.blocks.add(patternBlock);
          sb.delete(0, sb.length());
        }
      }
      else {
        sb.append(this.urlPattern.charAt(i));
      }
    }
    
    if(sb.length() > 0) {
      this.blocks.add(new UrlBlock(sb.toString(), false));
    }
    
    int index = 0;
    for(int i = 0; i < this.blocks.size(); i++) {
      UrlBlock urlBlock = this.blocks.get(i);
      
      if(urlBlock.isPattern()) {
        if(urlBlock.getIndex() == index) {
          this.indexedPatternBlocks.add(urlBlock);
          this.cursors.add(new IntWrapper(urlBlock.getInitialValue()));
          index++;
          
          //repeat search
          i = 0;
        }
      }
    }
  }
  
  public boolean isSessionEnded() {
    return this.sessionEnded;
  }
  
  public String getNextUrl() {
    StringBuilder url = new StringBuilder();
    
    for(UrlBlock urlBlock : this.blocks) {
      if(urlBlock.isPattern()) {
        int cursor = this.cursors.get(urlBlock.getIndex()).getValue();
        
        if(urlBlock.getType().equals("int")) {
          if(urlBlock.containsKey(UrlPatternParameter.MAX_STRING_SIZE.getKey())) {
            url.append(String.format("%1$0" + urlBlock.getMaxStringSize() + "d", cursor));
          }
          else {
            url.append(cursor);
          }
        }
      }
      else {
        url.append(urlBlock.getBlock());
      }
    }
    
    //advance cursor
    for(int i = 0; i < this.cursors.size(); i++) {
      if(this.indexedPatternBlocks.get(i).isAsc()) {
        this.cursors.get(i).incrementBy(1);
        
        if(this.cursors.get(i).getValue() <= this.indexedPatternBlocks.get(i).getUpperBound()) {
          break;
        }
        else {
          this.cursors.get(i).setValue(this.indexedPatternBlocks.get(i).getInitialValue());
        }
      }
      else {
        this.cursors.get(i).decrementBy(1);
        
        if(this.cursors.get(i).getValue() >= this.indexedPatternBlocks.get(i).getLowerBound()) {
          break;
        }
        else {
          this.cursors.get(i).setValue(this.indexedPatternBlocks.get(i).getInitialValue());
        }
      }
      
      if(i == (this.cursors.size() - 1)) {
        this.sessionEnded = true;
      }
    }
    
    if(this.sessionEnded) {
      return null;
    }
    else {
      this.currentUrl = url.toString();
      return this.currentUrl;
    }
  }
  
  public String getCurrentUrlAsFileName() {
    if(this.currentUrl == null) {
      return null;
    }
    
    return this.currentUrl.replace(':', '_').replace('/', '_');
  }
  
  public String getCurrentUrl() {
    return this.currentUrl;
  }
}
