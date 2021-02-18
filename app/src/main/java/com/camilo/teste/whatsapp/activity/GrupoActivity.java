package com.camilo.teste.whatsapp.activity;

import android.os.Bundle;

import com.camilo.teste.whatsapp.adapter.ContatosAdapter;
import com.camilo.teste.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.RecyclerItemClickListener;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.camilo.teste.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference membrosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        membrosRef = ConfiguracaoFirebase.getDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembros);

        //Configurando o recycler view para membros
        //Configurar adapter
        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());

        //Configurar o recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);

        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembros.get(position);

                        //removendo da lista de todos os membros
                        listaMembros.remove(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        //adicionando o membro no novo grupo
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        atualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configurando o recycler view para membros selecionados
        //configurando o adapter
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        //configurando o recycler view
        RecyclerView.LayoutManager layoutManagerGrupoSelecionado = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerGrupoSelecionado);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                        //remoever da lista de membros selecionados
                        listaMembrosSelecionados.remove(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        //adicionar na lista de membros ( totais )
                        listaMembros.add(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        atualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

    }

    public void recuperarMembros(){

        valueEventListenerMembros = membrosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaMembros.clear();
                for(DataSnapshot dados : snapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail()))
                        listaMembros.add(usuario);
                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarMembrosToolbar(){
        int totalMembros = listaMembros.size() + listaMembrosSelecionados.size();
        int totalMembrosSelecionados = listaMembrosSelecionados.size();

        toolbar.setSubtitle(totalMembrosSelecionados +" de " +totalMembros + " selecionados");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMembros();
    }

    @Override
    protected void onStop() {
        super.onStop();
        membrosRef.removeEventListener(valueEventListenerMembros);
    }
}
