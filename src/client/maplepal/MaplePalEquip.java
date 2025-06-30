package client.maplepal;

public class MaplePalEquip {

    public int id, hp, str, dex, int_, luk, atk, def, matk, mdef, speed, damage, defense;
    public String name;

    public MaplePalEquip(final int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setHp(int value) {
        this.hp = value;
    }

    public void setStr(int value) {
        this.str = value;
    }

    public void setDex(int value) {
        this.dex = value;
    }

    public void setInt(int value) {
        this.int_ = value;
    }

    public void setLuk(int value) {
        this.luk = value;
    }

    public void setAtk(int value) {
        this.atk = value;
    }

    public void setMatk(int value) {
        this.matk = value;
    }

    public void setDef(int value) {
        this.def = value;
    }

    public void setMdef(int value) {
        this.mdef = value;
    }

    public void setSpeed(int value) {
        this.speed = value;
    }

    public void setDamage(int value) {
        this.damage = value;
    }

    public void setDefense(int value) {
        this.defense = value;
    }

    public int getId() {
        return id;
    }

    public int getHp() {
        return hp;
    }

    public int getStr() {
        return str;
    }

    public int getDex() {
        return dex;
    }

    public int getInt() {
        return int_;
    }

    public int getLuk() {
        return luk;
    }

    public int getAtk() {
        return atk;
    }

    public int getMatk() {
        return matk;
    }

    public int getDef() {
        return def;
    }

    public int getMdef() {
        return mdef;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public int getDefense() {
        return defense;
    }

}
