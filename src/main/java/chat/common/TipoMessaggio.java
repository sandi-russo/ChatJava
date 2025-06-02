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

    /*
    // Test enumeratore
        TipoMessaggio ciao;
        ciao = TipoMessaggio.DOCUMENTO;
        System.out.println("Ao sei un " + ciao.getTipo());


     */
}
