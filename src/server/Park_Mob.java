/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author Evil0
 */
public class Park_Mob {

    public int mode = 0;
    public int id = 0;
    public int power = 0;
    public boolean mega = false;
    public boolean kaotic = false;
    public boolean ultimate = false;
    public int cap = 0;

    public Park_Mob(int mode, int id, int power, boolean mega, boolean kaotic, boolean ultimate, int cap) {
        this.mode = mode;
        this.id = id;
        this.power = power;
        this.mega = mega;
        this.kaotic = kaotic;
        this.ultimate = ultimate;
        this.cap = cap;
        
    }

    public int getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public int getPower() {
        return power;
    }

    public boolean getMega() {
        return mega;
    }

    public boolean getkaotic() {
        return kaotic;
    }

    public boolean getUlt() {
        return ultimate;
    }
    
    public int getCap() {
        return cap;
    }

}
