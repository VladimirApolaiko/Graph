package sample;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import java.util.Deque;
import java.util.LinkedList;

public class Controller {

    private static final int LINE = 0;
    private static final int CIRCLE = 1;
    private static final int PENCIL = 2;
    private static final int BEZE = 3;
    private static final int FLOOD_FILL = 4;

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

    private Deque<Double> buffer = new LinkedList<>();

    private PixelWriter pixelWriter;

    private int selectedTool;

    @FXML
    private void initialize() {

        /*Canvas initialize*/
        pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();

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
                        drawCircle(centerX, buffer.removeLast().intValue(), (int) Math.abs(centerX - event.getX()));
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
                        selectedTool = -1;
                    }
                }
                break;

                case FLOOD_FILL: {
                    floodFill4((int) event.getX(), (int) event.getY(), Color.GREEN, Color.BLACK, writableImage);
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
                    }
                }
            }
        });

        bezeButton.setOnMouseClicked(event -> {
            selectedTool = BEZE;
        });

    }

    private void drawBeze(int x1, int y1, int x2, int y2, int x3, int y3) {
        for (double t = 0.0; t <= 1; t += 0.0001) {
            pixelWriter.setColor(countCord(x1,x2,x3,t), countCord(y1, y2, y3, t), Color.BLACK);
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
            pixelWriter.setColor(x1, y1, Color.BLACK);
        }

        if (lengthY <= lengthX) {
            int x = x1;
            double y = y1;

            length++;

            while (length != 0) {
                length--;
                pixelWriter.setColor(x, (int) Math.round(y), Color.BLACK);
                x += signX;
                y += signY * lengthY / lengthX;
            }
        } else {
            double x = x1;
            int y = y1;

            length++;

            while (length != 0) {
                length--;
                pixelWriter.setColor((int) Math.round(x), y, Color.BLACK);
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
            pixelWriter.setColor(x1 + x, y1 + y, Color.BLACK);
            pixelWriter.setColor(x1 + x, y1 - y, Color.BLACK);
            pixelWriter.setColor(x1 - x, y1 + y, Color.BLACK);
            pixelWriter.setColor(x1 - x, y1 - y, Color.BLACK);

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

    private void floodFill4(int x, int y, Color newColor, Color oldColor, WritableImage writableImage){
        if(x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight() &&  !writableImage.getPixelReader().getColor(x,y).equals(oldColor) && !writableImage.getPixelReader().getColor(x, y).equals(newColor))
        {
            pixelWriter.setColor(x, y, newColor); //set color before starting recursion

            floodFill4(x + 1, y    , newColor, oldColor, writableImage);
            floodFill4(x - 1, y    , newColor, oldColor, writableImage);
            floodFill4(x    , y + 1, newColor, oldColor, writableImage);
            floodFill4(x    , y - 1, newColor, oldColor, writableImage);
        }
    }

}
