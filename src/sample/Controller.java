package sample;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Canvas mainCanvas;
    public TextField a1;
    public TextField b;
    public TextField a2;
    public Slider sldZoom;
    public Text areaT;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        draw();
        a1.textProperty().addListener((observableValue, s, t1) -> {
            draw();
        });

        b.textProperty().addListener((observableValue, s, t1) -> {
            draw();
        });

        a2.textProperty().addListener((observableValue, s, t1) -> {
            draw();
        });

        sldZoom.valueProperty().addListener((observableValue, number, t1) -> {
            draw();
        });


    }
    boolean check (String str){
        boolean result;
        try {
            Double.parseDouble(str);
            result = true;
        } catch (Exception e){
            result = false;
        }
        return result;
    }

    void draw() {

        if(check(a1.getText()) && check(a2.getText()) && check(b.getText())) {

            double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            double zoom = dpi / 2.54 * sldZoom.getValue() / 100;

            GraphicsContext ctx = mainCanvas.getGraphicsContext2D();

            ctx.setFill(Color.WHITE);
            ctx.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());

            ctx.save();
            Affine transform = ctx.getTransform();
            transform.appendTranslation(mainCanvas.getWidth() / 2, mainCanvas.getHeight() / 2);
            transform.appendScale(zoom, -zoom);
            ctx.setTransform(transform);

            double p1 = Double.parseDouble(a1.getText());
            double p2 = Double.parseDouble(b.getText());
            double p3 = Double.parseDouble(a2.getText());

            ctx.setLineWidth(2 / zoom);
            ctx.strokePolygon(
                    new double[]{-p2 / 2, p2 / 2, -p2 / 2},
                    new double[]{-p1 / 2, -p1 / 2, p1 / 2},
                    3
            );
            ctx.strokePolygon(
                    new double[]{p2 / 2, 0, -p2 / 2},
                    new double[]{-p1 / 2, p3 / 2, -p1 / 2},
                    3
            );

            //ctx.setStroke(Color.web("#4fef1b"));

            //точки пересечения
            double[] intersectionPoints = new double[2];
            cross(intersectionPoints, -p2 / 2, -p1 / 2, 0, p3 / 2, p2 / 2, -p1 / 2, -p2 / 2, p1 / 2);


            //две стороны треугольника, в который вписана окружность
            double secondSide = Math.sqrt(Math.pow(-p2 / 2 - intersectionPoints[0], 2) + Math.pow(-p1 / 2 - intersectionPoints[1], 2));
            double thirdSide = Math.sqrt(Math.pow(p2 / 2 - intersectionPoints[0], 2) + Math.pow(-p1 / 2 - intersectionPoints[1], 2));


            //центр окружности
            double x = (p2 * intersectionPoints[0] +
                    secondSide * p2 / 2 +
                    thirdSide * -p2 / 2) /
                    (p2 + secondSide + thirdSide);

            double y = (p2 * intersectionPoints[1] +
                    secondSide * -p1 / 2 +
                    thirdSide * -p1 / 2) /
                    (p2 + secondSide + thirdSide);

            //полупериметр
            double p = (p2 + secondSide + thirdSide) / 2;

            // радиус окружности
            double r = Math.sqrt(((p - p2) * (p - secondSide) * (p - thirdSide)) / p);



            //расстояние от центра окружности до вершины
            double l = Math.sqrt(Math.pow(x - intersectionPoints[0], 2) + Math.pow(y  - intersectionPoints[1], 2));

            //длина касательной
            double d = Math.sqrt( l * l - r * r);

            //косинус угла
            double cos = (x - intersectionPoints[0]) / l;

            double u1;

            if(y < intersectionPoints[1] ){
                u1 =  Math.acos(cos);
            }else u1 = 2 * Math.PI - Math.acos(cos);

            cos = d/l;

            double u2 = Math.acos(cos);

            double u = u1 - u2; // первая точка

            double x1 = intersectionPoints[0] + d * Math.cos(u);
            double y1 = intersectionPoints[1] - d * Math.sin(u);

            u = u1 + u2; // вторая
            double x2 = intersectionPoints[0] + d * Math.cos(u);
            double y2 = intersectionPoints[1] - d * Math.sin(u);



            ctx.strokePolygon(
                    new double[]{x1, x2,intersectionPoints[0]},
                    new double[]{y1, y2,intersectionPoints[1]},
                    3
            );

            ctx.setFill(Color.BLACK);
            ctx.fillPolygon(
                    new double[]{x1, x2,intersectionPoints[0]},
                    new double[]{y1, y2,intersectionPoints[1]},
                    3
            );
            //рисуем окружность
            ctx.strokeOval(
                    x - r, //x
                    y - r,//y
                    r * 2,
                    r * 2
            );
            ctx.setFill(Color.WHITE);
            ctx.fillOval(
                    x - r, //x
                    y - r,//y
                    r * 2,
                    r * 2
            );

            double a = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1  - y2, 2));
            double b = Math.sqrt(Math.pow(x2 - intersectionPoints[0], 2) + Math.pow(y2  - intersectionPoints[1], 2));
            double c = Math.sqrt(Math.pow(x - intersectionPoints[0], 2) + Math.pow(y  - intersectionPoints[1], 2));


            double hperimeter = (a + b + c) / 2;

            double h = (2 * Math.sqrt( hperimeter * (hperimeter - a) * (hperimeter - b) * (hperimeter - c))) / a;

            double s = (a * h) / 2;


            areaT.setText(String.format("%.3f",s));



            ctx.restore();
        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Неверный формат числа.");
            alert.showAndWait();

        }

    }


    boolean cross(double intersectionPoints[], double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double n;
        if (y2 - y1 != 0) {  // a(y)
            double q = (x2 - x1) / (y1 - y2);
            double sn = (x3 - x4) + (y3 - y4) * q;
            if (sn == 0) {
                return false;
            }  // c(x) + c(y)*q
            double fn = (x3 - x1) + (y3 - y1) * q;   // b(x) + b(y)*q
            n = fn / sn;
        } else {
            if ((y3 - y4) == 0) {
                return false;
            }  // b(y)
            n = (y3 - y1) / (y3 - y4);   // c(y)/b(y)
        }
        intersectionPoints[0] = x3 + (x4 - x3) * n;  // x3 + (-b(x))*n
        intersectionPoints[1] = y3 + (y4 - y3) * n;  // y3 +(-b(y))*n
        return true;
    }
}
