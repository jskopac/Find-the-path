
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

public class PathFinder extends Application {

    private ListGraph<Location> locationGraph = new ListGraph<>();

    private Location fromLocation;
    private Location toLocation;
    private Pane outputPane;
    private Image image;
    private ImageView imageView;
    private Button newPlace;
    private TextField nameField;
    private TextField timeField;
    private VBox topVBox;
    private Stage primaryStage;
    private Boolean unsavedChanges = false;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        primaryStage.setTitle("PathFinder");
        BorderPane root = new BorderPane();
        outputPane = new Pane();
        outputPane.setId("outputArea");
        imageView = new ImageView();
        image = new Image("file:europa.gif");
        Scene scene = new Scene(root, 600, 100);

        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        menuBar.setId("menu");

        Menu menuFile = new Menu("File");
        menuFile.setId("menuFile");

        MenuItem newMap = new MenuItem("New map");
        newMap.setOnAction(new NewMapHandler());
        newMap.setId("menuNewMap");

        MenuItem open = new MenuItem("Open");
        open.setOnAction(new OpenFileHandler());
        open.setId("menuOpenFile");

        MenuItem save = new MenuItem("Save");
        save.setOnAction(new SaveFileHandler());
        save.setId("menuSaveFile");

        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setOnAction(new SaveImageHandler());
        saveImage.setId("menuSaveImage");

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(new ExitHandler());
        exit.setId("menuExit");

        menuFile.getItems().addAll(newMap, open, save, saveImage, exit);
        menuBar.getMenus().add(menuFile);

        Button findPath = new Button("Find Path");
        findPath.setOnMouseClicked(new FindPathHandler());
        findPath.setId("btnFindPath");

        Button showConnection = new Button("Show Connection");
        showConnection.setOnMouseClicked(new ShowConnectionHandler());
        showConnection.setId("btnShowConnection");

        newPlace = new Button("New Place");
        newPlace.setOnAction(new NewPlaceHandler());
        newPlace.setId("btnNewPlace");

        Button newConnection = new Button("New Connection");
        newConnection.setOnMouseClicked(new NewConnectionHandler());
        newConnection.setId("btnNewConnection");

        Button changeConnection = new Button("Change Connection");
        changeConnection.setOnMouseClicked(new ChangeConnectionHandler());
        changeConnection.setId("btnChangeConnection");

        HBox bottom = new HBox();
        bottom.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        bottom.setSpacing(5);

