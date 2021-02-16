package com.camilo.teste.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.activity.ChatActivity;
import com.camilo.teste.whatsapp.adapter.ConversasAdapter;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.RecyclerItemClickListener;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Conversas;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewListaConversas;
    private ArrayList<Conversas> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;

    private DatabaseReference databaseRef;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewListaConversas = view.findViewById(R.id.recyclerViewListaConversas);

        //configurar o adapter
        adapter = new ConversasAdapter(listaConversas, getActivity());
        //configurar o recycler view

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaConversas.setLayoutManager(layoutManager);
        recyclerViewListaConversas.setHasFixedSize(true);
        recyclerViewListaConversas.setAdapter(adapter);

        //configurando o evento de clique no recycler view
        recyclerViewListaConversas.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewListaConversas,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //para passar objetos para outra activity, use o put extra
                        //Contudo, na classe usuario, é preciso usar o implements serializable, para que seja permitido a passagem de dados pela intent

                        Conversas conversaSelecionada = listaConversas.get(position);

                        Intent i = new Intent(getActivity(), ChatActivity.class);
                        i.putExtra("chatContato", conversaSelecionada.getUsuario());
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configurando a referencia para as conversas no database
        databaseRef = ConfiguracaoFirebase.getDatabase();
        String idUsuario = UsuarioFirebase.getIdUsuario();
        conversasRef = databaseRef.child("conversas")
                .child(idUsuario);

        return  view;
    }

    public void pesquisarConversas(String texto){
        List<Conversas> listaConversasBusca = new ArrayList<>();
        for(Conversas conversa : listaConversas){
            String nome = conversa.getUsuario().getNome().toLowerCase();

            if(nome.contains(texto)){
                listaConversasBusca.add(conversa);
            }
        }

        ConversasAdapter adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){


        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversas conversa = snapshot.getValue(Conversas.class);
                listaConversas.add(conversa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recarregarConversas(){

        //Essa função tem o intuito de apenas atualizar o adapter com a lista de conversas (geral)
        //como a lista já está criada, e o child event listner também, só é preciso trocar a lista para o adapter
        ConversasAdapter adapter = new ConversasAdapter(listaConversas, getActivity());
        recyclerViewListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        listaConversas.clear();
        conversasRef.removeEventListener(childEventListenerConversas);
    }
}
