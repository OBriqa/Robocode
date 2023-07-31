package prop.com.robocode;

/**
 * Clase que contiene un par formado por un valor decimal (Double) y un String
 * Representa la distancia (distance) que hay desde un robot (robotName) a otro punto.
 */

public class Parell implements Comparable<Parell>{
    
    private final Double distance;
    private final String robotName;

    /**
     * Constructora que utiliza el nombre del robot y la distancia.
     * @param robotName Nombre del robot
     * @param distance Distancia desde el robot a otro punto
     */
    public Parell(String robotName, Double distance) {
        this.distance = distance;
        this.robotName = robotName;
    }

    /**
     * Función que devuelve la distancia que hay almacenada en el objeto.
     * @return Distancia que hay almacenada en el objeto
     */
    public Double getDist() {
        return distance;
    }

    /**
     * Función que devuelve el nombre del robot que hay almacenado en el objeto.
     * @return Nombre que hay almacenado en el objeto
     */
    public String getName() {
        return robotName;
    }

    @Override
    public int compareTo(Parell o) {
        Double D = (this.distance - o.distance);
        return D.intValue();
    }

    @Override
    public String toString() {
        return "{" + "D=" + distance + 
                   ", N=" + robotName.charAt(robotName.length()-2) + '}';
    }
    
}