        topVBox = new VBox();
        topVBox.getChildren().addAll(menuBar, bottom);
        root.setTop(topVBox);
        root.setCenter(outputPane);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(new CloseWindowHandler());
        primaryStage.show();
    }

    private void parseLocations(BufferedReader reader) throws IOException {
        String nodeDetails = reader.readLine();
        String[] nodeDetailsArray = nodeDetails.split(";");

        for (int i = 0; i < nodeDetailsArray.length - 1; i += 3) {
            String name = nodeDetailsArray[i];
            double x = Double.parseDouble(nodeDetailsArray[i + 1]);
            double y = Double.parseDouble(nodeDetailsArray[i + 2]);

            Location newLocation = new Location(x, y, 8);
            newLocation.setName(name);
            newLocation.setId(newLocation.getName());

            locationGraph.add(newLocation);
            newLocation.setOnMouseClicked(new MarkLocationHandler());

            outputPane.getChildren().add(newLocation);
        }
    }

    private void parseEdges(String edgeDetails) throws IOException {
        String[] edgeDetailsArray = edgeDetails.split(";");

        Location nodeA = getLocation(edgeDetailsArray[0]);
        Location nodeB = getLocation(edgeDetailsArray[1]);
        String edgeName = edgeDetailsArray[2];
        int weight = Integer.parseInt(edgeDetailsArray[3]);

        if (nodeA != null && nodeB != null) {
            if (locationGraph.getEdgeBetween(nodeA, nodeB) == null) {
                drawLineAndConnect(nodeA, nodeB, edgeName, weight);
            }
        }
    }

    private Location getLocation(String name) {
        for (Location location : locationGraph.getNodes()) {
            if (location != null) {
                if (location.getName().equals(name)) {
                    return location;
                }
            }
        }
        return null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void drawLineAndConnect(Location from, Location to, String name, int time) {
        if (locationGraph.getEdgeBetween(from, to) == null) {
            locationGraph.connect(from, to, name, time);
            Line line = new Line(from.getCenterX(),
                    from.getCenterY(),
                    to.getCenterX(),
                    to.getCenterY());
            line.setStroke(Color.BLUE);
            line.setDisable(true);
            outputPane.getChildren().add(line);
        }
    }

    private void showEdgeConfirmation(TextField name, TextField time) {
        Alert getConnection = new Alert(Alert.AlertType.CONFIRMATION);
        getConnection.setTitle("Connection");
        getConnection.setHeaderText("Connection from " + fromLocation + " to " + toLocation);
        GridPane grid = new GridPane();
        grid.addRow(0, new Label("Name:"), name);
        grid.addRow(1, new Label("Time:"), time);
        grid.setHgap(10);
        getConnection.getDialogPane().setContent(grid);
        getConnection.showAndWait();
    }

    private boolean unsavedChanges(Event event) {
        if (unsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Unsaved changes, continue?");
            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get().equals(ButtonType.CANCEL))
                return false;
        }
        return true;
    }

    private void clearOutputPane() {
        fromLocation = null;
        toLocation = null;
        unsavedChanges = true;
        locationGraph = new ListGraph<>();
        outputPane.getChildren().clear();
    }

    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            if (unsavedChanges(e)) {
                clearOutputPane();
                imageView.setImage(image);
                outputPane.getChildren().add(imageView);
                primaryStage.setHeight(topVBox.getHeight() + image.getHeight());
                primaryStage.setWidth(image.getWidth());
                unsavedChanges = true;
            }
        }
    }

    class OpenFileHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                if (unsavedChanges(actionEvent)) {
                    clearOutputPane();
                    BufferedReader reader = new BufferedReader(new FileReader("europa.graph"));
                    String imageUrl = reader.readLine();

                    image = new Image(imageUrl);
                    imageView.setImage(image);
                    outputPane.getChildren().add(imageView);
                    primaryStage.setHeight(topVBox.getHeight() + image.getHeight());
                    primaryStage.setWidth(image.getWidth());

                    parseLocations(reader);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        parseEdges(line);
                    }
                    unsavedChanges = true;
                    reader.close();
                }
            } catch (IOException e) {
                showError("File not found!");
            }
        }
    }

    class SaveFileHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("europa.graph"));
                PrintWriter printWriter = new PrintWriter(bufferedWriter);

                printWriter.println("file:europa.gif");
                for (Location location : locationGraph.getNodes()) {
                    printWriter.print(location.getName() + ";" + location.getCenterX() + ";" + location.getCenterY() + ";");
                }
                printWriter.println();

                for (Location location : locationGraph.getNodes()) {
                    for (Edge edge : locationGraph.getEdgesFrom(location)) {
                        printWriter.print(location.getName() + ";" + edge.getDestination() + ";" + edge.getName() + ";" + edge.getWeight() + "\n");
                    }
                }
                unsavedChanges = false;
                printWriter.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                WritableImage image = outputPane.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    class FindPathHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (fromLocation != null && toLocation != null) {
                TextArea textArea = new TextArea();
                Alert showPath = new Alert(Alert.AlertType.CONFIRMATION);
                showPath.setTitle("Find Path");
                showPath.setHeaderText("Path from " + fromLocation + " to " + toLocation);
                GridPane grid = new GridPane();
                grid.addRow(0, textArea);
                showPath.getDialogPane().setContent(grid);

                int totalTime = 0;
                for (Edge edge : locationGraph.getPath(fromLocation, toLocation)) {
                    textArea.appendText(edge.toString() + "\n");
                    totalTime += edge.getWeight();
                }
                textArea.appendText("Total " + totalTime);
                textArea.setEditable(false);

                showPath.showAndWait();
            } else {
                showError("Two places must be selected!");
            }
        }
    }

    class ShowConnectionHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (fromLocation != null && toLocation != null) {
                try {
                    Edge edgeBetween = locationGraph.getEdgeBetween(fromLocation, toLocation);
                        nameField.setEditable(false);
                        timeField.setEditable(false);
                        nameField.setText(edgeBetween.getName());
                        timeField.setText(String.valueOf(edgeBetween.getWeight()));
                        showEdgeConfirmation(nameField, timeField);
                } catch (NullPointerException e) {
                    showError("There is no connection between the locations!");
                }
            } else {
                showError("Two places must be selected!");
            }
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            if (!outputPane.getChildren().isEmpty()) {
                outputPane.setCursor(Cursor.CROSSHAIR);
                newPlace.setDisable(true);
                outputPane.setOnMouseClicked(new MapClickHandler());
                unsavedChanges = true;
            }
        }
    }

    class ChangeConnectionHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (fromLocation != null && toLocation != null) {
                try {
                    Edge fromAtoB = locationGraph.getEdgeBetween(fromLocation, toLocation);
                    Edge fromBtoA = locationGraph.getEdgeBetween(toLocation, fromLocation);

                    nameField.setText(fromAtoB.getName());
                    nameField.setEditable(false);
                    timeField = new TextField();

                    Alert getConnection = new Alert(Alert.AlertType.CONFIRMATION);
                    getConnection.setHeaderText("Connection from " + fromLocation + " to " + toLocation);
                    getConnection.setTitle("Connection");
                    GridPane grid = new GridPane();
                    grid.addRow(0, new Label("Name:"), nameField);
                    grid.addRow(1, new Label("Time:"), timeField);
                    grid.setHgap(10);
                    getConnection.getDialogPane().setContent(grid);
                    getConnection.showAndWait();

                    int time = Integer.parseInt(timeField.getText());

                    fromBtoA.setWeight(time);
                    fromAtoB.setWeight(time);
                    unsavedChanges = true;
                } catch (NumberFormatException e) {
                    showError("'Time' field cannot be empty");
                } catch (NullPointerException e) {
                    showError("There is no connection between the locations!");
                }
            } else {
                showError("Two places must be selected!");
            }
        }
    }

    class ExitHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class CloseWindowHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            if (!unsavedChanges(event))
                event.consume();
        }
    }

    class MapClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();

            Alert getName = new Alert(Alert.AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            TextField nameField = new TextField();
            grid.addRow(0, new Label("Name of Place:"), nameField);
            grid.setHgap(10);
            getName.getDialogPane().setContent(grid);
            getName.showAndWait();

            if (nameField.getText().isEmpty()) {
                showError("Location name can't be empty");
                newPlace.setDisable(false);
            } else {
                Location location = new Location(x, y, 8);
                location.setOnMouseClicked(new MarkLocationHandler());

                location.setName(nameField.getText());
                locationGraph.add(location);
                location.setId(location.getName());

                outputPane.getChildren().add(location);
                outputPane.setOnMouseClicked(null);
                newPlace.setDisable(false);
                outputPane.setCursor(Cursor.DEFAULT);
            }
        }
    }

    class MarkLocationHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Location source = (Location) event.getSource();

            if (fromLocation == null) {
                fromLocation = source;
                fromLocation.markedLocation();
            } else if (toLocation == null && source != fromLocation) {
                toLocation = source;
                toLocation.markedLocation();
            } else if (source == fromLocation) {
                fromLocation.unmarkedLocation();
                if (toLocation != null) {
                    fromLocation = toLocation;
                    toLocation = null;
                } else {
                    fromLocation = null;
                }
            } else if (source == toLocation) {
                toLocation.unmarkedLocation();
                toLocation = null;
            }

        }
    }

    class NewConnectionHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (fromLocation != null && toLocation != null) {
                try {
                    if (locationGraph.getEdgeBetween(fromLocation, toLocation) != null) {
                        showError("There is already an edge between selected locations!");
                        return;
                    }
                    nameField = new TextField();
                    timeField = new TextField();
                    showEdgeConfirmation(nameField, timeField);


                    if (nameField.getText().isBlank()) {
                        showError("Name field is empty!");
                        return;
                    }

                    int time = Integer.parseInt(timeField.getText());

                    drawLineAndConnect(fromLocation, toLocation, nameField.getText(), time);
                    unsavedChanges = true;
                } catch (NumberFormatException exception) {
                    showError("Time not numerical!");
                }
            } else {
                showError("Two places must be selected!");
            }
        }
    }
}