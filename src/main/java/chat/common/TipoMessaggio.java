package chat.common;

public enum TipoMessaggio {

    TESTO("1"),
    DOCUMENTO("2");

    private String suffisso;
    private String tipo;

    TipoMessaggio(String tipo){
        this.tipo = tipo;
    }

    public String getTipo(){
        return tipo;
    }
}
