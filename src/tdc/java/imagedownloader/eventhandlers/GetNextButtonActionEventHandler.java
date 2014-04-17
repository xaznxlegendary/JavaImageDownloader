package tdc.java.imagedownloader.eventhandlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import tdc.java.imagedownloader.ImageDownloaderApplication;
import tdc.java.utility.http.FirefoxHTTPRequest;
import tdc.java.utility.http.HTTPRequest.ResponseHeaderKey;

/**
 * @author Cuong Truong
 */
public class GetNextButtonActionEventHandler implements EventHandler<ActionEvent> {
  private static final Logger LOGGER = Logger.getLogger(GetNextButtonActionEventHandler.class);
  
  private ImageDownloaderApplication imageDownloaderApplication;
  
  public GetNextButtonActionEventHandler(ImageDownloaderApplication imageDownloaderApplication) {
    this.imageDownloaderApplication = imageDownloaderApplication;
  }
  
  @Override
  public void handle(ActionEvent actionEvent) {    
    String url = this.imageDownloaderApplication.getUrlPatternHandlingSession().getNextUrl();
    
    if(url == null) {
      String errorMessage;
      
      if(this.imageDownloaderApplication.getUrlPatternHandlingSession().isSessionEnded()) {
        errorMessage = "End of URL pattern session";
      }
      else {
        errorMessage = "Unable to obtain next URL";
      }
      
      LOGGER.error(errorMessage);
      this.imageDownloaderApplication.getMessageLabelProperty().setValue(errorMessage);
    }
    
    FirefoxHTTPRequest request = null;
    InputStream inputStream = null;

    try {
      request = new FirefoxHTTPRequest(url);
      inputStream = request.getInputStream();
    }
    catch(IOException e) {
      LOGGER.error(e);
      this.imageDownloaderApplication.getMessageLabelProperty().setValue("Failed to get image for url " + url);
      this.imageDownloaderApplication.getGettingImageProgressVisibleProperty().setValue(false);
      
      return;
    }
    
    if(request.getResponseHeader(ResponseHeaderKey.CONTENT_TYPE) == null || !request.getResponseHeader(ResponseHeaderKey.CONTENT_TYPE).contains("image")) {
      String errorMessage = "Failed to get image for url " + url;
      LOGGER.error(errorMessage);
      this.imageDownloaderApplication.getMessageLabelProperty().setValue(errorMessage);
      
      return;
    }
    
    Image image = new Image(inputStream);
    this.imageDownloaderApplication.getImageProperty().setValue(image);
    
    try {
      request.close();
    }
    catch(IOException e) {
      LOGGER.error(e);
    }
  }
}
