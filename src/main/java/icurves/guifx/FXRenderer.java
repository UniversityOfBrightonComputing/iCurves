package icurves.guifx;

import icurves.description.AbstractCurve;
import icurves.concrete.*;
import icurves.graph.EulerDualGraph;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXRenderer extends Pane {

    Pane rootShadedZones = new Pane();
    private Canvas canvas = new Canvas();

    Pane rootSceneGraph = new Pane();

    private GraphicsContext g;

    private List<Color> colors = new ArrayList<>();
    private int colorIndex = 0;

    public FXRenderer() {
        canvas.setMouseTransparent(true);
        g = canvas.getGraphicsContext2D();

        rootSceneGraph.setMouseTransparent(true);

        getChildren().addAll(rootShadedZones, rootSceneGraph, canvas);

        // these values are adapted from "How should we use colour in ED" Andrew Blake, et al
        for (int i = 0; i < 10; i++) {
            colors.add(Color.hsb(((i+1))*32,
                    (i == 1 || i == 2) ? 0.26 : 0.55,
                    (i == 1 || i == 2) ? 0.88 : 0.92));
        }

        Collections.swap(colors, 1, 9);
        Collections.swap(colors, 3, 7);
    }

    public void clearSceneGraph() {
        colorIndex = 0;

        rootSceneGraph.getChildren().clear();
        rootShadedZones.getChildren().clear();
    }

    public void addContour(Contour contour) {
        Shape s = contour.getShape();
        s.setStrokeWidth(16);
        s.setStroke(colors.get(colorIndex++));
        s.setFill(null);

        Text label = new Text(contour.getCurve().getLabel());
        label.setFont(Font.font(72));
        label.setFill(s.getStroke());
        label.setTranslateX(s.getLayoutBounds().getMaxX());
        label.setTranslateY(s.getLayoutBounds().getMinY());

        Platform.runLater(() -> rootSceneGraph.getChildren().addAll(s, label));
    }

    // ORIGINAL BELOW

    public void setCanvasSize(double w, double h) {
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public void draw(ConcreteDiagram diagram, boolean showDual) {
        draw(diagram);

        if (!showDual) {
            return;
        }

        EulerDualGraph dual = new EulerDualGraph(diagram);
        drawPoints(dual.getNodes().stream().map(n -> n.getZone().getCenter()).collect(Collectors.toList()));
        dual.getEdges().forEach(e -> {
            e.getCurve().setStroke(Color.RED);
            rootSceneGraph.getChildren().addAll(e.getCurve());
        });
    }

    public void draw(ConcreteDiagram diagram) {
        Rectangle bbox = toFXRectangle(diagram.getBoundingBox());
        setCanvasSize(bbox.getWidth(), bbox.getHeight());

        clearRenderer();

        for (ConcreteZone zone : diagram.getShadedZones())
            drawShadedZone(zone, bbox);

        List<ConcreteZone> normalZones = new ArrayList<>(diagram.getAllZones());
        normalZones.removeAll(diagram.getShadedZones());
        //normalZones.removeIf(z -> z.getContainingContours().isEmpty());

        for (ConcreteZone zone : normalZones)
            drawNormalZone(zone, bbox);

        for (CircleContour contour : diagram.getCircles())
            drawCircleContour(contour);

        rootSceneGraph.getChildren().clear();

        for (PathContour contour : diagram.getContours()) {
            rootSceneGraph.getChildren().addAll(contour.getShape());
        }
    }

    private Set<AbstractCurve> makeCurves(String... curveLabels) {
        return Arrays.asList(curveLabels)
                .stream()
                .map(AbstractCurve::new)
                .collect(Collectors.toSet());
    }

    private class MovablePoint extends StackPane {
        private double mouseX, mouseY;
        private double oldX, oldY;

        //        dual.getEdges().forEach(q -> {
//            Point2D center1 = q.getV1().getZone().getCenter();
//            Point2D center2 = q.getV2().getZone().getCenter();
//
//            MovablePoint p1 = new MovablePoint(center1.getX(), center1.getY());
//            MovablePoint p2 = new MovablePoint(center2.getX(), center2.getY());
//
//            q.getCurve().startXProperty().bind(p1.layoutXProperty());
//            q.getCurve().startYProperty().bind(p1.layoutYProperty());
//            q.getCurve().endXProperty().bind(p2.layoutXProperty());
//            q.getCurve().endYProperty().bind(p2.layoutYProperty());
//
//            rootSceneGraph.getChildren().addAll(q.getCurve(), p1, p2);
//        });

        public MovablePoint(double x, double y) {
            relocate(x, y);

            oldX = x;
            oldY = y;

            Circle c = new Circle(5, Color.RED);
            c.setCenterX(5);
            c.setCenterY(5);

            getChildren().addAll(c);

            setOnMousePressed(e -> {
                mouseX = e.getSceneX();
                mouseY = e.getSceneY();
            });

            setOnMouseDragged(e -> {
                relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
            });

            setOnMouseReleased(e -> {
                oldX = getLayoutX();
                oldY = getLayoutY();
            });
        }
    }

    private void drawShadedZone(ConcreteZone zone, Rectangle bbox) {
        Shape shape = bbox;

        for (Contour contour : zone.getContainingContours()) {
            shape = Shape.intersect(shape, contour.getShape());
        }

        for (Contour contour : zone.getExcludingContours()) {
            shape = Shape.subtract(shape, contour.getShape());
        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.LIGHTGREY);
        rootShadedZones.getChildren().add(shape);
    }

    private void drawNormalZone(ConcreteZone zone, Rectangle bbox) {
        //Shape shape = bbox;

        Shape shape = zone.getShape();
//        for (Contour contour : zone.getContainingContours()) {
//            shape = Shape.intersect(shape, contour.getShape());
//        }
//
//        for (Contour contour : zone.getExcludingContours()) {
//            shape = Shape.subtract(shape, contour.getShape());
//        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.TRANSPARENT);

//        Set<AbstractCurve> set = new TreeSet<>();
//        set.add(new AbstractCurve("c"));
//
//        Rectangle rect = null;
//
//        if (zone.getAbstractZone().equals(new AbstractBasicRegion(set))) {
//            shape.setFill(Color.RED);
//
//            rect = new Rectangle(5, 5, Color.BLUE);
//            rect.setX(550);
//            rect.setY(300);
//            rect.setStroke(Color.YELLOW);
//
//            System.out.println("Render shape bounds: " + shape.getLayoutBounds());
//            System.out.println("Render Empty: " + Shape.subtract(rect, shape).getLayoutBounds().isEmpty());
//        }

        rootShadedZones.getChildren().add(shape);

//        if (rect != null) {
//            rootShadedZones.getChildren().addAll(rect);
//        }
    }

    private void drawCircleContour(CircleContour contour) {
        g.setFill(Color.BLACK);
        g.setStroke(colors.get(colorIndex++));
        g.setLineWidth(16);
        g.setFont(Font.font(72));

        double radius = contour.getRadius();
        double x = contour.getCenterX() - radius;
        double y = contour.getCenterY() - radius;
        double w = 2 * radius;
        double h = 2 * radius;

        g.strokeOval(x, y, w, h);
        g.fillText(contour.getCurve().getLabel(), contour.getLabelXPosition(), contour.getLabelYPosition());
    }

    public void drawPoints(List<Point2D> points) {
        double r = 5;

        points.forEach(p -> {
            g.fillOval(300 + p.getX() - r / 2, 300 + p.getY() - r / 2, r, r);
        });
    }

    public void clearRenderer() {
        rootShadedZones.getChildren().clear();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private Rectangle toFXRectangle(icurves.geometry.Rectangle rectangle) {
        return new Rectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    public List<Node> getShadedZones() {
        return rootShadedZones.getChildrenUnmodifiable();
    }
}
