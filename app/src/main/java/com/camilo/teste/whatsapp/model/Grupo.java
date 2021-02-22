package com.camilo.teste.whatsapp.model;

import android.util.Log;
import android.widget.Toast;

import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.Base64Custom;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class Grupo implements Serializable {
    private String id;
    private String nome;
    private String foto;
    private List<Usuario> membros;


    public Grupo() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabase();
        DatabaseReference grupoRef = databaseReference.child("grupos");

        //Faço o push ( crio um filho com um idUnico) e obtenho esse id através do getKey
        String idGrupoFirebase = grupoRef.push().getKey();
        setId(idGrupoFirebase);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }

    public void salvar(){
        DatabaseReference databaseRef = ConfiguracaoFirebase.getDatabase();
        DatabaseReference grupoRef = databaseRef.child("grupos");

        //os filhos do nó grupo, são os id unicos de cada grupo e os valores desse nó são os próprios dados do grupo
        grupoRef.child(getId()).setValue(this);

        for(Usuario membro : getMembros()){

            String idMembro = membro.getEmail();
            String idRemetente = Base64Custom.codigicarBase64(idMembro);
            String idDestinatario = getId();

            Conversas conversa = new Conversas();
            conversa.setIdUsuarioRemetente(idRemetente);
            conversa.setIdUsuarioDestinatario(idDestinatario);
            conversa.setUltimaMensagem("");
            conversa.setIsGroup("true");
            conversa.setGrupo(this);

            conversa.salvar();

        }

    }
}
