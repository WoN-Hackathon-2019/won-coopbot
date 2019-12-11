package won.bot.skeleton.model;

import java.io.Serializable;

public class Group implements Serializable {

    private String name;
    private int capacity;


    public Group(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
