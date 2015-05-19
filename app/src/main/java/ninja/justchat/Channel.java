package ninja.justchat;

import java.util.ArrayList;

/**
 * Created by Chad on 5/18/2015.
 */
public class Channel {

    public String name;

    public ArrayList<String> chatLog = new ArrayList<>();

    public Channel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    //public ArrayList<String> getChatLog() {
    //    return chatLog;
    //}

}
