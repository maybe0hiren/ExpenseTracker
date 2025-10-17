public class ReceiverModel {
    String receiver;
    String group;
    String colour;

    public ReceiverModel(String receiver, String group, String colour) {
        this.receiver = receiver;
        this.group = group;
        this.colour = colour;
    }

    public String getReceiver() { return receiver; }
    public String getGroup() { return group; }
    public String getColour() { return colour; }
}
