package tdc.java.imagedownloader;

import java.io.File;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import tdc.java.imagedownloader.eventhandlers.GetNextButtonActionEventHandler;
import tdc.java.imagedownloader.eventhandlers.SaveButtonActionEventHandler;
import tdc.java.imagedownloader.url.UrlPatternHandlingSession;

/**
 * @author Cuong Truong
 */
public class ImageDownloaderApplication extends Application {  
  private static final String STAGE_TITLE = "Image Downloader";
  private static final boolean STAGE_MAXIMIZED = true;
  private static final String STAGE_ICON_PATH = "tdc/java/imagedownloader/resources/icon.png";

  private Stage stage;
  private Scene scene;
  private GridPane gpUrlInput;
  private GridPane gpRoot;
  private GridPane gpImageOutput;
  private TextField tfUrlPattern;
  private Button btnLock;
  private Button btnGet;
  private Button btnOutputFolder;
  private Button btnSave;
  private ImageView imageView;
  private ProgressIndicator piGettingImage;
  private Label lblMessage;
  
  private DirectoryChooser directoryChooser;
  
  private SimpleBooleanProperty gettingImageProgressVisibleProperty;
  private SimpleStringProperty urlPatternProperty;
  private SimpleObjectProperty<Image> imageProperty;
  private SimpleStringProperty messageLabelProperty;
  private SimpleStringProperty outputFolderProperty;
  
  private UrlPatternHandlingSession urlPatternHandlingSession;
  private File outputFolder;
  
  static {
    configureLoggers();
  }
  
