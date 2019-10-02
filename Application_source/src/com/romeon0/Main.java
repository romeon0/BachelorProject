package com.romeon0;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Pair;

import java.awt.Point;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

enum WINDOW_TYPE {
	TRAINING, TESTING, MAIN, RECOGNIZE
}

public class Main extends Application implements Observer {
	// menu
	private MenuItem menuiNewNetwork;
	private MenuItem menuiLoadNetwork;
	private MenuItem menuiLoadNetworkFrom;
	private MenuItem menuiSaveNetwork;
	private MenuItem menuiExit;
	// recognition window
	private Canvas cnvRecDrawField;
	private Button btnRecAddLetter;
	private Button btnRecUndo;
	private Button btnRecRedo;
	private Button btnRecEraser;
	private Button btnRecPencil;
	private Button btnRecClearCanvas;
	private Button btnRecIdentify;
	private Label lblRecNetworkAnswer;
	private Label lblRecProbLetter1;
	private Label lblRecProbLetter2;
	private Label lblRecProbLetter3;
	private Label lblRecProbLetter4;
	private HBox hboxRecLetters;
	private Button btnRecForward;
	private Button btnRecBackward;
	private Pane paneDrawing;
	private Label lblRecRecognizedLetter;
	private Label lblRecMostProbLetters;
	private Label lblRecDrawLetter;
	// train window
	private LineChart<Integer, Double> chartTrainError;
	private TextField tfTrainMaxError;
	private TextField tfTrainNrEpochs;
	private TextField tfTrainLearningRate;
	private Button btnTrainStart;
	private Button btnTrainStop;
	private Button btnTrainForward;
	private Button btnTrainBackward;
	private Label lblTrainCurrEpoch;
	private Label lblTrainCurrEpochError;
	private Button btnTrainMaxErrorUp;
	private Button btnTrainMaxErrorDown;
	private Button btnTrainNrEpochsUp;
	private Button btnTrainNrEpochsDown;
	private Button btnTrainLearnRateUp;
	private Button btnTrainLearnRateDown;
	private Label lblTrainMaxError;
	private Label lblTrainNrEpochs;
	private Label lblTrainLearningRate;
	// testing
	private TableView tvTestExamples;
	private Button btnTestStart;
	private Button btnTestStop;
	private Button btnTestForward;
	private Button btnTestBackward;
	private Label lblTestCorrectAnswers;
	// other
	private GraphicsContext gc;
	private Stage windowStage;
	private Network network;
	private WritableImage imgWithoutSegments;
	private boolean segmented = false;
	private Vector<Figure> figures;
	private boolean dragStarted = true;
	private Figure figure;
	private int editPosition = -1;// for undo / redo functionality
	private int editMode = 0;// for pencil/eraser functionality
	private Vector<WritableImage> letters;
	private Scene mainScene, trainingScene, testingScene, recognScene, currentScene;
	private Stage loadingStage;
	private Language lang;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// PrintStream out = new PrintStream(new FileOutputStream("console_file.txt"));
		// System.setOut(out);

		windowStage = primaryStage;
		// ResourceBundle resourceBundle = ResourceBundle.getBundle("ro",new
		// Locale("ro"));
		// .getClassLoader()
		Parent mainRoot = FXMLLoader.load(getClass().getResource("/xmls/window_main.fxml"));
		Parent trainingRoot = FXMLLoader.load(getClass().getResource("/xmls/window_training.fxml"));
		Parent testingRoot = FXMLLoader.load(getClass().getResource("/xmls/window_testing.fxml"));
		Parent recognRoot = FXMLLoader.load(getClass().getResource("/xmls/window_recognition.fxml"));

		mainScene = new Scene(mainRoot, 870, 520);
		trainingScene = new Scene(trainingRoot, 870, 520);
		testingScene = new Scene(testingRoot, 870, 520);
		recognScene = new Scene(recognRoot, 870, 520);
		currentScene = mainScene;
		

		lang = new Language();

		windowStage = primaryStage;
		getElementIds();
		// settings
		gc = cnvRecDrawField.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.clearRect(0, 0, cnvRecDrawField.getWidth(), cnvRecDrawField.getHeight());
		gc.setLineWidth(1);
		figures = new Vector<>();
		letters = new Vector<>();
		updateLanguage("ro", WINDOW_TYPE.MAIN);
		setListeners();
		getElementIds();
		initMenuBar(WINDOW_TYPE.MAIN);

		primaryStage.setTitle("Handwriting Character Recognition Network");
		primaryStage.setScene(currentScene);
		Image icon = new Image(new File("./files/images/logo_icon.png").toURI().toURL().toString());
		primaryStage.getIcons().add(icon);
		primaryStage.setResizable(false);

