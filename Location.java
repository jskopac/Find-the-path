
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Location extends Circle {
    private String name;

    public Location(double centerX, double centerY, double radius) {
        super(centerX, centerY, radius);
        unmarkedLocation();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void markedLocation() {
        setFill(Color.RED);
    }

    public void unmarkedLocation() {
        setFill(Color.BLUE);
    }

    public String toString() {
        return this.name;
    }
}
