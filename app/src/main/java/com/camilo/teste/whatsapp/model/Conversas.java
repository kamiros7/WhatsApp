package com.camilo.teste.whatsapp.model;

import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Conversas {

    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private String ultimaMensagem;
    private Usuario usuarioExibicao; //usuário que será exibido

    private Grupo grupo;
    private String isGroup;

    public Conversas() {
        this.setIsGroup("false");
    }

    public void salvar(){
        DatabaseReference databaseRef = ConfiguracaoFirebase.getDatabase();
        DatabaseReference conversaRef = databaseRef.child("conversas");

        conversaRef.child(this.idUsuarioRemetente)
                .child(this.idUsuarioDestinatario)
                .setValue(this);
    }

    public String getIdUsuarioRemetente() {
        return idUsuarioRemetente;
    }

    public void setIdUsuarioRemetente(String idUsuarioRemetente) {
        this.idUsuarioRemetente = idUsuarioRemetente;
    }

    public String getIdUsuarioDestinatario() {
        return idUsuarioDestinatario;
    }

    public void setIdUsuarioDestinatario(String idUsuarioDestinatario) {
        this.idUsuarioDestinatario = idUsuarioDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuario() {
        return usuarioExibicao;
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioExibicao = usuario;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }
}
