package win.aladhims.meetme.Model;

/**
 * Created by Aladhims on 20/03/2017.
 */

public class Chat {

    private String fromUid;
    private String toUid;

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    private String pesan;

    public String getFromUid() {
        return fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public String getPesan() {
        return pesan;
    }


    public String getFotoPesanURL() {
        return fotoPesanURL;
    }

    private String fotoPesanURL;

    public Chat(){}

    public Chat(String fromUid,String toUid,String pesan,String url){
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.pesan = pesan;
        this.fotoPesanURL = url;
    }

}
