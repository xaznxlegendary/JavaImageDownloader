package tdc.java.imagedownloader.eventhandlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import org.apache.log4j.Logger;

import tdc.java.imagedownloader.ImageDownloaderApplication;
import tdc.java.utility.http.FirefoxHTTPRequest;
import tdc.java.utility.http.HTTPRequest.ResponseHeaderKey;

/**
 * @author Cuong Truong
 */
public class SaveButtonActionEventHandler implements EventHandler<ActionEvent> {
  private static final Logger LOGGER = Logger.getLogger(SaveButtonActionEventHandler.class);
  
  private ImageDownloaderApplication application;
  
  public SaveButtonActionEventHandler(ImageDownloaderApplication application) {
    this.application = application;
  }
  
  @Override
  public void handle(ActionEvent event) {
    File directory = this.application.getOutputDirectory();
    
    if(directory == null || !directory.exists()) {
      String errorMessage = "Can't save image because directory does not exist";
      LOGGER.error(errorMessage);
      this.application.getMessageLabelProperty().setValue(errorMessage);
      
      return;
    }
    
    if(this.application.getUrlPatternHandlingSession() == null) {
      String errorMessage = "No URL pattern handling session";
      LOGGER.error(errorMessage);
      this.application.getMessageLabelProperty().setValue(errorMessage);
      
      return;
    }
    
    String fileName = this.application.getUrlPatternHandlingSession().getCurrentUrlAsFileName();
    
    if(fileName == null) {
      String errorMessage = "Output filename invalid";
      LOGGER.error(errorMessage);
      this.application.getMessageLabelProperty().setValue(errorMessage);
      
      return;
    }
    
    File newFile = new File(directory.getAbsolutePath() + File.separator + fileName);
    
    if(newFile.exists()) {
      String errorMessage = "File " + newFile.getName() + " already exist";
      LOGGER.error(errorMessage);
      this.application.getMessageLabelProperty().setValue(errorMessage);
      
      return;
    }
    
    String imageUrl = this.application.getUrlPatternHandlingSession().getCurrentUrl();
    
    try {
      FirefoxHTTPRequest request = new FirefoxHTTPRequest(imageUrl);
      InputStream inputStream = request.getInputStream();
      
      if(request.getResponseHeader(ResponseHeaderKey.CONTENT_TYPE) != null && request.getResponseHeader(ResponseHeaderKey.CONTENT_TYPE).contains("image")) {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        
        while((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
          bos.write(buffer, 0, bytesRead);
        }
        
        bos.close();
        
        this.application.getMessageLabelProperty().setValue(imageUrl + " saved successfully");
      }
      
      request.close();
    }
    catch(IOException e) {
      LOGGER.error(e);
      this.application.getMessageLabelProperty().setValue("Unable to save image " + imageUrl);
      
      return;
    }
  }
}
