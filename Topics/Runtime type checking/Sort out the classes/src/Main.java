import java.util.List;

class Sort {
    public static void sortShapes(Shape[] array,
                                  List<Shape> shapes,
                                  List<Polygon> polygons,
                                  List<Square> squares,
                                  List<Circle> circles) {

        for (Shape shape : array) {
            if (shape instanceof Square s) {
                squares.add(s);
            } else if (shape instanceof Polygon p) {
                polygons.add(p);
            } else if (shape instanceof Circle c) {
                circles.add(c);
            } else {
                shapes.add(shape);
            }
        }
    }
}

//Don't change classes below
class Shape { }
class Polygon extends Shape { }
class Square extends Polygon { }
class Circle extends Shape { }