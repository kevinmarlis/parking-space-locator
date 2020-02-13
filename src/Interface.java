import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Interface extends Application {
	
	ImageProcessing psl = new ImageProcessing();
	ArrayList<ImageView> cars = new ArrayList<ImageView>();
	BorderPane bp = new BorderPane();
	Pane center;
	Pane root;
	HBox top;
	HBox bottom;
	double yCoord = 0;
	Pane pane;
	Button locate = new Button("Locate Spots");
	Button result = new Button("Result");
	boolean clicked = true;
	boolean lot;
	boolean searchClicked = false;
	double startDragX = 0;
	double startDragY = 0;
	ImageView resultPic;
	ImageView currentLot = new ImageView();
	Text available = new Text();
	Scene scene = new Scene(bp, 1300, 800);

	@Override
	public void start(Stage primaryStage) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		center = new Pane();
		top = new HBox();
		bottom = new HBox();
		pane = new Pane();
		locate.getStyleClass().add("glass-grey");
		result.getStyleClass().add("glass-grey");
		center.getChildren().addAll(pane);
		bottom.getChildren().addAll(locate, result);
		bottom.alignmentProperty().set(Pos.CENTER);
		available = new Text();
		top.setPadding(new Insets(20,0,0,0));
		available.getStyleClass().add("text2");
		top.setStyle("-fx-background-color: \"black\"");
		top.setAlignment(Pos.CENTER);
		leftMenu();
		rightMenu();
		bp.setTop(top);
		bp.setCenter(center);
		bottom.setSpacing(50);
		bp.setBottom(bottom);
		top.getChildren().add(available);
		bp.setTop(top);
		
		// BUTTON FOR LOCATING CARS
		locate.setOnMousePressed(e -> {
			if(lot) {
				searchClicked = true;
				if (!clicked) {
					snapShot("rsc/after");
					searchFunctions();
					available.setText(psl.spotsAvailable + " available parking spots");
				}
			}
		});
		
		// BUTTON FOR TOGGLING RESULT & IMAGE OF CARS
		result.setOnMousePressed(e -> {
			if(lot && searchClicked) {
				if(clicked == false) {
					clicked = true;
					center.getChildren().remove(currentLot);
					for (ImageView v : cars) {
						center.getChildren().remove(v);
					}

					try {
						yCoord = 0;
						Image result = new Image(new FileInputStream("rsc/result.png"));
						resultPic = new ImageView(result);
						resultPic.setX(250);
						resultPic.setY(50);
						resultPic.setFitWidth(800);
						resultPic.setFitHeight(600);
						center.getChildren().add(resultPic);
						rightMenu();
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();

					}
				}
				else  {
					clicked = false;
					center.getChildren().remove(currentLot);
					center.getChildren().add(currentLot);
					for (ImageView v : cars) {
						center.getChildren().remove(v);
						center.getChildren().add(v);
					}
				}
			}
		});
		center.setStyle("-fx-background-color: \"black\"");
		bottom.setStyle("-fx-background-color: \"black\"");
		bottom.setPadding(new Insets(0, 0, 10, 0));
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	
	// CALLS THE SEARCH METHODS FROM ImageProcessing
	public void searchFunctions() {
		// SENDS IN THE TWO IMAGES TO BE COMPARED
		psl.locateSpots("rsc/before.png", "rsc/after.png");
	}

	// TAKES A SCREENSHOT OF THE CURRENT PARKING LOT WITH CARS
	public void snapShot(String fileName) {
		WritableImage snapImage = scene.snapshot(null);        
        ImageView snapView = new ImageView();
        snapView.setImage(snapImage);
         
        StackPane snapLayout = new StackPane();
        snapLayout.getChildren().add(snapView);
         
        // USED TO CROP IMAGE
        Scene snapScene = new Scene(snapLayout, 800, 600);

        // SAVES THE IMAGE SO IT CAN BE COMPARED IN SEARCH FUNCITONS
        File file = new File(fileName + ".png");
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(snapScene.snapshot(null), null);
        try {
        	ImageIO.write(renderedImage, "png", file);
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
	}

	// DRAG AND DROP FEATURE FOR THE CARS ON THE RIGHT SIDE OF THE (center) PANE
	public void dragAndDrop(ImageView v, double yCoord2) {
		v.setOnMousePressed(e -> {
			startDragX = e.getX();
			startDragY = e.getY();
		});

		v.setOnMouseDragged(e -> {
			v.setTranslateX(e.getSceneX() - startDragX - 1200);
			v.setTranslateY(e.getSceneY() - startDragY - yCoord2);
		});
		v.setOnMouseReleased(e -> {
			try {
				yCoord = 0;
				rightMenu();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
	}

	// PUTS THE LOT SELECTED IN VIEW AND DISPLAYS IT IN THE PANE
	public void lotChoice(ImageView v, Image i) throws IOException {
		ImageView v1 = new ImageView(i);
		available.setText("");
		lot = true;
		currentLot = v1;
		reset();
		clicked = false;
		center.getChildren().add(v1);
		reAdd();
		v1.setX(250);
		v1.setY(50);
		v1.setFitWidth(800);
		v1.setFitHeight(600);
		snapShot("rsc/before");
	}

	// USED TO RE-ADD CARS TO (center) PANE
	// PREVENTS OVERLAPPING OF LOT WITH CARS (I.E. CARS BEING DRAGGED UNDER LOT IMAGE)
	public void reAdd() {
		for (ImageView v : cars) {
			center.getChildren().remove(v);
			center.getChildren().add(v);
		}
	}
	
	// RESETS EVERYTHING TO THE ORIGINAL 
	public void reset() throws IOException {
		center.getChildren().clear();
		yCoord = 0;
		cars.clear();
		leftMenu();
		psl = new ImageProcessing();
		rightMenu();
	}

	// MENU ON LEFT SIDE OF (center) PANE; USED FOR DISPLAYING LOT OPTIONS
	public void leftMenu() throws IOException {
		Image lot1 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("lot1.png").getFile()));
		Image lot2 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("lot7.png").getFile()));
		Image lot3 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("lot2.png").getFile()));
		ImageView v1 = new ImageView(lot1);
		ImageView v2 = new ImageView(lot2);
		ImageView v3 = new ImageView(lot3);
		v1.setFitWidth(275);
		v1.setFitHeight(120);
		v2.setFitWidth(275);
		v2.setFitHeight(110);
		v3.setFitWidth(275);
		v3.setFitHeight(120);
		root = new Pane();
		root.setPrefSize(400, 300);
		VBox menu = new VBox();
		menu.setId("menu");
		menu.prefHeightProperty().bind(root.heightProperty());
		menu.setPrefWidth(200);

		menu.getChildren().addAll(v1, v2, v3);
		menu.setPadding(new Insets(20, 0, 20, 0));
		menu.setTranslateX(-190);
		TranslateTransition menuTranslation = new TranslateTransition(Duration.millis(300), menu);

		menuTranslation.setFromX(-190);
		menuTranslation.setToX(0);
		menu.setOnMouseEntered(evt -> {
			menuTranslation.setRate(1);
			menuTranslation.play();
			v1.setOnMouseClicked(e -> {
				try {
					lotChoice(v1, lot1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			v2.setOnMouseClicked(e -> {
				try {
					lotChoice(v2, lot2);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			v3.setOnMouseClicked(e -> {
				try {
					lotChoice(v3, lot3);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
		});
		menu.setOnMouseExited(evt -> {
			menuTranslation.setRate(-1);
			menuTranslation.play();
		});
		root.setLayoutY(50);
		root.getChildren().add(menu);
		center.getChildren().add(root);

	}

	// MENU ON RIGHT SIDE OF (center) PANE
	// DISPLAYS CARS AND CALLS dragAndDrop()
	public void rightMenu() throws FileNotFoundException {
		Image car1 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("car1.png").getFile()));
		Image car2 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("car2.png").getFile()));
		Image car3 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("car3.png").getFile()));
		Image car4 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("car4.png").getFile()));
		Image car5 = new Image(new FileInputStream(this.getClass().getClassLoader().getResource("car5.png").getFile()));
		Image[] c = { car1, car2, car3, car4, car5};
		for (Image i : c) {
			ImageView v = new ImageView(i);
			center.getChildren().add(v);
			cars.add(v);
			v.setLayoutX(1200);
			v.setLayoutY(yCoord);
			v.setFitHeight(200);
			v.setFitWidth(100);
			dragAndDrop(v, yCoord);
			yCoord += 125;
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
