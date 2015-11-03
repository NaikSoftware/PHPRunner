package ua.naiksoftware.phprunner;

public class Item implements SortItem {

    private int imageId, type;
    private String header, subheader;

    public Item(int imageId_, String header_, String subheader_, int type_) {
        imageId = imageId_;
        header = header_;
        subheader = subheader_;
        type = type_;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setSubheader(String subheader) {
        this.subheader = subheader;
    }

    public String getSubheader() {
        return subheader;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getSortField() {
        return header;
    }
}
