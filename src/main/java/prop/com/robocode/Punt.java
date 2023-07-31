package prop.com.robocode;

/**
 * Clase que representa un punto en dos dimensiones (2D).
 */

public class Punt {
    
    private double x, y;
    
    /**
     * Constructora por defecto. Inicializa el punto a (0.0, 0.0).
     */
    public Punt(){
        this.x = 0.0;
        this.y = 0.0;
    }
    
    /**
     * Constructora a partir del valor de x e y.
     * @param x Valor de x
     * @param y Valor de y
     */
    public Punt(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Función que devuelve el valor x del punto.
     * @return Devuelve el valor x del punto.
     */
    public double getX() {
        return x;
    }

    /**
     * Función que devuelve el valor y del punto.
     * @return Devuelve el valor y del punto.
     */
    public double getY() {
        return y;
    }
    
    /**
     * Función que calcula la distancia entre el punto del parámetro implícito y el punto P
     * @param P Punto P
     * @return Distancia entre el punto del parámetro implícito y el punto P
     */
    public double distanceBetween(Punt P){
        return Math.sqrt(Math.pow(Math.abs(P.getX() - this.x), 2.0) +
                         Math.pow(Math.abs(P.getY() - this.y), 2.0));
    }
    
    
    public String toString() {
        return x + "," + y;
    }
    
}
