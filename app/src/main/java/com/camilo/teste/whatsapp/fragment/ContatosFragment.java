package com.camilo.teste.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.activity.ChatActivity;
import com.camilo.teste.whatsapp.activity.GrupoActivity;
import com.camilo.teste.whatsapp.adapter.ContatosAdapter;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.RecyclerItemClickListener;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {


    private RecyclerView recyclerViewContatos;
    private ContatosAdapter contatosAdapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contatos, container, false);


        recyclerViewContatos = view.findViewById(R.id.recyclerViewContatos);
        usuariosRef = ConfiguracaoFirebase.getDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //configurar o adapter
        contatosAdapter = new ContatosAdapter(listaContatos, getActivity());


        //configurar o recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewContatos.setLayoutManager(layoutManager);
        recyclerViewContatos.setHasFixedSize(true);
        recyclerViewContatos.setAdapter(contatosAdapter);

        //configurar envento de clique no recycler view
        recyclerViewContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                //para passar objetos para outra activity, use o put extra
                                //Contudo, na classe usuario, é preciso usar o implements serializable, para que seja permitido a passagem de dados pela intent
                                Usuario usuarioSelecionado = listaContatos.get(position);
                                boolean itemGrupo = usuarioSelecionado.getEmail().isEmpty(); //itemGrupo se refere ao item para criar o novo grupo na lista de contatos
                                if(itemGrupo){
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);
                                }else{
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);
                                    startActivity(i);
                                }

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        adicionaItemGrupo();


        return view;
    }


    public void recuperarContatos(){

       valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaContatos.clear();

                adicionaItemGrupo();
                contatosAdapter.notifyDataSetChanged();

                for(DataSnapshot dados : snapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail()))
                        listaContatos.add(usuario);
                }

                contatosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void adicionaItemGrupo(){
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setEmail(""); //email vazio, pois como qualquer usuario tem email, o usuario com email vazio será o item para grupos
        listaContatos.add(itemGrupo);
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }
}
