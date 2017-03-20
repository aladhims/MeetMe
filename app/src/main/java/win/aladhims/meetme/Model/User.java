package win.aladhims.meetme.Model;

/**
 * Created by Aladhims on 20/03/2017.
 */

public class User {

    private String photoURL,name,email;

    public User(){}

    public User(String photoURL,String name,String email){
        this.photoURL = photoURL;
        this.name = name;
        this.email = email;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
