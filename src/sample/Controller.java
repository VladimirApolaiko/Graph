package sample;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Controller {

    private static final int LINE = 0;
    private static final int CIRCLE = 1;
    private static final int PENCIL = 2;
    private static final int BEZE = 3;
    private static final int FLOOD_FILL = 4;

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;

    private static final String OUT_OF_BOUNDS = "Out of screen bounds";

    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    @FXML
    private Canvas canvas;

    @FXML
    private Button lineButton;

    @FXML
    private Button circleButton;

    @FXML
    private Button pencilButton;

    @FXML
    private Button fillButton;

    @FXML
    private Button bezeButton;

    @FXML
    private Label errorMessage;

    @FXML
    private ColorPicker borderColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;

    private Deque<Double> buffer = new LinkedList<>();

    private PixelWriter pixelWriter;

    private PixelReader pixelReader;

    private int selectedTool;

    @FXML
    private void initialize() {
        /*Canvas initialize*/
        WritableImage image = new WritableImage(MAX_WIDTH, MAX_HEIGHT);
        pixelWriter = image.getPixelWriter();
        pixelReader = image.getPixelReader();


        lineButton.setOnAction(event -> selectedTool = LINE);

        circleButton.setOnAction(event -> selectedTool = CIRCLE);

        pencilButton.setOnAction(event -> selectedTool = PENCIL);

        fillButton.setOnAction(event -> selectedTool = FLOOD_FILL);

        canvas.setOnMouseClicked(event -> {
            switch (selectedTool) {
                case LINE: {
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    } else {
                        drawLine(buffer.removeLast().intValue(), buffer.removeLast().intValue(), (int) event.getX(), (int) event.getY());
                        applyChanges(image);
                        selectedTool = -1;
                    }
                }
                break;

                case CIRCLE: {
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    } else {
                        int centerX = buffer.removeLast().intValue();
                        int centerY = buffer.removeLast().intValue();
                        double deltaX = Math.abs(centerX - event.getX());
                        double deltaY = Math.abs(centerY - event.getY());
                        int radius = (int) Math.round(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
                        drawCircle(centerX, centerY, radius);
                        applyChanges(image);
                        selectedTool = -1;
                    }
                }
                break;

                case BEZE: {
                    if (buffer.size() < 6) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    }
                    if (buffer.size() == 6) {
                        drawBeze(buffer.removeLast().intValue(),
                                buffer.removeLast().intValue(),
                                buffer.removeLast().intValue(),
                                buffer.removeLast().intValue(),
                                buffer.removeLast().intValue(),
                                buffer.removeLast().intValue());
                        applyChanges(image);
                        selectedTool = -1;
                    }
                }
                break;

                case FLOOD_FILL: {
                    floodFill4((int) event.getX(), (int) event.getY(), backgroundColorPicker.getValue(), borderColorPicker.getValue());
                    applyChanges(image);
                    selectedTool = -1;
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            switch (selectedTool) {
                case PENCIL: {
                    if (!event.getButton().equals(MouseButton.PRIMARY)) return;
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    } else {
                        drawLine(buffer.removeLast().intValue(), buffer.removeLast().intValue(), (int) event.getX(), (int) event.getY());
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                        applyChanges(image);
                    }
                }
            }
        });

        canvas.setOnMouseReleased(event -> {
            switch (selectedTool) {
                case PENCIL: {
                    buffer.clear();
                }
            }
        });

        bezeButton.setOnMouseClicked(event -> {
            selectedTool = BEZE;
        });

    }

    private void showMessage(String message) {
        errorMessage.setText(message);
        pool.schedule(() -> errorMessage.setText(""), 3, TimeUnit.SECONDS);
    }

    private void drawBeze(int x1, int y1, int x2, int y2, int x3, int y3) {
        for (double t = 0.0; t <= 1; t += 0.0001) {
            drawPixel(countCord(x1, x2, x3, t), countCord(y1, y2, y3, t), borderColorPicker.getValue());
        }
    }

    private int countCord(int value1, int value2, int value3, double t) {
        return (int) Math.round(Math.pow(1 - t, 2) * value1 + 2 * (1 - t) * t * value2 + Math.pow(t, 2) * value3);
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        double lengthX = Math.abs(x2 - x1);
        double lengthY = Math.abs(y2 - y1);

        int signX = x1 < x2 ? 1 : -1;
        int signY = y1 < y2 ? 1 : -1;

        double length = Math.max(lengthX, lengthY);

        if (length == 0) {
            drawPixel(x1, y1, borderColorPicker.getValue());
        }

        if (lengthY <= lengthX) {
            int x = x1;
            double y = y1;

            length++;

            while (length != 0) {
                length--;
                drawPixel(x, (int) Math.round(y), borderColorPicker.getValue());
                x += signX;
                y += signY * lengthY / lengthX;
            }
        } else {
            double x = x1;
            int y = y1;

            length++;

            while (length != 0) {
                length--;
                drawPixel((int) Math.round(x), y, borderColorPicker.getValue());
                y += signY;
                x += signX * lengthX / lengthY;
            }
        }
    }

    private void drawCircle(int x1, int y1, int r) {
        int x = 0;
        int y = r;
        int error;
        int delta = (2 - 2 * r);

        while (y >= 0) {
            drawPixel(x1 + x, y1 + y, borderColorPicker.getValue());
            drawPixel(x1 + x, y1 - y, borderColorPicker.getValue());
            drawPixel(x1 - x, y1 + y, borderColorPicker.getValue());
            drawPixel(x1 - x, y1 - y, borderColorPicker.getValue());

            error = 2 * (delta + y) - 1;

            if (delta < 0 && error <= 0) {
                x++;
                delta += 2 * x + 1;
                continue;
            }

            if (delta > 0 && error > 0) {
                y--;
                delta -= 2 * y + 1;
                continue;
            }

            x++;
            delta += 2 * (x - y);
            y--;
        }
    }

    private void floodFill4(int x, int y, Color backgroundColor, Color borderColor) {
        if (x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight() && !pixelReader.getColor(x, y).equals(backgroundColor) && !pixelReader.getColor(x, y).equals(borderColor)) {
            drawPixel(x, y, backgroundColor); //set color before starting recursion

            floodFill4(x + 1, y, backgroundColor, borderColor);
            floodFill4(x - 1, y, backgroundColor, borderColor);
            floodFill4(x, y + 1, backgroundColor, borderColor);
            floodFill4(x, y - 1, backgroundColor, borderColor);
        }
    }

    private void drawPixel(int x, int y, Color color) {
        if (x > 0 && x < MAX_WIDTH && y > 0 && y < MAX_HEIGHT) {
            pixelWriter.setColor(x, y, color);
        }
    }

    private void applyChanges(Image image) {
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
    }

}
