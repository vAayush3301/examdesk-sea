package av.sea.examdesk.model;

import java.io.Serializable;

public class Image implements Serializable {
    private String imageKey, imageAlt;

    public Image(String imageKey, String imageAlt) {
        this.imageKey = imageKey;
        this.imageAlt = imageAlt;
    }

    public Image() {
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String getImageAlt() {
        return imageAlt;
    }

    public void setImageAlt(String imageAlt) {
        this.imageAlt = imageAlt;
    }
}
