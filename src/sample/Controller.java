package sample;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

public class Controller {

    private static final int LINE = 0;
    private static final int CIRCLE = 1;
    private static final int PENCIL = 2;

    @FXML
    private Canvas canvas;

    @FXML
    private Button lineButton;

    @FXML
    private Button circleButton;

    @FXML
    private Button pencilButton;

    private Deque<Double> buffer = new LinkedList<>();

    private PixelWriter pixelWriter;

    private int selectedTool;

    @FXML
    private void initialize() {
        lineButton.setOnAction(event -> {
            selectedTool = LINE;
        });

        circleButton.setOnAction(event -> {
            selectedTool = CIRCLE;
        });

        pencilButton.setOnAction(event -> {
            selectedTool = PENCIL;
        });

        canvas.setOnMouseClicked(event -> {
            switch(selectedTool){
                case LINE :{
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    } else {
                        drawLine(buffer.removeLast().intValue(), buffer.removeLast().intValue(), (int) event.getX(), (int) event.getY());
                        selectedTool = -1;
                    }
                }break;

                case CIRCLE : {
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    }else{
                        int centerX = buffer.removeLast().intValue();
                        drawCircle(centerX, buffer.removeLast().intValue(), (int) Math.abs(centerX - event.getX()));
                    }
                }break;
            }


        });

        canvas.setOnMouseMoved(event -> {
            switch(selectedTool){
                case PENCIL :{
                    if (buffer.size() == 0) {
                        buffer.add(event.getY());
                        buffer.add(event.getX());
                    } else {
                        drawLine(buffer.removeLast().intValue(), buffer.removeLast().intValue(), (int) event.getX(), (int) event.getY());
                    }
                }
            }
        });


        pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
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
        int error = 0;
        int delta = (2- 2 * r);

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

            if(delta > 0 && error > 0){
                y--;
                delta -= 2 * y + 1;
                continue;
            }

            x++;
            delta += 2  * (x - y);
            y--;
        }
    }
}
