package prop.com.robocode;

import robocode.*;
import java.awt.Color;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections; 

/**
 * Clase que implementa un equipo cinco de Robots, cuatro de ellos situados en la esquina y un 'kamikaze' de libre.
 * @author Gabriel Guardiola, Omar Briqa
 */
public class Corner extends TeamRobot{

    enum type { 
        DEFAULT, 
        CORNER, 
        KAMIKAZE
    } type MODEL = type.DEFAULT;
    enum state { 
        START, 
        toCORNER, 
        CORNER, 
        ATTACK 
    } state STATUS = state.START;
    
    private Punt iniP = new Punt();
    private Punt[] Corner  = {new Punt(), new Punt(), new Punt(), new Punt()};
    private Punt[] CornerX = {new Punt(), new Punt(), new Punt(), new Punt()};
    
    private static final int ROTATETURN = 200;
    
    private int turnCounter = 1;
    private String kamikaze = null;
    private String targetRobot = null;
    private boolean kamikazeAlive = true;
    private boolean firstTimeCorner = false;
    
    private int     corner = -1;
    private boolean cornerTaked = false;
    private List<String> L = new ArrayList<>();
    private List<String> blackList = new ArrayList<>();
    private List<Parell> targetRobots = new ArrayList<>();
    private List<String> targetCorners = new ArrayList<>();
    
    
    @Override
    public void run(){
        
        setPoints();
        
        try { broadcastMessage("Point," + iniP.toString());
        } catch (IOException ignored) {}
                
        while(true){
            
            sentinelMovement();
            
            switch(STATUS){
                case toCORNER -> {
                    if(firstTimeCorner) setTurnRadarRight(720*3);
                    while(STATUS != state.CORNER)
                        checkCornerPosition();
                }
                case CORNER -> {
                    setTurnRadarRight(720*3);
                    setTargetCorners();
                }
                case ATTACK -> {
                    turnRadarRight(360);
                    setTargetKamikaze();
                }
            }
            
            execute();
        }
        
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e){
        
        if(!isTeammate(e.getName())){
            
            switch(STATUS){
                case toCORNER -> {
                    if(!firstTimeCorner) fire(Math.min(300/e.getDistance(), 3));
                    else if(targetRobot == null && !kamikazeAlive)
                        targetCorners.add(e.getName());
                    else if(e.getName().equals(targetRobot)){
                        switch(corner){
                            case 0 -> setTurnGunRight((180 - getGunHeading()) + e.getBearing());
                            case 1 -> {
                                if(getGunHeading() <= 90) turnGunRight((90 - getGunHeading()) + e.getBearing());
                                else turnGunRight((360 - getGunHeading() + 90) + e.getBearing());
                            }
                            case 2 -> setTurnGunRight((360 - getGunHeading()) + e.getBearing());
                            case 3 -> setTurnGunRight((270 - getGunHeading()) + e.getBearing());
                        }
                        fireToRobot(e.getDistance(), e.getEnergy(), getGunHeading());
                    }
                }
                case CORNER -> {
                    if(targetRobot == null && !kamikazeAlive)
                        targetCorners.add(e.getName());
                    else if(e.getName().equals(targetRobot)){
                        switch(corner){
                            case 0 -> setTurnGunRight(-getGunHeading() + (45 + e.getBearing()));  
                            case 1 -> setTurnGunLeft((getGunHeading() - 360) + (45 - e.getBearing()));
                            case 2 -> setTurnGunRight((270 - getGunHeading()) - ((45 - e.getBearing())));
                            case 3 -> setTurnGunRight((180 - getGunHeading()) - (45 - e.getBearing()));
                        }
                        fireToRobot(e.getDistance(), e.getEnergy(), getGunHeading());
                    }
                }
                case ATTACK -> {
                    if(targetRobot == null)
                        targetRobots.add(new Parell(e.getName(), e.getDistance()));
                    
                    else if(e.getName().equals(targetRobot))
                        attackRobot(e.getBearing(), getGunHeading(), e.getDistance(), e.getEnergy());
                }
            }
            
            execute();
        }
    }
    
    @Override
    public void onRobotDeath(RobotDeathEvent e){   
        if(e.getName().equals(targetRobot))
            targetRobot = null;
        else if(e.getName().equals(kamikaze)){
            kamikazeAlive = false;
        }
    }
    
    @Override
    public void onHitWall(HitWallEvent e){
        if(STATUS == state.ATTACK){
            stop(); ahead(-65); 
            if (e.getBearing() > 0.0) turnLeft(-e.getBearing());
            else turnRight(-e.getBearing());
        }
    }
    
