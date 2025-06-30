package client.maplepal;

public class MaplePalData {

    public int id, time;
    public String name;
    public byte element;

    public MaplePalData(int id, String name, byte element, int time) {
        this.id = id;
        this.name = name;
        this.element = element;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte getType() {
        return element;
    }

}