  private static void configureLoggers() {
    PatternLayout patternLayout = new PatternLayout();
    patternLayout.setConversionPattern("%p %d %-10.30c{1} %-10.30M %-5.10L %m%n");
    
    RollingFileAppender rollingFileAppender = new RollingFileAppender();
    rollingFileAppender.setName("rollingFileAppender");
    rollingFileAppender.setFile("./log/application.log");
    rollingFileAppender.setMaxFileSize("10MB");
    rollingFileAppender.setMaxBackupIndex(5);
    rollingFileAppender.setLayout(patternLayout);
    rollingFileAppender.activateOptions();
    
    ConsoleAppender consoleAppender = new ConsoleAppender();
    consoleAppender.setName("consoleAppender");
    consoleAppender.setLayout(patternLayout);
    consoleAppender.setTarget(ConsoleAppender.SYSTEM_OUT);
    consoleAppender.activateOptions();
    
    Logger loggerFilesOrganizer = Logger.getLogger("tdc.java.imagedownloader");
    loggerFilesOrganizer.addAppender(rollingFileAppender);
    loggerFilesOrganizer.setAdditivity(false);
    
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.addAppender(consoleAppender);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {
    this.initializeProperties();
    this.initializeDirectoryChooser();
    
    this.scene = new Scene(this.createRoot());

    this.stage = stage;
    this.stage.setTitle(STAGE_TITLE);
    this.stage.setMaximized(STAGE_MAXIMIZED);
    this.stage.getIcons().add(new Image(STAGE_ICON_PATH));
    this.stage.setScene(this.scene);
    this.stage.show();
  }
  
  /**
   * Initialize all the properties
   */
  protected void initializeProperties() {
    this.gettingImageProgressVisibleProperty = new SimpleBooleanProperty(false);
    this.urlPatternProperty = new SimpleStringProperty();
    this.imageProperty = new SimpleObjectProperty<Image>();
    this.messageLabelProperty = new SimpleStringProperty();
    this.outputFolderProperty = new SimpleStringProperty("C:/");
  }
  
  /**
   * Initialize the directory chooser to select directory
   */
  protected void initializeDirectoryChooser() {
    this.directoryChooser = new DirectoryChooser();
    this.directoryChooser.setTitle("Select directory");
  }

  /**
   * Create the main root node for the scene.
   * @return {@link Parent} instance
   */
  protected Parent createRoot() {
    ScrollPane scrollPane = new ScrollPane();

    this.gpRoot = new GridPane();
    this.gpRoot.add(this.createUrlInputWidget(), 0, 0);
    this.gpRoot.add(this.createImageOutputWidget(), 0, 1);
    this.gpRoot.add(this.createGettingImageProgressIndicator(), 0, 2);
    this.gpRoot.add(this.createImageView(), 0, 3);
    this.gpRoot.add(this.createMessageLabel(), 0, 4);

    scrollPane.setContent(this.gpRoot);
    scrollPane.fitToHeightProperty().setValue(true);
    scrollPane.fitToWidthProperty().setValue(true);

    return scrollPane;
  }

  /**
   * Creates a text field where URL will be entered.
   * @return
   */
  protected GridPane createUrlInputWidget() {
    this.gpUrlInput = new GridPane();
    this.gpUrlInput.setPadding(new Insets(15, 15, 15, 15));
    this.gpUrlInput.setHgap(15);
    this.gpUrlInput.setStyle("-fx-background-color: rgb(220, 220, 220)");
    ColumnConstraints column0Constraints = new ColumnConstraints();
    column0Constraints.setPercentWidth(75);
    this.gpUrlInput.getColumnConstraints().add(0, column0Constraints);

    this.tfUrlPattern = new TextField();
    this.tfUrlPattern.textProperty().bindBidirectional(this.getUrlPatternProperty());
    
    this.btnLock = new Button("Lock");
    this.btnLock.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if(btnLock.getText().equals("Lock")) {
          tfUrlPattern.setDisable(true);
          btnGet.setDisable(false);
          btnLock.setText("Unlock");
          urlPatternHandlingSession = new UrlPatternHandlingSession(getUrlPatternProperty().getValue());
        }
        else if(btnLock.getText().equals("Unlock")) {
          tfUrlPattern.setDisable(false);
          btnGet.setDisable(true);
          btnLock.setText("Lock");
        }
      }
    });

    this.btnGet = new Button("Get Next");
    this.btnGet.addEventHandler(ActionEvent.ACTION, new GetNextButtonActionEventHandler(this));
    this.btnGet.setDisable(true);
    this.btnGet.setMinWidth(100);

    this.gpUrlInput.add(this.tfUrlPattern, 0, 0);
    this.gpUrlInput.add(this.btnLock, 1, 0);
    this.gpUrlInput.add(this.btnGet, 2, 0);

    return gpUrlInput;
  }
  
  /**
   * Creates a collection of controls that allows an image to be downloaded to file
   * @return
   */
  protected GridPane createImageOutputWidget() {
    this.gpImageOutput = new GridPane();
    this.gpImageOutput.setPadding(new Insets(15, 15, 15, 15));
    this.gpImageOutput.setStyle("-fx-background-color: rgb(230, 230, 230)");
    
    this.btnOutputFolder = new Button();
    this.btnOutputFolder.textProperty().bind(this.getOutputFolderProperty());
    this.btnOutputFolder.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        outputFolder = directoryChooser.showDialog(stage);
        
        if(outputFolder != null) {
          getOutputFolderProperty().setValue(outputFolder.getName());
        }
      }
    });
    
    this.btnSave = new Button("Save");
    this.btnSave.addEventHandler(ActionEvent.ACTION, new SaveButtonActionEventHandler(this));
    
    this.gpImageOutput.add(this.btnOutputFolder, 0, 0);
    this.gpImageOutput.add(this.btnSave, 1, 0);
    
    return this.gpImageOutput;
  }
  
  /**
   * Creates a progress indicator to display while the image is being downloaded.
   * @return {@link ProgressIndicator} instance
   */
  protected ProgressIndicator createGettingImageProgressIndicator() {
    this.piGettingImage = new ProgressIndicator();
    this.piGettingImage.visibleProperty().bindBidirectional(this.getGettingImageProgressVisibleProperty());
    
    return this.piGettingImage;
  }

  /**
   * Creates an image view to display the image
   * @return {@link ImageView} instance
   */
  protected ImageView createImageView() {
    this.imageView = new ImageView();
    this.imageView.imageProperty().bindBidirectional(this.getImageProperty());

    return this.imageView;
  }
  
  /**
   * Creates a label to display message
   * @return
   */
  protected Label createMessageLabel() {
    this.lblMessage = new Label();
    this.lblMessage.textProperty().bindBidirectional(this.getMessageLabelProperty());
    
    return this.lblMessage;
  }
  
  /**
   * Gets the url string property that is bound to the url text field.
   * @return {@link SimpleStringProperty} instance
   */
  public SimpleStringProperty getUrlPatternProperty() {
    return this.urlPatternProperty;
  }
  
  /**
   * Returns the image property that is bound to the image view.
   * @return {@link SimpleObjectProperty} instance
   */
  public SimpleObjectProperty<Image> getImageProperty() {
    return this.imageProperty;
  }
  
  /**
   * Returns the visible property of the progress indicator that is
   * displayed when the image is downloading.
   * @return {@link BooleanProperty} instance
   */
  public SimpleBooleanProperty getGettingImageProgressVisibleProperty() {
    return this.gettingImageProgressVisibleProperty;
  }
  
  /**
   * Gets the message label property that is displayed when an image is failed
   * to download.
   * @return {@link SimpleStringProperty} instance
   */
  public SimpleStringProperty getMessageLabelProperty() {
    return this.messageLabelProperty;
  }
  
  /**
   * Gets the output folder property that is used to save the image.
   * @return {@link SimpleStringProperty} instance
   */
  public SimpleStringProperty getOutputFolderProperty() {
    return this.outputFolderProperty;
  }
  
  /**
   * @return the output directory to save the image.
   */
  public File getOutputDirectory() {
    return this.outputFolder;
  }
  
  public UrlPatternHandlingSession getUrlPatternHandlingSession() {
    return this.urlPatternHandlingSession;
  }
}