    @Override
    public void onMessageReceived(MessageEvent e){
        
        if(e.getMessage().toString().contains("Point")){
            String[] msg = e.getMessage().toString().split(",");
            L.add(e.getSender() + "," + msg[1] + "," + msg[2]);
        }
        else if(e.getMessage().toString().contains("TargetKK")){
            String[] msg = e.getMessage().toString().split(",");
            targetRobot = msg[1];
        }
        else if(e.getMessage().toString().contains("KamikazeName")){
            String[] msg = e.getMessage().toString().split(",");
            kamikaze = msg[1];
        }
        
        if(L.size() == 4) decideCornerRobot();
    }
    
    /**
     * Función que decide, al inicio de la partida, la posición (esquina) de cada robot CORNER.
     */
    public void decideCornerRobot(){
        
        STATUS = state.toCORNER;
        
        String[] ini0 =  L.get(0).split(",");
        String[] ini1 =  L.get(1).split(",");
        String[] ini2 =  L.get(2).split(",");
        String[] ini3 =  L.get(3).split(",");
        
        String[] names = {ini0[0], ini1[0], ini2[0], ini3[0]};
        
        Punt iniPX[] = {
            new Punt(Double.parseDouble(ini0[1]), Double.parseDouble(ini0[2])),
            new Punt(Double.parseDouble(ini1[1]), Double.parseDouble(ini1[2])),
            new Punt(Double.parseDouble(ini2[1]), Double.parseDouble(ini2[2])),
            new Punt(Double.parseDouble(ini3[1]), Double.parseDouble(ini3[2]))
        };
        
        for(int i = 0; i < 4; i++){
            
            List<Parell> distL = new ArrayList<>();
            double dist = iniP.distanceBetween(Corner[i]);
            for(int j = 0; j < 4; j++) distL.add(new Parell(names[j], iniPX[j].distanceBetween(Corner[i])));
            Collections.sort(distL);

            if((dist < distL.get(0).getDist()) && !blackList.contains(getName())){
                cornerTaked = true; corner = i;
                blackList.add(getName());
            }
            else if(!cornerTaked && blackList.contains(distL.get(0).getName())){
                Parell nF = nextFreeRobot(distL);
                if(dist < nF.getDist()){
                    cornerTaked = true; corner = i;
                    blackList.add(getName());
                }
                else blackList.add(nF.getName());
            }
            else blackList.add(distL.get(0).getName());
            
        }
                        
        MODEL  = cornerTaked ? type.CORNER    : type.KAMIKAZE;
        STATUS = cornerTaked ? state.toCORNER : state.ATTACK;
        
        if(MODEL == type.KAMIKAZE && STATUS == state.ATTACK){
            setColors(Color.RED, Color.RED, Color.RED, Color.RED, Color.RED);
            try { broadcastMessage("KamikazeName," + getName());
            } catch (IOException ignored) {}
        }
        
    }
    
    /**
     * Función que devuelve el primer robot no ocupado y que es el de la distancia mínima hacia una esquina concreta
     * @param L Lista de pares con todos los robots y su respectiva distancia hacia una esquina concreta
     * @return Par con el nombre del robot libre y su distancia a la esquina correspondiente
     */
    public Parell nextFreeRobot(List<Parell> L){
        int i = 0; boolean trobat = false;
        while(i < L.size() && !trobat){
            trobat = !blackList.contains(L.get(i).getName()); i++;
        } return (i > 0) ? L.get(i-1) : L.get(0);
    }
   
    
    /**
     * Función que comprueba la posición del robot con la de la esquina asignada, y lo vuelve a enviar a ella en caso de que no esté allí.
     */
    public void checkCornerPosition(){
        
        Punt actP = new Punt(getX(), getY());
        boolean checkCorner = false;
                        
        switch (corner) {
            case 0 -> {
                if(getX() > 20 || getY() > 20) 
                    sendRobotToCorner(actP);                    
                else checkCorner = true;
            }
            case 1 ->{
                if(getX() < (Corner[1].getX() - 20) || getY() > 20) 
                    sendRobotToCorner(actP);                 
                else checkCorner = true;
            }
            case 2 -> {
                if(getX() < (Corner[2].getX() - 20) || getY() < (Corner[2].getY() - 20))
                    sendRobotToCorner(actP);                  
                else checkCorner = true;
            }
            case 3 ->{
                if(getX() > 20 || getY() < (Corner[3].getY() - 20))
                    sendRobotToCorner(actP); 
                
                else checkCorner = true;
            }
        }
                
        STATUS = checkCorner ? state.CORNER : state.toCORNER;
        if(STATUS == state.CORNER){
            if(firstTimeCorner == false) 
                firstTimeCorner = true;
            aimCenter();
        }
        
        execute();
    }
    
