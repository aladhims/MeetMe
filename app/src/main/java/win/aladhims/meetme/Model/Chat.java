package win.aladhims.meetme.Model;

/**
 * Created by Aladhims on 20/03/2017.
 */

public class Chat {

    String fromUid;
    String toUid;
    String pesan;
    String FotoPesanURL;

    public Chat(){}

    public Chat(String fromUid,String toUid,String pesan,String fotoPesanURL){
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.pesan = pesan;
        this.FotoPesanURL = fotoPesanURL;
    }

    public String getFromUid() {
        return fromUid;
    }
    public String getFotoPesanURL() {
        return FotoPesanURL;
    }

    public void setFotoPesanURL(String fotoPesanURL) {
        FotoPesanURL = fotoPesanURL;
    }


    public String getToUid() {
        return toUid;
    }

    public String getPesan() {
        return pesan;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }
}