		primaryStage.show();
	}

	// initializing
	private void setListeners() {
		// Recognition----------------
		// canvas listeners
		cnvRecDrawField.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
			if (!segmented && dragStarted) {
				figure.addPoint((int) e.getX(), (int) e.getY());
				if (editMode == 0)
					gc.fillRect(e.getX(), e.getY(), 5, 5);
				else if (editMode == 1)
					gc.clearRect(e.getX(), e.getY(), 5, 5);
			}
		});
		cnvRecDrawField.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				currentScene.setCursor(Cursor.HAND);
			}
		});
		cnvRecDrawField.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				currentScene.setCursor(Cursor.DEFAULT);
			}
		});
		cnvRecDrawField.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				dragStarted = true;
				figure = new Figure(editMode);
				figure.addPoint((int) event.getX(), (int) event.getY());
				if (editMode == 0)
					gc.fillRect(event.getX(), event.getY(), 3, 3);
				else
					gc.clearRect(event.getX(), event.getY(), 3, 3);
			}
		});
		cnvRecDrawField.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				figures.add(figures.size(), figure);
				editPosition += 1;
				btnRecUndo.setDisable(true);
				btnRecRedo.setDisable(false);
			}
		});
		btnRecIdentify.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				List<double[][]> imageMatrices = new ArrayList<>();
				if (hboxRecLetters.getChildren().size() != 0) {
					Helper h = new Helper();
					for (Node n : hboxRecLetters.getChildren()) {
						WritableImage snapshot = ((ImageView) n).snapshot(null, null);
						Image image = h.to28x28(snapshot);
						double[][] imgMatrix = h.imageToMatrix(image);
						imageMatrices.add(imgMatrix);
					}
					hboxRecLetters.getChildren().clear();

				} else {
					Helper h = new Helper();
					WritableImage snapshot = cnvRecDrawField.snapshot(null, null);
					Image image = h.to28x28(snapshot);
					double[][] imgMatrix = h.imageToMatrix(image);
					imageMatrices.add(imgMatrix);
				}
				List<Pair<Double, String>> letters = network.startRecognizing(imageMatrices);

				String type = "";
				String mostProbably = letters.get(0).getValue();
				if (letters.get(0).getValue().length() > 1) {// word
					type = lang.get("WORD") + " '";
				} else// character
					type = lang.get("LETTER") + " '";
				type = type.trim();

				lblRecNetworkAnswer.setText(mostProbably);
				lblRecProbLetter1.setText(
						type + letters.get(0).getValue() + "': " + String.format("%.6f", letters.get(0).getKey()));
				lblRecProbLetter2.setText(
						type + letters.get(1).getValue() + "': " + String.format("%.6f", letters.get(1).getKey()));
				lblRecProbLetter3.setText(
						type + letters.get(2).getValue() + "': " + String.format("%.6f", letters.get(2).getKey()));
				lblRecProbLetter4.setText(
						type + letters.get(3).getValue() + "': " + String.format("%.6f", letters.get(3).getKey()));
				btnRecAddLetter.setDisable(false);
			}
		});
		btnRecAddLetter.setOnAction((ActionEvent event) -> {
			if (hboxRecLetters.getChildren().size() == 9) {
				btnRecAddLetter.setDisable(true);
			}
			Helper h = new Helper();
			WritableImage imgNewLetter = cnvRecDrawField.snapshot(null, null);
			letters.add(imgNewLetter);
			ImageView ivNewLetter = new ImageView(imgNewLetter);
			hboxRecLetters.getChildren().add(ivNewLetter);

			figures.removeAllElements();
			editMode = 0;
			editPosition = -1;
			btnRecRedo.setDisable(true);
			btnRecUndo.setDisable(true);
			segmented = false;
			gc.clearRect(0, 0, cnvRecDrawField.getWidth(), cnvRecDrawField.getHeight());
		});
		btnRecBackward.setOnAction((ActionEvent event) -> {
			onExitRecognizingWindow();
			onEnterTestingWindow();
		});
		// drawing
		btnRecClearCanvas.setOnAction((ActionEvent event) -> {
			figures.removeAllElements();
			editMode = 0;
			editPosition = -1;
			gc.clearRect(0, 0, cnvRecDrawField.getWidth(), cnvRecDrawField.getHeight());
			btnRecRedo.setDisable(true);
			btnRecUndo.setDisable(true);
			segmented = false;
		});
		btnRecRedo.setOnAction((ActionEvent event) -> {
			if (editPosition >= 0) {
				btnRecUndo.setDisable(false);
				Figure f = figures.get(editPosition);
				if (f.getMode() == 1)
					for (Point p : f.getPoints()) {
						gc.fillRect(p.x, p.y, 5, 5);
					}
				else if (f.getMode() == 0)
					for (Point p : f.getPoints()) {
						gc.clearRect(p.x, p.y, 5, 5);
					}
				editPosition -= 1;
			}

			if (editPosition < 0)
				btnRecRedo.setDisable(true);
		});
		btnRecUndo.setOnAction((ActionEvent event) -> {
			if (editPosition < figures.size() - 1) {
				btnRecRedo.setDisable(false);
				editPosition += 1;
				Figure f = figures.get(editPosition);
				if (f.getMode() == 0)
					for (Point p : f.getPoints()) {
						gc.fillRect(p.x, p.y, 5, 5);
					}
				else if (f.getMode() == 1)
					for (Point p : f.getPoints()) {
						gc.clearRect(p.x, p.y, 5, 5);
					}
			}
			if (editPosition == figures.size() - 1)
				btnRecUndo.setDisable(true);
		});
		btnRecEraser.setOnAction((ActionEvent event) -> editMode = 1);
		btnRecPencil.setOnAction((ActionEvent event) -> editMode = 0);
		// ----------------

		// training---------------------
		XYChart.Series series = new XYChart.Series();
		series.setName(lang.get("TRAINCHART_INFO"));
		chartTrainError.getData().add(series);
		btnTrainStart.setOnAction((e) -> {
			network.startTraining("files/datasets/trainset.dataset");
		});
		btnTrainStop.setOnAction((e) -> {
			network.stopTraining();
		});
		btnTrainForward.setOnAction((e) -> {
			boolean confirmExit=true;
			if(network.isRunningTraining()) {
				confirmExit = showConfirmDialog(lang.get("CONFIRMEXIT_TRAIN"));
			}
			
			if(confirmExit) {
				onExitTrainingWindow();
				onEnterTestingWindow();
			}
		});
		btnTrainMaxErrorUp.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			double value = Double.parseDouble(tfTrainMaxError.getText());
			value += 0.005;
			tfTrainMaxError.setText(value + "");
		});
		btnTrainMaxErrorDown.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			double value = Double.parseDouble(tfTrainMaxError.getText());
			value -= 0.005;
			tfTrainMaxError.setText(value + "");
		});
		btnTrainNrEpochsUp.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			int value = Integer.parseInt(tfTrainNrEpochs.getText());
			value += 10;
			tfTrainNrEpochs.setText(value + "");
		});
		btnTrainNrEpochsDown.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			int value = Integer.parseInt(tfTrainNrEpochs.getText());
			value -= 10;
			tfTrainNrEpochs.setText(value + "");
		});
		btnTrainLearnRateUp.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			double value = Double.parseDouble(tfTrainLearningRate.getText());
			value += 0.002;
			tfTrainLearningRate.setText(value + "");
		});
		btnTrainLearnRateDown.setOnAction((e) -> {
			if (network.isRunningTraining())
				return;
			double value = Double.parseDouble(tfTrainLearningRate.getText());
			value -= 0.002;
			tfTrainLearningRate.setText(String.format("%.10f", value));
		});
		// -----------------------------

		// testing------
		ObservableList<TableColumn> columns = tvTestExamples.getColumns();
		TableColumn numberColumn = columns.get(0);
		TableColumn actualAnswerColumn = columns.get(1);
		TableColumn desiredAnswerColumn = columns.get(2);
		numberColumn.setCellValueFactory(new PropertyValueFactory<TestingTableDataModel, Integer>("number"));
		actualAnswerColumn.setCellValueFactory(new PropertyValueFactory<TestingTableDataModel, String>("actualAnswer"));
		desiredAnswerColumn
				.setCellValueFactory(new PropertyValueFactory<TestingTableDataModel, String>("desiredAnswer"));
		btnTestStart.setOnAction((e) -> {
			network.startTesting("files/datasets/testset.dataset");
		});
		btnTestStop.setOnAction((e) -> {
			network.stopTesting();
		});
		btnTestForward.setOnAction((e) -> {
			onExitTestingWindow(1);
			onEnterRecognizingWindow();
		});
		btnTestBackward.setOnAction((e) -> {		
			onExitTestingWindow(2);
			onEnterTrainingWindow();
		});

		// -------------

		windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (network != null)
					network.destroy();
			}
		});
	}

	private void getElementIds() {

		// ----------get GUI elements
		// recognition
		Parent trainingRoot = trainingScene.getRoot();
		Parent testingRoot = testingScene.getRoot();
		Parent recognRoot = recognScene.getRoot();
		Parent mainRoot = mainScene.getRoot();
		paneDrawing = (Pane) recognRoot.lookup("#paneDrawing");
		lblRecNetworkAnswer = (Label) recognRoot.lookup("#lblNetworkAnswer");
		lblRecProbLetter1 = (Label) recognRoot.lookup("#lblProbLetter1");
		lblRecProbLetter2 = (Label) recognRoot.lookup("#lblProbLetter2");
		lblRecProbLetter3 = (Label) recognRoot.lookup("#lblProbLetter3");
		lblRecProbLetter4 = (Label) recognRoot.lookup("#lblProbLetter4");
		cnvRecDrawField = (Canvas) paneDrawing.lookup("#cnvDrawField");
		btnRecClearCanvas = (Button) paneDrawing.lookup("#btnClearCanvas");
		btnRecUndo = (Button) paneDrawing.lookup("#btnUndo");
		btnRecRedo = (Button) paneDrawing.lookup("#btnRedo");
		btnRecEraser = (Button) paneDrawing.lookup("#btnEraser");
		btnRecPencil = (Button) paneDrawing.lookup("#btnPencil");
		btnRecAddLetter = (Button) paneDrawing.lookup("#btnAddLetter");
		btnRecIdentify = (Button) recognRoot.lookup("#btnIdentify");
		btnRecBackward = (Button) recognRoot.lookup("#btnBackward");
		hboxRecLetters = (HBox) (((ScrollPane) recognRoot.lookup("#paneLetters")).getContent());
		lblRecRecognizedLetter = (Label) recognRoot.lookup("#lblRecognizedLetter");
		lblRecMostProbLetters = (Label) recognRoot.lookup("#lblMostProbLetters");
		lblRecDrawLetter  = (Label) paneDrawing.lookup("#lblDrawLetter");
		// train
		chartTrainError = (LineChart<Integer, Double>) trainingRoot.lookup("#chartError");
		tfTrainMaxError = (TextField) trainingRoot.lookup("#tfMaxError");
		tfTrainNrEpochs = (TextField) trainingRoot.lookup("#tfNrEpochs");
		tfTrainLearningRate = (TextField) trainingRoot.lookup("#tfLearningRate");
		btnTrainStart = (Button) trainingRoot.lookup("#btnStart");
		btnTrainStop = (Button) trainingRoot.lookup("#btnStop");
		btnTrainForward = (Button) trainingRoot.lookup("#btnForward");
		btnTrainBackward = (Button) trainingRoot.lookup("#btnBackward");
		lblTrainCurrEpoch = (Label) trainingRoot.lookup("#lblCurrEpoch");
		lblTrainCurrEpochError = (Label) trainingRoot.lookup("#lblCurrEpochError");
		btnTrainMaxErrorUp = (Button) trainingRoot.lookup("#btnMaxErrorUp");
		btnTrainMaxErrorDown = (Button) trainingRoot.lookup("#btnMaxErrorDown");
		btnTrainNrEpochsUp = (Button) trainingRoot.lookup("#btnNrEpochsUp");
		btnTrainNrEpochsDown = (Button) trainingRoot.lookup("#btnNrEpochsDown");
		btnTrainLearnRateUp = (Button) trainingRoot.lookup("#btnLearnRateUp");
		btnTrainLearnRateDown = (Button) trainingRoot.lookup("#btnLearnRateDown");
		lblTrainMaxError = (Label) trainingRoot.lookup("#lblMaxError");
		lblTrainNrEpochs = (Label) trainingRoot.lookup("#lblNrEpochs");
		lblTrainLearningRate = (Label) trainingRoot.lookup("#lblLearningRate");
		// testing
		tvTestExamples = (TableView) testingRoot.lookup("#tvExamples");
		btnTestStart = (Button) testingRoot.lookup("#btnStart");
		btnTestStop = (Button) testingRoot.lookup("#btnStop");
		btnTestForward = (Button) testingRoot.lookup("#btnForward");
		btnTestBackward = (Button) testingRoot.lookup("#btnBackward");
		lblTestCorrectAnswers = (Label) testingRoot.lookup("#lblCorrectAnswers");
		// ------------
	}

	private void initMenuBar(WINDOW_TYPE type) {
		Parent root = currentScene.getRoot();
		MenuBar menuBar = (MenuBar) root.lookup("#menuBar");
		ObservableList<Menu> menus = menuBar.getMenus();
		Menu menuFile = menus.get(0);
		Menu menuSettings = menus.get(1);
		Menu menuHelp = menus.get(2);
		ObservableList<MenuItem> fileMenuItems = menuFile.getItems();
		Menu menuLanguage = (Menu) menuSettings.getItems().get(0);
		ObservableList<MenuItem> helpMenuItems = menuHelp.getItems();
		menuiNewNetwork = fileMenuItems.get(0);
		menuiLoadNetwork = fileMenuItems.get(1);
		menuiLoadNetworkFrom = fileMenuItems.get(2);
		menuiSaveNetwork = fileMenuItems.get(3);
		menuiExit = fileMenuItems.get(4);
		MenuItem menuiHelp = helpMenuItems.get(0);
		MenuItem menuiAbout = helpMenuItems.get(1);
		// update menus
		menuFile.setText(lang.get("MENU_FILE"));
		menuSettings.setText(lang.get("MENU_SETTINGS"));
		menuHelp.setText(lang.get("MENU_HELP"));
		// update submenus/options
		menuiSaveNetwork.setText(lang.get("SAVE_NET"));
		menuiLoadNetwork.setText(lang.get("LOAD_NET"));
		menuiLoadNetworkFrom.setText(lang.get("LOAD_NET_FROM"));
		menuiNewNetwork.setText(lang.get("NEW_NET"));
		menuiExit.setText(lang.get("EXIT"));
		(menuLanguage).setText(lang.get("LANGUAGE"));
		menuiHelp.setText(lang.get("HELP"));
		menuiAbout.setText(lang.get("ABOUT"));

		MenuItem menuiLangRo = menuLanguage.getItems().get(0);
		MenuItem menuiLangEng = menuLanguage.getItems().get(1);
		MenuItem menuiLangHun = menuLanguage.getItems().get(2);
		MenuItem menuiLangRus = menuLanguage.getItems().get(3);

		menuiHelp.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showHelpWindow();
			}
		});
		menuiAbout.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showAboutWindow();
			}
		});

		menuiLangRo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateLanguage("ro", type);
			}
		});
		menuiLangEng.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateLanguage("eng", type);
			}
		});
		menuiLangHun.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateLanguage("hun", type);
			}
		});
		menuiLangRus.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateLanguage("rus", type);
			}
		});

		// set menu Bar listeners
		menuiNewNetwork.setOnAction((e) -> {
			try {
				if(network!=null)
					network.destroy();
				currentScene = trainingScene;
				network = new Network(currentScene.getRoot());
				network.addObserver(Main.this);

				windowStage.setScene(currentScene);
				initMenuBar(type);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		});
		menuiLoadNetwork.setOnAction((e) -> {
			if(network!=null)
				network.destroy();
			File weightsFile = new File("./files/network/memory.weights");
			currentScene = recognScene;
			network = new Network(currentScene.getRoot());
			network.addObserver(Main.this);
			boolean loaded = network.load(weightsFile);
			if (!loaded) {
				showErrorDialog(lang.get("ERR_WEIGHTSNOTLOADED"));
				return;
			}
			windowStage.setScene(currentScene);
			initMenuBar(type);
		});
		menuiLoadNetworkFrom.setOnAction((e) -> {
			if(network!=null)
				network.destroy();
			File weightsFile = showOpenDialog(windowStage, "Network weights", "*.weights");
			if (weightsFile != null) {
				currentScene = recognScene;
				network = new Network(currentScene.getRoot());
				network.addObserver(Main.this);
				boolean loaded = network.load(weightsFile);
				if (!loaded) {
					showErrorDialog(lang.get("ERR_WEIGHTSNOTLOADED"));
					return;
				}
				windowStage.setScene(currentScene);
				initMenuBar(type);
			}
		});
		menuiSaveNetwork.setOnAction((e) -> {
			File weightsFile = showSaveDialog(windowStage, "Network weights", "*.weights");
			if (weightsFile != null) {
				try {
					if (!weightsFile.exists())
						weightsFile.createNewFile();
					network.save(weightsFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		menuiExit.setOnAction((e) -> {
			windowStage.close();
		});

		if (WINDOW_TYPE.MAIN == type) {
			menuiSaveNetwork.setDisable(true);
		}
	}

	// fake events
	private void onStartingTraining() {
		btnTrainStart.setDisable(true);
		btnTrainStop.setDisable(false);
		tfTrainLearningRate.setDisable(true);
		tfTrainNrEpochs.setDisable(true);
		tfTrainMaxError.setDisable(true);
		int nrEpochs = Integer.parseInt(tfTrainNrEpochs.getText());
		double maxError = Double.parseDouble(tfTrainMaxError.getText());
		double learningRate = Double.parseDouble(tfTrainLearningRate.getText());
		network.setLearningRate(learningRate);
		network.setMaxTrainingError(maxError);
		network.setNrEpochs(nrEpochs);
		menuiSaveNetwork.setDisable(true);
	}

	private void onStoppingTraining() {
		btnTrainStart.setDisable(false);
		btnTrainStop.setDisable(true);
		tfTrainLearningRate.setDisable(false);
		tfTrainNrEpochs.setDisable(false);
		tfTrainMaxError.setDisable(false);
		menuiSaveNetwork.setDisable(false);
	}
	private void onStoppingTesting() {
		btnTestStop.setDisable(true);
		btnTestStart.setDisable(false);
	}
	private void onStartingTesting() {
		btnTestStop.setDisable(false);
		btnTestStart.setDisable(true);
	}

	private void onExitTrainingWindow() {		
		network.stopTraining();
		currentScene = testingScene;
		windowStage.setScene(currentScene);
	}

	private void onExitTestingWindow(int side) {
		boolean exitResult=true;
		if(network.isRunningTesting()) {
			exitResult = showConfirmDialog(lang.get("CONFIRMEXIT_TEST"));
		}

		if(exitResult) {
			network.stopTesting();
			if (side == 1)
				currentScene = recognScene;
			else
				currentScene = trainingScene;
			
			if(side==1)
				onEnterRecognizingWindow();
			else
				onEnterTrainingWindow();
			windowStage.setScene(currentScene);
		}
	}

	private void onExitRecognizingWindow() {
		cnvRecDrawField.getGraphicsContext2D().clearRect(0, 0, cnvRecDrawField.getWidth(),
				cnvRecDrawField.getHeight());
		lblRecNetworkAnswer.setText("-");
		lblRecProbLetter1.setText(lang.get("LETTER") + " -: -");
		lblRecProbLetter2.setText(lang.get("LETTER") + " -: -");
		lblRecProbLetter3.setText(lang.get("LETTER") + " -: -");
		lblRecProbLetter4.setText(lang.get("LETTER") + " -: -");
		btnRecAddLetter.setDisable(false);
		hboxRecLetters.getChildren().clear();
		figures.clear();
		currentScene = trainingScene;
		windowStage.setScene(currentScene);
		onEnterTrainingWindow();
	}

	private void onEnterTrainingWindow() {
		initMenuBar(WINDOW_TYPE.TRAINING);
	}

	private void onEnterTestingWindow() {
		initMenuBar(WINDOW_TYPE.TESTING);
	}

	private void onEnterRecognizingWindow() {
		initMenuBar(WINDOW_TYPE.RECOGNIZE);
	}

	// dialogs and windows
	private File showOpenDialog(Stage parent, String desc, String... extensions) {
		Stage stage = new Stage();
		stage.setTitle(lang.get("SAVE") + "...");
		stage.initOwner(parent);
		stage.initModality(Modality.APPLICATION_MODAL);
		Group root = new Group();
		Scene scene = new Scene(root);
		stage.setScene(scene);

		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(desc, extensions);
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(extFilter);

		File chosenFile = fileChooser.showOpenDialog(parent);

		return chosenFile;
	}

	private File showSaveDialog(Stage parent, String desc, String... extensions) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(desc, extensions);
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(extFilter);

		File chosenFile = fileChooser.showSaveDialog(parent);
		return chosenFile;
	}

	private void showErrorDialog(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setContentText(message);
		alert.setHeaderText("");
		alert.setTitle(lang.get("SOMETHING_WRONG"));
		alert.showAndWait();

	}

	private boolean showConfirmDialog(String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText(message);
		alert.setHeaderText("");
		alert.setTitle(lang.get("CONFIRMATION"));

		String yesStr = lang.get("YES");
		String noStr = lang.get("NO");
		ButtonType btnYes = new ButtonType(yesStr);
		ButtonType btnNo = new ButtonType(noStr);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(btnYes);
		alert.getButtonTypes().add(btnNo);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get().getText().equals(yesStr)) {
			return true;
		}
		return false;
	}

	private void showInfoDialog(String message) {
		Alert info = new Alert(Alert.AlertType.INFORMATION);
		info.setContentText(message);
		info.setHeaderText("Info");
		info.showAndWait();
	}

	private void showLoadingWindow(String info) {
		Parent loadingRoot = null;
		try {
			loadingRoot = FXMLLoader.load(getClass().getResource("/xmls/window_loading.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		((Label) loadingRoot.lookup("#lblInfo")).setText(info);

		loadingStage = new Stage();
		loadingStage.initOwner(windowStage);
		loadingStage.initModality(Modality.APPLICATION_MODAL);
		loadingStage.initStyle(StageStyle.UNDECORATED);
		Scene scene = new Scene(loadingRoot);
		loadingStage.setScene(scene);
		loadingStage.show();
	}

	private void stopLoadingWindow() {
		loadingStage.close();
	}

	private void showAboutWindow() {
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/xmls/window_about.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Image image = null;
		try {
			image = new Image(new File("./files/images/logo.png").toURI().toURL().toString());
			((ImageView) root.lookup("#ivLogo")).setImage(image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Label lblDescription = (Label)root.lookup("#lblDescription");
		lblDescription.setText(lang.get("APP_DESC"));
		
		Stage stage = new Stage();
		stage.setResizable(false);
		stage.initOwner(windowStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(lang.get("ABOUT"));
		Image icon = null;
		try {
			icon = new Image(new File("./files/images/logo_icon.png").toURI().toURL().toString());
			stage.getIcons().add(icon);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	private void updateLanguage(String newLanguage, WINDOW_TYPE windowType) {
		String error = lang.load(newLanguage);
		if (error!=null) {
			showErrorDialog("Eroare in schimbarea limbii.");
		} else {
			// update menu bar
			initMenuBar(windowType);

			// recognizing
			btnRecClearCanvas.setText(lang.get("CLEAR"));
			btnRecUndo.setText(lang.get("UNDO"));
			btnRecRedo.setText(lang.get("REDO"));
			btnRecEraser.setText(lang.get("ERASER"));
			btnRecPencil.setText(lang.get("PENCIL"));
			btnRecAddLetter.setText(lang.get("ADDLETTER"));
			btnRecIdentify.setText(lang.get("IDENTIFY"));
			String type = lblRecNetworkAnswer.getText().length() > 1 ? lang.get("WORD") : lang.get("LETTER");
			type = type.replace(" ", "");
			String[] tmpArray1 = lblRecProbLetter1.getText().split(" ");
			for (int a = 0; a < tmpArray1.length; ++a) {
				System.out.println(a + ": " + tmpArray1[a]);
			}
			String[] tmpArray2 = lblRecProbLetter2.getText().split(" ");
			String[] tmpArray3 = lblRecProbLetter3.getText().split(" ");
			String[] tmpArray4 = lblRecProbLetter4.getText().split(" ");
			String prob1NewText = (type + " " + tmpArray1[1] + " " + tmpArray1[2]);
			String prob2NewText = (type + " " + tmpArray2[1] + " " + tmpArray2[2]);
			String prob3NewText = (type + " " + tmpArray3[1] + " " + tmpArray3[2]);
			String prob4NewText = (type + " " + tmpArray4[1] + " " + tmpArray4[2]);
			lblRecProbLetter1.setText(prob1NewText);
			lblRecProbLetter2.setText(prob2NewText);
			lblRecProbLetter3.setText(prob3NewText);
			lblRecProbLetter4.setText(prob4NewText);
			lblRecRecognizedLetter.setText(lang.get("REC_LETTER_OR_WORD"));
			lblRecMostProbLetters.setText(lang.get("REC_MOSTPROB_LETTERS"));
			btnRecBackward.setText("<" + lang.get("TRAINING"));
			lblRecDrawLetter.setText(lang.get("DRAW_LETTER"));

			// train
			chartTrainError.setTitle(lang.get("TRAINCHART_TITLE"));
			btnTrainStart.setText(lang.get("START"));
			btnTrainStop.setText(lang.get("STOP"));
			btnTrainForward.setText(lang.get("TESTING") + ">");
			if (chartTrainError.getData().size() != 0)
				chartTrainError.getData().get(0).setName(lang.get("TRAINCHART_INFO"));
			tmpArray1 = lblTrainCurrEpoch.getText().split(" ");
			tmpArray2 = lblTrainCurrEpochError.getText().split(" ");
			lblTrainCurrEpoch.setText(lang.get("EPOCH").trim() + ": " + tmpArray1[1].trim());//
			lblTrainCurrEpochError.setText(lang.get("EPOCH_ERR").trim() + ": " + tmpArray2[2].trim());//
			lblTrainMaxError.setText(lang.get("MAX_ERR"));
			lblTrainNrEpochs.setText(lang.get("NR_EPOCHS"));
			lblTrainLearningRate.setText(lang.get("LEARNING_RATE"));

			// testing
			btnTestStart.setText(lang.get("START"));
			btnTestStop.setText(lang.get("STOP"));
			lblTestCorrectAnswers.setText(lang.get("CORRECT_GUESSED") + ": -/-");
			btnTestBackward.setText("<" + lang.get("TRAINING"));
			btnTestForward.setText(lang.get("RECOGNIZING") + ">");
			((TableColumn) tvTestExamples.getColumns().get(0)).setText(lang.get("NUMBER"));
			((TableColumn) tvTestExamples.getColumns().get(1)).setText(lang.get("RECOGNIZED_LETTER"));
			((TableColumn) tvTestExamples.getColumns().get(2)).setText(lang.get("CORRECT_LETTER"));
		}
	}

	private void showHelpWindow() {
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/xmls/window_faq.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		TextArea taQA = (TextArea) root.lookup("#taQA");
		taQA.setWrapText(true);
		taQA.setFont(Font.font("Verdana", 17));

		for (int a = 1;; ++a) {
			String qa = lang.get("FAQ" + a);
			if (qa != null) {
				String[] phrases = qa.split("<br>");
				for (String p : phrases) {
					taQA.appendText(p + "\n");
				}
			} else
				break;
		}

		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setTitle(lang.get("FAQ"));
		stage.initOwner(windowStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		Image icon = null;
		try {
			icon = new Image(new File("./files/images/help_icon.png").toURI().toURL().toString());
			stage.getIcons().add(icon);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	// other
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				WindowUpdate winUpdate = (WindowUpdate) arg;
				WindowUpdateType type = winUpdate.getType();
				if (type == WindowUpdateType.TESTING_TABLEVALUE) {
					Pair<String, String> value = (Pair<String, String>) winUpdate.getValue();
					System.out.println(value.getKey());
					tvTestExamples.getItems().add(new TestingTableDataModel(tvTestExamples.getItems().size() + 1,
							value.getKey(), value.getValue()));
				} else if (type == WindowUpdateType.TESTING_NRCORRECTANSWERS) {
					String value = (String) winUpdate.getValue();
					String[] array = value.split("/");
					double correctAnswers = (double) Integer.parseInt(array[0]);
					double nrExamples = (double) Integer.parseInt(array[1]);
					double correctProcent = correctAnswers / nrExamples * 100.0;
					String text = String.format("%s: %s (%.2f%s)", lang.get("CORRECT_GUESSED"), value, correctProcent,
							"%");
					lblTestCorrectAnswers.setText(text);
				} else if (type == WindowUpdateType.TESTING_LOADDATA) {
					Integer value = (Integer) winUpdate.getValue();
					if (value == 0) {
						showLoadingWindow(lang.get("LOADING_TESTSET"));
					} else {
						stopLoadingWindow();
					}
				} else if (type == WindowUpdateType.TRAINING_LOADDATA) {
					Integer value = (Integer) winUpdate.getValue();
					if (value == 0) {
						showLoadingWindow(lang.get("LOADING_TRAINSET"));
					} else {
						stopLoadingWindow();
					}
				} else if(type==WindowUpdateType.TESTING_STARTED) {
					onStartingTesting();
				} else if(type==WindowUpdateType.TESTING_STOPPED) {
					onStoppingTesting();
					boolean normalStopped = (boolean) winUpdate.getValue();
					if(!normalStopped) {
						tvTestExamples.getItems().clear();
						lblTestCorrectAnswers.setText(lang.get("CORRECT_GUESSED") + ": -/-");
					}
				} else if (type == WindowUpdateType.TRAINING_NREPOCH) {
					int currEpoch = (int) winUpdate.getValue();
					int nrEpochs = Integer.parseInt(tfTrainNrEpochs.getText());
					lblTrainCurrEpoch.setText(String.format(lang.get("EPOCH") + ": %d/%d", currEpoch, nrEpochs));
				} else if (type == WindowUpdateType.TRAINING_EPOCHERROR) {
					Pair<Integer, Double> value = (Pair<Integer, Double>) winUpdate.getValue();
					double currEpochErr = value.getValue();
					lblTrainCurrEpochError
							.setText(String.format(lang.get("EPOCH_ERR").trim() + ": %.10f", currEpochErr));
					((XYChart.Series) chartTrainError.getData().get(0)).getData()
							.add(new XYChart.Data(value.getKey(), value.getValue()));
				} else if(type==WindowUpdateType.TRAINING_STARTED) {
					onStartingTraining();
				} else if(type==WindowUpdateType.TRAINING_STOPPED) {
					onStoppingTraining();
					boolean normalStopped = (boolean) winUpdate.getValue();
					lblTrainCurrEpoch.setText(lang.get("EPOCH") + ": -");
					lblTrainCurrEpochError.setText(lang.get("EPOCH_ERR") + ": -/-");
					if(!normalStopped) {
						((XYChart.Series) chartTrainError.getData().get(0)).getData().clear();
					}
				} else {

				}
			}
		});
	}
}