    /**
     * Función que envia el robot a una esquina concreta
     * @param P Punto que representa una esquina
     */
    public void sendRobotToCorner(Punt P){
                                
        double phi   = 0.0;
        double beta  = 0.0;
        double alpha = getHeading();

        switch(corner){
            case 0 -> {
                phi = Math.toDegrees(Math.atan((P.getY() - 18.0)/(P.getX() - 18.0)));
                phi = Math.abs(phi); beta = (360.0 - alpha) - (90 + phi);
            } case 1 -> {
                phi = Math.toDegrees(Math.atan((P.getY() - 18.0)/(CornerX[1].getX() - P.getX()))); 
                phi = Math.abs(phi); beta = -alpha + (90 + phi);
            } case 2 -> {
                phi = Math.toDegrees(Math.atan((CornerX[2].getY() - P.getY())/(CornerX[2].getX() - P.getX())));
                phi = Math.abs(phi); beta = -alpha + (90 - phi);
            } case 3 -> {
                phi = Math.toDegrees(Math.atan((CornerX[3].getY() - P.getY())/(P.getX() - 18.0)));
                phi = Math.abs(phi); beta = (360.0 - alpha) - (90 - phi);
            }
        } if(beta < -180) beta += 360;
        
        if(firstTimeCorner) setTurnRadarRight(360);
        
        if(Math.abs(beta) > 0.01) setTurnRight(beta);
        else setAhead(P.distanceBetween(CornerX[corner]));
        
        execute();
    }
    
    /**
     * Función que rota el robot esquina CORNER para que todo apunte hacia el centro del campo de batalla.
     */
    public void aimCenter(){
        
        double alpha = getHeading();
        if(alpha > 180.0) alpha = (-1)*(360-alpha);
        turnRight(-alpha);
        
        switch(corner){
            case 0 -> setTurnRight(45);
            case 1 -> setTurnLeft(45);
            case 2 -> setTurnLeft(135);
            case 3 -> setTurnRight(135);
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
        
        setTurnRight(bearing);                
        setAhead(dist);
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
        
        if(dist < 50) fire(Rules.MAX_BULLET_POWER);
        else if(dist < 200) 
            setFire(Math.min(getEnergy()/100 + 50/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else if(dist < 400)
            setFire(Math.min(getEnergy()/125 + 40/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else if(dist < 600)
            setFire(Math.min(getEnergy()/150 + 30/enemyEnergy, Rules.MAX_BULLET_POWER));
        
        else{
            double alpha = Math.abs(gunHeading) % 90; 
            if(alpha > 80 && alpha < 90) alpha = 90 - alpha;
            
            if(alpha < 4) setFire(Math.min((600/dist + 0.5), Rules.MAX_BULLET_POWER));
            else{
                Double rand = Math.random(); 
                int R = rand.intValue() % 5; if(R > 3) fire(0.5);
            }
        }
        
    }
    
    
    /**
     * Asigna el robot más cercano al kamikaze como su objetivo a destruir.
     */
    public void setTargetKamikaze(){
        
        if(targetRobot == null && !targetRobots.isEmpty()){
            Collections.sort(targetRobots);
            targetRobot = targetRobots.get(0).getName();
            targetRobots.clear();
            
            try { broadcastMessage("TargetKK," + targetRobot);
            } catch (IOException ignored) {}
        }
        
    }
    
    /**
     * Asigna el objetivo común de los robots de las esquinas (CORNER) una vez haya caído en combate el robot 'kamikaze'.
     */
    public void setTargetCorners(){
        
        if(targetRobot == null && !kamikazeAlive && !targetCorners.isEmpty()){
            Collections.sort(targetCorners);
            targetRobot = targetCorners.get(0);
            targetCorners.clear();
        }
        
    }
    
    /**
     * Cambia la esquina asignada al robot, y el estado a toCORNER si, y solo si, el robot esta en estado CORNER y ha transcurrido el numero de turnos ROTATETURN.
     */
    public void sentinelMovement(){
        if(STATUS == state.CORNER && getTime() > ROTATETURN*turnCounter){
            STATUS = state.toCORNER; turnCounter++;
            corner = (corner + 1) % 4;
        }
    }
    
    /**
     * Asigna el punto donde estan situadas las esquinas a las variables Corner y CornerX
     * También asigna el valor del punto inicial del robot a la variable iniP.
     */
    public void setPoints(){
        iniP = new Punt(getX(), getY());
        Corner[0] = new Punt(0.0, 0.0);
        Corner[1] = new Punt(getBattleFieldWidth(), 0.0);
        Corner[2] = new Punt(getBattleFieldWidth(), getBattleFieldHeight());
        Corner[3] = new Punt(0.0, getBattleFieldHeight());
        
        CornerX[0] = new Punt(18.0, 18.0);
        CornerX[1] = new Punt(getBattleFieldWidth()-18.0, 18.0);
        CornerX[2] = new Punt(getBattleFieldWidth()-18.0, getBattleFieldHeight()-18.0);
        CornerX[3] = new Punt(18.0, getBattleFieldHeight()-18.0);
    }
    
}