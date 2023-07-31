package prop.com.robocode;

import robocode.*;
import java.awt.Color;

/**
 * Clase que contiene la implementación de un equipo de cinco robots, todos ellos de tipologia de asalto, atacan todo el tiempo.
 * @author Gabriel Guardiola, Omar Briqa
 */
public class Assault extends TeamRobot {
    
    private String targetRobot = null;
    
    @Override
    public void run(){
        
        setColors(Color.CYAN, Color.CYAN, Color.CYAN);
        
        while(true){            
            turnRadarRight(360);
            execute();
        }
        
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e){
               
        if(targetRobot == null && !isTeammate(e.getName())){
            targetRobot = e.getName();
            attackRobot(e.getBearing(), getGunHeading(), e.getDistance(), e.getEnergy());
        }
        else if(isTeammate(e.getName()) && Math.abs(e.getBearing()) < 5){
            targetRobot = null;
            setBack(100); setTurnRight((getTime() % 2 == 0) ? 90 : -90 );
        }
        else if(e.getName().equals(targetRobot))
            attackRobot(e.getBearing(), getGunHeading(), e.getDistance(), e.getEnergy());
        
        execute();
    }
    
    @Override
    public void onHitRobot(HitRobotEvent e){
        if(isTeammate(e.getName())){
            setBack(100); 
            setTurnRight((getTime() % 2 == 0) ? 90 : -90 );
        }
        else if(!isTeammate(e.getName())){
            setFire(Rules.MAX_BULLET_POWER);
            setBack(100);
        }
        
        execute();
    }
    
    @Override
    public void onHitWall(HitWallEvent e){
        stop(); back(100); 
        if (e.getBearing() > 0.0) turnLeft(-e.getBearing());
        else turnRight(-e.getBearing());
        
    }
    
    @Override
    public void onRobotDeath(RobotDeathEvent e){   
        if(e.getName().equals(targetRobot)){
            targetRobot = null;
            setTurnRadarRight(360);
            setBack(100); setTurnRight((getTime() % 2 == 0) ? 90 : -90 );
        }
        execute();
    }
    
    
    /**
    * Función que persigue al enemigo del kamikaze hasta destruirlo o ser destruido.
    * @param bearing Ángulo relativo del robot al robot enemigo
    * @param gunHeading Ángulo de apertura del cañon propio
    * @param dist Distancia hacia el robot enemigo
    * @param enemyEnergy Energia restante del robot enemigo
    */
    public void attackRobot(double bearing, double gunHeading, double dist, double enemyEnergy){
 
        setTurnRight(bearing);setAhead(dist); 
        fireToRobot(dist, enemyEnergy, gunHeading);
        execute();
    }
    
    /**
    * Función que dispara a un robot enemigo en función de la distancia hacia este y la energia restante de ese robot.
    * @param dist Distancia hacia el robot enemigo
    * @param enemyEnergy Energia restante del robot enemigo
    * @param gunHeading Ángulo de apertura del cañon propio
    */
    public void fireToRobot(double dist, double enemyEnergy, double gunHeading){
        
        if(dist < 100) fire(Rules.MAX_BULLET_POWER);
        else if(dist < 200) 
            setFire(Math.min(getEnergy()/50 + 30/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else if(dist < 400)
            setFire(Math.min(getEnergy()/75 + 20/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else if(dist < 600)
            setFire(Math.min(getEnergy()/100 + 10/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else{
            double alpha = Math.abs(gunHeading) % 90; 
            if(alpha > 80 && alpha < 90) alpha = 90 - alpha;
            
            if(alpha < 5) setFire(Rules.MAX_BULLET_POWER);
            else{
                Double rand = Math.random(); 
                int R = rand.intValue() % 3; if(R > 1) fire(Rules.MAX_BULLET_POWER);
            }
        }
        
    }

}
