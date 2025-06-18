package chat.richieste;

public class RichiestaLogin extends RichiestaGenerale {
    private String username;
    private String password;

    public RichiestaLogin(String username, String password) {
        super(TipoRichiesta.richiestaLogin);
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
